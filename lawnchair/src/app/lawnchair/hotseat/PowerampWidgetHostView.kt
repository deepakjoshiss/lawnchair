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
 */
package app.lawnchair.hotseat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewDebug
import android.view.ViewGroup
import android.widget.RemoteViews
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.widget.NavigableAppWidgetHostView

/**
 * Appwidget host view with QSB specific logic.
 */
open class PowerampWidgetHostView(context: Context?) : NavigableAppWidgetHostView(context) {
    @ViewDebug.ExportedProperty(category = "launcher")
    private var mPreviousOrientation = 0

    init {
        isFocusable = true
        setBackgroundResource(R.drawable.qsb_host_view_focus_bg)
    }

    override fun updateAppWidget(remoteViews: RemoteViews) {
        // Store the orientation in which the widget was inflated
        mPreviousOrientation = resources.configuration.orientation
        super.updateAppWidget(remoteViews)
    }


    fun isReinflateRequired(orientation: Int): Boolean {
        // Re-inflate is required if the orientation has changed since last inflation.
        return mPreviousOrientation != orientation
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        // Prevent the base class from applying the default widget padding.
        super.setPadding(0, 0, 0, 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        try {
            super.onLayout(changed, left, top, right, bottom)
        } catch (e: RuntimeException) {
            // Update the widget with 0 Layout id, to reset the view to error view.
            post {
                updateAppWidget(
                    RemoteViews(appWidgetInfo.provider.packageName, 0),
                )
            }
        }
    }

    override fun getErrorView(): View {
        return getDefaultView(this)
    }

    override fun getDefaultView(): View {
        val v = super.getDefaultView()
        v.setOnClickListener { v2: View? ->
            Launcher.getLauncher(
                context,
            ).startSearch("", false, null, true)
        }
        return v
    }

    override fun shouldAllowDirectClick(): Boolean {
        return true
    }

    companion object {
        fun getDefaultView(parent: ViewGroup): View {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.qsb_default_view, parent, false)
            v.findViewById<View>(R.id.btn_qsb_search).setOnClickListener { v2: View ->
                Launcher.getLauncher(
                    v2.context,
                ).startSearch("", false, null, true)
            }
            v.findViewById<View>(R.id.qsb_background).clipToOutline = true
            return v
        }
    }
}
