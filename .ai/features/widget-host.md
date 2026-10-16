# widget-host: transparent Google Calendar "schedule" widget

status: wip — built 2026-10-17; look unverified (emulator Calendar not signed in), owner checks on a device

## What the user sees

Google Calendar's agenda widget ("Calendar schedule") on the home screen or dock loses its card:
no background, no add-event button, softer event colour stripes (65 %), tighter month headers,
a little more space between items and faded top/bottom edges on the list; 16dp padding left,
8dp right. No setting; other widgets are untouched.

## Settings

None (hard-coded on 15).

## On 15 (reference)

All in `NavigableAppWidgetHostView.java`:

- `bc555f2b26` (2024-08-24) "fix dock widget, Transparent calendar widget": first version
  (child-index walk in `onLayout`, stripe alpha 0.80, FAB scaled/moved).
- `e5ed825962` (2024-08-25): FAB alpha 0.
- `e89c4d3f8f` (2024-08-27) "Calender widget by id": final shape. `setAppWidget` marks the widget
  when `info.label` is "Calendar schedule" and sets padding (16dp, 0, 8dp, 0); an `addView`
  override catches each RemoteViews root; views are found by Calendar's resource names
  (`com.google.android.calendar:id/…`: `event_chip`, `widget_row`, `agenda_item_color`,
  `widget_month_label`, `accessibility_fab_target`, `ghost_chip`) and hierarchy listeners handle
  rows inflated later. Root background `GradientDrawable` alpha 0; root child 2 (add-event
  button, inferred) alpha 0; list divider 6dp (reuses an all-apps dimen), fading edge 24dp.
- `c6dcab5af1` (2025-01-25): month header padding (0, 4dp, 0, 8dp).
- Quirks: matches the English label only; a debug `println`; a layout dump of the widget kept as
  a comment (useful reference). Fails silently if Calendar renames its view ids.

## On 16

Overlap: none (no calendar handling in 16's widget host). → full port. Hook: the same two spots
in `NavigableAppWidgetHostView` (padding after `setAppWidget`, `addView`), logic in a Lawnchair
layer class.

## Improvements (owner confirmed 2026-10-17: match by provider)

1. Match the widget by its provider (`com.google.android.calendar` /
   `com.android.calendar.widget.CalendarAppWidgetProvider`) instead of the English label, so it
   works in any language.
2. Own dimens for the divider and fade instead of reusing an all-apps dimen; no debug output.

## Acceptance

Emulator, Google Calendar's agenda widget placed on home:

1. Card background and add-event button gone; event stripes at 65 %; list fades at the edges.
   Evidence: before/after screenshots.
2. Survives a resize, a launcher restart and new events (rows inflated later are styled too).
3. Other widgets (Calendar month view, clock, Chrome Dino) unchanged. Evidence: screenshots.
4. With improvement 1: still applied with the launcher in another language (Hebrew locale).
5. No crashes.
