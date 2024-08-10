/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright 2021, Lawnchair
 */
package app.lawnchair.hotseat

import android.app.Activity
import android.app.SearchManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.WorkerThread
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherPrefs.Companion.getPrefs
import com.android.launcher3.R
import com.android.launcher3.config.FeatureFlags
import com.android.launcher3.graphics.FragmentWithPreview
import com.android.launcher3.widget.util.WidgetSizes

/**
 * A frame layout which contains a QSB. This internally uses fragment to bind the view, which
 * allows it to contain the logic for [Fragment.startActivityForResult].
 *
 *
 * Note: WidgetManagerHelper can be disabled using FeatureFlags. In QSB, we should use
 * AppWidgetManager directly, so that it keeps working in that case.
 */
open class PowerampWidgetContainerView : FrameLayout {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs,
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr,
    )

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    protected fun setPaddingUnchecked(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
    }

    /**
     * A fragment to display the QSB.
     */
    class QsbFragment : FragmentWithPreview() {
        protected var mKeyWidgetId: String = "qsb_widget_id"
        private var mQsbWidgetHost: PowerampWidgetHost? = null
        protected var mWidgetInfo: AppWidgetProviderInfo? = null
        private var mQsb: PowerampWidgetHostView? = null

        // We need to store the orientation here, due to a bug (b/64916689) that results in widgets
        // being inflated in the wrong orientation.
        private var mOrientation = 0

        override fun onInit(savedInstanceState: Bundle?) {
            mQsbWidgetHost = createHost()
            mOrientation = context.resources.configuration.orientation
        }

        protected fun createHost(): PowerampWidgetHost {
            return PowerampWidgetHost(
                context, QSB_WIDGET_HOST_ID,
                object : WidgetViewFactory {
                    override fun newView(context: Context?): PowerampWidgetHostView {
                        return PowerampWidgetHostView(context);
                    }
                },
                object : WidgetProvidersUpdateCallback {
                    override fun onProvidersUpdated() {
                        rebindFragment();
                    }
                },
            )
        }

        private var mWrapper: FrameLayout? = null

        override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            mWrapper = createWrapper(context)
            // Only add the view when enabled
            if (isQsbEnabled) {
                mQsbWidgetHost!!.startListening()
                mWrapper!!.addView(createQsb(mWrapper!!))
            }
            return mWrapper!!
        }

        protected fun createWrapper(context: Context): FrameLayout {
            return FrameLayout(context)
        }

        private fun createQsb(container: ViewGroup): View? {
            mWidgetInfo = searchWidgetProvider
            if (mWidgetInfo == null) {
                // There is no search provider, just show the default widget.
                return getDefaultView(container, false /* show setup icon */)
            }
            val opts = createBindOptions()
            val context = context
            val widgetManager = AppWidgetManager.getInstance(context)

            var widgetId = getPrefs(context).getInt(mKeyWidgetId, -1)
            val widgetInfo = widgetManager.getAppWidgetInfo(widgetId)
            var isWidgetBound =
                (widgetInfo != null) && (widgetInfo.provider == mWidgetInfo!!.provider)

            val oldWidgetId = widgetId
            if (!isWidgetBound && !isInPreviewMode) {
                if (widgetId > -1) {
                    // widgetId is already bound and its not the correct provider. reset host.
                    mQsbWidgetHost!!.deleteHost()
                }

                widgetId = mQsbWidgetHost!!.allocateAppWidgetId()
                isWidgetBound = widgetManager.bindAppWidgetIdIfAllowed(
                    widgetId, mWidgetInfo!!.profile, mWidgetInfo!!.provider, opts,
                )
                if (!isWidgetBound) {
                    mQsbWidgetHost!!.deleteAppWidgetId(widgetId)
                    widgetId = -1
                }

                if (oldWidgetId != widgetId) {
                    saveWidgetId(widgetId)
                }
                if (!isInPreviewMode) {
                    startConfigureFlow(widgetId, mWidgetInfo!!)
                }
            }

            if (isWidgetBound) {
                mQsb = mQsbWidgetHost!!.createView(
                    context, widgetId,
                    mWidgetInfo,
                ) as PowerampWidgetHostView
                mQsb!!.id = R.id.qsb_widget

                if (!isInPreviewMode) {
                    if (!containsAll(
                            AppWidgetManager.getInstance(context)
                                .getAppWidgetOptions(widgetId),
                            opts,
                        )
                    ) {
                        mQsb!!.updateAppWidgetOptions(opts)
                    }
                }
                return mQsb
            }

            // Return a default widget with setup icon.
            return getDefaultView(container, true /* show setup icon */)
        }

        fun startConfigureFlow(appWidgetId: Int, info: AppWidgetProviderInfo) {
            val intent = Intent()
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.setComponent(info.configure)
            // TODO: we need to make sure that this accounts for the options bundle.
            // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
            startActivityForResult(intent, REQUEST_CONFIGURE)
        }

        private fun saveWidgetId(widgetId: Int) {
            getPrefs(context).edit().putInt(mKeyWidgetId, widgetId).apply()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            if (requestCode == REQUEST_BIND_QSB) {
                println(">>>>> gotResult code $resultCode")
                if (resultCode == Activity.RESULT_OK) {
                    saveWidgetId(data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
                    rebindFragment()
                } else {
                    mQsbWidgetHost!!.deleteHost()
                }
            }
        }

        override fun onResume() {
            super.onResume()
            if (mQsb != null && mQsb!!.isReinflateRequired(mOrientation)) {
                rebindFragment()
            }
        }

        override fun onDestroy() {
            mQsbWidgetHost!!.stopListening()
            super.onDestroy()
        }

        private fun rebindFragment() {
            // Exit if the embedded qsb is disabled
            if (!isQsbEnabled) {
                return
            }

            if (mWrapper != null && context != null) {
                mWrapper!!.removeAllViews()
                mWrapper!!.addView(createQsb(mWrapper!!))
            }
        }

        val isQsbEnabled: Boolean
            get() = FeatureFlags.topQsbOnFirstScreenEnabled(context)

        protected fun createBindOptions(): Bundle {
            val idp = LauncherAppState.getIDP(context)
            return WidgetSizes.getWidgetSizeOptions(
                context, mWidgetInfo!!.provider,
                idp.numColumns, 1,
            )
        }

        protected fun getDefaultView(container: ViewGroup, showSetupIcon: Boolean): View {
            // Return a default widget with setup icon.
            val v = PowerampWidgetHostView.getDefaultView(container)
            if (showSetupIcon) {
                requestQsbCreate()
                val setupButton = v.findViewById<View>(R.id.btn_qsb_setup)
                setupButton.visibility = VISIBLE
                setupButton.setOnClickListener { v2: View? -> requestQsbCreate() }
            }
            return v
        }

        fun requestQsbCreate() {
            startActivityForResult(
                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        mQsbWidgetHost!!.allocateAppWidgetId(),
                    )
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, mWidgetInfo!!.provider),
                REQUEST_BIND_QSB,
            )
        }

        @get:WorkerThread
        protected val searchWidgetProvider: AppWidgetProviderInfo?
            /**
             * Returns a widget with category [AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX]
             * provided by the package from getSearchProviderPackageName
             * If widgetCategory is not supported, or no such widget is found, returns the first widget
             * provided by the package.
             */
            get() = getSearchWidgetProviderInfo(
                context,
            )

        companion object {
            const val QSB_WIDGET_HOST_ID: Int = 1026
            private const val REQUEST_BIND_QSB = 1
            private const val REQUEST_CONFIGURE = 2
        }
    }

    class PowerampWidgetHost @JvmOverloads constructor(
        context: Context?, hostId: Int, private val mViewFactory: WidgetViewFactory,
        private val mWidgetsUpdateCallback: WidgetProvidersUpdateCallback? = null,
    ) :
        AppWidgetHost(context, hostId) {
        override fun onCreateView(
            context: Context, appWidgetId: Int, appWidget: AppWidgetProviderInfo,
        ): AppWidgetHostView {
            return mViewFactory.newView(context)
        }

        override fun onProvidersChanged() {
            super.onProvidersChanged()
            mWidgetsUpdateCallback?.onProvidersUpdated()
        }
    }

    interface WidgetViewFactory {
        fun newView(context: Context?): PowerampWidgetHostView
    }

    /**
     * Callback interface for packages list update.
     */
    interface WidgetProvidersUpdateCallback {
        /**
         * Gets called when widget providers list changes
         */
        fun onProvidersUpdated()
    }

    companion object {
        const val SEARCH_PROVIDER_SETTINGS_KEY: String = "SEARCH_PROVIDER_PACKAGE_NAME"

        /**
         * Returns the package name for user configured search provider or from searchManager
         *
         * @param context
         * @return String
         */
        @WorkerThread
        fun getSearchWidgetPackageName(context: Context): String? {
            var providerPkg = "com.maxmpz.audioplayer"
            if (providerPkg == null) {
                val searchManager = context.getSystemService(
                    SearchManager::class.java,
                )
                val componentName = searchManager.globalSearchActivity
                if (componentName != null) {
                    providerPkg = searchManager.globalSearchActivity.packageName
                }
            }
            return providerPkg
        }

        /**
         * returns it's AppWidgetProviderInfo using package name from getSearchWidgetPackageName
         *
         * @param context
         * @return AppWidgetProviderInfo
         */
        @WorkerThread
        fun getSearchWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
            return getSearchWidgetProviderInfo(context, getSearchWidgetPackageName(context))
        }

        fun getSearchWidgetProviderInfo(
            context: Context,
            providerPkg: String?,
        ): AppWidgetProviderInfo? {
            if (providerPkg == null) {
                return null
            }

            var defaultWidgetForSearchPackage: AppWidgetProviderInfo? = null
            val appWidgetManager = AppWidgetManager.getInstance(context)
            for (info in appWidgetManager.getInstalledProvidersForPackage(providerPkg, null)) {
                println(">>>>>>> " + info.configure)
                if (info.provider.packageName == providerPkg && info.provider.className == "com.maxmpz.audioplayer.widgetpackcommon.Widget4x1Provider") {
                    return info
                } else if (defaultWidgetForSearchPackage == null) {
                    defaultWidgetForSearchPackage = info
                }
            }
            return defaultWidgetForSearchPackage
        }

        /**
         * returns componentName for searchWidget if package name is known.
         */
        @WorkerThread
        fun getSearchComponentName(context: Context): ComponentName? {
            val providerInfo =
                getSearchWidgetProviderInfo(context)
            if (providerInfo != null) {
                return providerInfo.provider
            } else {
                val pkgName = getSearchWidgetPackageName(context)
                if (pkgName != null) {
                    //we don't know the class name yet. we'll put the package name as placeholder
                    return ComponentName(pkgName, pkgName)
                }
                return null
            }
        }

        /**
         * Returns true if {@param original} contains all entries defined in {@param updates} and
         * have the same value.
         * The comparison uses [Object.equals] to compare the values.
         */
        private fun containsAll(original: Bundle, updates: Bundle): Boolean {
            for (key in updates.keySet()) {
                val value1 = updates[key]
                val value2 = original[key]
                if (value1 == null) {
                    if (value2 != null) {
                        return false
                    }
                } else if (value1 != value2) {
                    return false
                }
            }
            return true
        }
    }
}
