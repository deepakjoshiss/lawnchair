package app.lawnchair.allapps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached
import com.android.launcher3.DeviceProfile
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.ActivityAllAppsContainerView

/**
 * DJ(drawer-glass): "Scroll apps under search bar" (Android 12+, full-screen drawer, no work tabs).
 *
 * The A-Z and search-result lists move up to the top of the screen, behind the status bar
 * ([extraTop]), and get the same amount of extra top padding, so nothing moves at rest. They are
 * no longer clipped at the search bar: apps stay sharp around the search box. Behind the status
 * bar and inside the search pill they are frosted: the container skips the sharp list there
 * ([clipOutPill]) and a [Backdrop] draws a blurred copy of the list in its place. The
 * suggestion rows (floating header) are not moved but may draw above it, so they pass under too.
 * DECISIONS #29.
 */
object DrawerScrollUnder {

    private const val PILL_BLUR_DP = 24f
    private const val STATUS_BAR_BLUR_DP = 72f // owner's choice

    @JvmStatic
    fun isOn(context: Context, dp: DeviceProfile) = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        !dp.shouldShowAllAppsOnSheet() &&
        PreferenceManager2.getInstance(context).drawerSearchScrollUnder.firstCached()

    /** How far the lists move up: from below the search box to the top of the screen. */
    @JvmStatic
    fun extraTop(context: Context, dp: DeviceProfile) = if (isOn(context, dp)) {
        context.resources.getDimensionPixelSize(R.dimen.all_apps_header_top_margin) + dp.insets.top +
            context.resources.getDimensionPixelSize(R.dimen.dj_drawer_search_top_gap)
    } else {
        0
    }

    /** Adds the blurred backdrop just below the search bar in the drawer's drawing order. */
    @JvmStatic
    fun attach(apps: ActivityAllAppsContainerView<*>, searchContainer: View, dp: DeviceProfile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !isOn(apps.context, dp)) return
        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT,
        )
        apps.addView(Backdrop(apps, searchContainer), apps.indexOfChild(searchContainer), lp)
        // Suggestion rows move above the header's own top; let them draw there.
        apps.floatingHeaderView?.clipChildren = false
    }

    /**
     * Called from the container's drawChild: for the lists, cuts the pill's outline out of the
     * canvas so the sharp list does not show through the frosted copy. Returns the save count to
     * restore, or -1 when nothing was clipped.
     */
    @JvmStatic
    fun clipOutPill(
        apps: ActivityAllAppsContainerView<*>,
        searchContainer: View,
        dp: DeviceProfile,
        canvas: Canvas,
        child: View,
    ): Int {
        if (child !== apps.appsRecyclerViewContainer &&
            child !== apps.searchRecyclerView &&
            child !== apps.floatingHeaderView
        ) {
            return -1
        }
        if (!isOn(apps.context, dp)) return -1
        val path = frostPath(apps, searchContainer) ?: return -1
        val save = canvas.save()
        canvas.clipOutPath(path)
        return save
    }

    private val tmpLoc = IntArray(2)
    private val tmpLoc2 = IntArray(2)
    private val tmpRect = RectF()
    private val tmpPath = Path()

    /** The pill's rounded outline in [inView]'s coordinates, or null if it is not laid out. */
    private fun pillRect(inView: View, searchContainer: View, out: RectF): Boolean {
        val pill = searchContainer.findViewById<View>(R.id.qsb_shell) ?: return false
        if (pill.width == 0 || !searchContainer.isShown) return false
        inView.getLocationInWindow(tmpLoc)
        pill.getLocationInWindow(tmpLoc2)
        val l = (tmpLoc2[0] - tmpLoc[0]).toFloat()
        val t = (tmpLoc2[1] - tmpLoc[1]).toFloat()
        out.set(l, t, l + pill.width, t + pill.height)
        return true
    }

    /** Bottom of the status bar in [inView]'s coordinates: the search box top minus its gap. */
    private fun statusBarBottom(inView: View, searchContainer: View): Float {
        inView.getLocationInWindow(tmpLoc)
        searchContainer.getLocationInWindow(tmpLoc2)
        val gap = inView.resources.getDimensionPixelSize(R.dimen.dj_drawer_search_top_gap)
        return (tmpLoc2[1] - tmpLoc[1] - gap).toFloat()
    }

    /** The frosted area: the pill's rounded outline plus the strip behind the status bar. */
    private fun frostPath(inView: View, searchContainer: View): Path? {
        if (!pillRect(inView, searchContainer, tmpRect)) return null
        val r = tmpRect.height() / 2f
        tmpPath.rewind()
        tmpPath.addRoundRect(tmpRect, r, r, Path.Direction.CW)
        inView.getLocationInWindow(tmpLoc)
        val screenTop = -tmpLoc[1].toFloat()
        tmpPath.addRect(0f, screenTop, inView.width.toFloat(), statusBarBottom(inView, searchContainer), Path.Direction.CW)
        return tmpPath
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class Backdrop(
        private val apps: ActivityAllAppsContainerView<*>,
        private val searchContainer: View,
    ) : View(apps.context) {

        private val pillBlur = Utilities.dpToPx(PILL_BLUR_DP)
        private val statusBlur = Utilities.dpToPx(STATUS_BAR_BLUR_DP)
        private val pillNode = blurNode("drawerScrollUnderPill", pillBlur)
        private val statusNode = blurNode("drawerScrollUnderStatusBar", statusBlur)
        private val loc = IntArray(2)
        private val rvLoc = IntArray(2)
        private val pill = RectF()
        private val pillPath = Path()

        private fun blurNode(name: String, radius: Int) = RenderNode(name).apply {
            setRenderEffect(RenderEffect.createBlurEffect(radius.toFloat(), radius.toFloat(), Shader.TileMode.DECAL))
        }
        private var lastKey = 0L

        // Redraw only when the list moved; checked before each frame, so no extra frames.
        private val preDraw = ViewTreeObserver.OnPreDrawListener {
            val rv = apps.activeRecyclerView
            val key = if (rv == null) {
                0L
            } else {
                (System.identityHashCode(rv).toLong() shl 32) xor
                    (rv.computeVerticalScrollOffset().toLong() shl 8) xor rv.childCount.toLong()
            }
            if (key != lastKey) {
                lastKey = key
                invalidate()
            }
            true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            viewTreeObserver.addOnPreDrawListener(preDraw)
        }

        override fun onDetachedFromWindow() {
            viewTreeObserver.removeOnPreDrawListener(preDraw)
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            val rv: ViewGroup = apps.activeRecyclerView ?: return
            if (!canvas.isHardwareAccelerated || !rv.isShown) return
            if (!pillRect(this, searchContainer, pill)) return
            getLocationInWindow(loc)
            val screenTop = -loc[1]
            val statusBottom = statusBarBottom(this, searchContainer)

            // Each layer records only its own area plus the blur's reach, so the blur stays small.
            record(statusNode, rv, -statusBlur * 2, screenTop - statusBlur * 2, width + statusBlur * 2, (statusBottom + statusBlur * 2).toInt())
            record(pillNode, rv, (pill.left - pillBlur * 2).toInt(), (pill.top - pillBlur * 2).toInt(), (pill.right + pillBlur * 2).toInt(), (pill.bottom + pillBlur * 2).toInt())

            canvas.save()
            canvas.clipRect(0f, screenTop.toFloat(), width.toFloat(), statusBottom)
            canvas.drawRenderNode(statusNode)
            canvas.restore()

            val r = pill.height() / 2f
            pillPath.rewind()
            pillPath.addRoundRect(pill, r, r, Path.Direction.CW)
            canvas.save()
            canvas.clipPath(pillPath)
            canvas.drawRenderNode(pillNode)
            canvas.restore()
        }

        /** Records the list and the suggestion rows into [node], positioned at the given bounds. */
        private fun record(node: RenderNode, rv: View, left: Int, top: Int, right: Int, bottom: Int) {
            node.setPosition(left, top, right, bottom)
            val rc = node.beginRecording()
            rv.getLocationInWindow(rvLoc)
            rc.save()
            rc.translate((rvLoc[0] - loc[0] - left).toFloat(), (rvLoc[1] - loc[1] - top).toFloat())
            rv.draw(rc) // public draw(): content only, without the clip bounds set on the list
            rc.restore()
            // The suggestion rows float above the list in their own header view.
            val header = apps.floatingHeaderView
            if (header != null && header.isShown) {
                header.getLocationInWindow(rvLoc)
                rc.translate((rvLoc[0] - loc[0] - left).toFloat(), (rvLoc[1] - loc[1] - top).toFloat())
                header.draw(rc)
            }
            node.endRecording()
            node.alpha = rv.alpha
        }
    }
}
