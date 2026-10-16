package app.lawnchair.widget

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.android.launcher3.R

/**
 * DJ(widget-host): Google Calendar's agenda ("Calendar schedule") widget drawn without its card
 * (DECISIONS #21): no background or add-event button, event colour stripes at 65 %, tighter
 * month headers, more space between items and faded list edges.
 *
 * Calendar's views are found by their resource names; if Calendar renames them, nothing matches
 * and the widget simply keeps its own look.
 */
object CalendarScheduleWidget {

    private val PROVIDER = ComponentName(
        "com.google.android.calendar",
        "com.android.calendar.widget.CalendarAppWidgetProvider",
    )
    private const val STRIPE_ALPHA = 0.65f

    @JvmStatic
    fun matches(info: AppWidgetProviderInfo?): Boolean = info?.provider == PROVIDER

    /** Host padding: 16dp left, 8dp right (15's values). */
    @JvmStatic
    fun applyPadding(host: View) {
        val res = host.resources
        host.setPadding(
            res.getDimensionPixelSize(R.dimen.dj_calendar_widget_padding_start),
            0,
            res.getDimensionPixelSize(R.dimen.dj_calendar_widget_padding_end),
            0,
        )
    }

    /**
     * Styles the widget's root view each time the [host] adds one (inflate, update, resize).
     * Sizes come from the host: the root's resources are Calendar's.
     */
    @JvmStatic
    fun style(host: View, root: View) {
        if (root !is ViewGroup || root.childCount == 0) return
        val list = root.getChildAt(0) as? ListView ?: return
        val background = root.background as? GradientDrawable ?: return

        if (root.childCount >= 3) root.getChildAt(2).alpha = 0f // the add-event button
        background.alpha = 0

        val ids = Ids(list.resources)
        val monthPadding = host.resources.getDimensionPixelSize(R.dimen.dj_calendar_widget_month_padding)
        list.dividerHeight = host.resources.getDimensionPixelSize(R.dimen.dj_calendar_widget_divider)
        list.isVerticalFadingEdgeEnabled = true
        list.setFadingEdgeLength(list.dividerHeight * 4)
        list.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                val row = child as? ViewGroup ?: return
                (row.getChildAt(0) as? ViewGroup)?.let { styleRow(row, it, ids, monthPadding) }
            }

            override fun onChildViewRemoved(parent: View, child: View) {
                (child as? ViewGroup)?.setOnHierarchyChangeListener(null)
            }
        })
    }

    private fun styleRow(row: ViewGroup, content: ViewGroup, ids: Ids, monthPadding: Int) {
        when (content.id) {
            ids.eventChip, ids.widgetRow ->
                content.findViewById<View>(ids.itemColor)?.alpha = STRIPE_ALPHA

            ids.monthLabel -> content.findViewById<View>(ids.fab)?.let {
                it.visibility = View.GONE
                val header = content.getChildAt(0)
                header.minimumHeight = 0
                header.setPadding(0, monthPadding / 2, 0, monthPadding)
            }

            // Rows filled in later (after a placeholder) arrive as new children of the row
            ids.ghostChip -> row.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View, child: View) {
                    (child as? ViewGroup)?.let { styleRow(row, it, ids, monthPadding) }
                }

                override fun onChildViewRemoved(parent: View, child: View) = Unit
            })
        }
    }

    /** Calendar's view ids, looked up by name in Calendar's own resources (0 when missing). */
    private class Ids(res: Resources) {
        private fun id(res: Resources, name: String) = res.getIdentifier("com.google.android.calendar:id/$name", null, null)

        val itemColor = id(res, "agenda_item_color")
        val widgetRow = id(res, "widget_row")
        val eventChip = id(res, "event_chip")
        val monthLabel = id(res, "widget_month_label")
        val ghostChip = id(res, "ghost_chip")
        val fab = id(res, "accessibility_fab_target")
    }
}
