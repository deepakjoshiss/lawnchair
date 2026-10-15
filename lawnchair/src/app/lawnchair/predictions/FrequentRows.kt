package app.lawnchair.predictions

import kotlin.math.min

/**
 * DJ(drawer-frequent): how many suggested apps the app drawer shows.
 *
 * Row 1 always shows. Extra full rows (at most [maxExtraRows]) show only for apps launched at least
 * half as often as the least-launched app in row 1, and at least [minLaunches] times. Row 1 must
 * itself be made of such apps; when it is padded with fallback suggestions there are no extra rows.
 *
 * @param launchCounts launch count of each suggestion, in suggestion order (most launched first).
 * @param columns apps per row.
 * @return number of suggestions to keep: [columns] times the number of rows, or fewer if there
 *   are fewer suggestions than one row.
 */
fun frequentSuggestionCount(
    launchCounts: List<Int>,
    columns: Int,
    maxExtraRows: Int = 2,
    minLaunches: Int = 3,
): Int {
    if (columns <= 0) return 0
    if (launchCounts.size <= columns) return launchCounts.size
    val weakest = launchCounts.take(columns).min()
    if (weakest < minLaunches) return columns
    val qualifying = launchCounts.drop(columns)
        .takeWhile { it >= minLaunches && it * 2 >= weakest }
        .size
    return columns + min(qualifying / columns, maxExtraRows) * columns
}
