package app.lawnchair.util

/**
 * DJ(home-grid): cell geometry for a home screen grid whose cells may be smaller than an icon and
 * its label (DECISIONS #14).
 *
 * Each cell is drawn at `drawn` size when that is bigger than its slot (`cell`). The extra is
 * taken back evenly along the row, so the first cell sits flush with the start and the last with
 * the end; neighbours overlap. An item spanning cells runs from its first cell's start to its
 * last cell's end. Item layout, drop outlines, hit-testing and the resize frame all use these
 * functions, so they cannot disagree. With `drawn <= cell` this is exactly upstream's geometry.
 */
object OverlapGrid {

    /** Offset of cell [index] from the start of the grid. */
    @JvmStatic
    fun start(index: Int, cell: Int, border: Int, drawn: Int, count: Int): Int {
        val overlap = (drawn - cell).coerceAtLeast(0)
        return index * (cell + border) - if (count > 1) overlap * index / (count - 1) else 0
    }

    /** Length of an item spanning [span] cells from [index]. */
    @JvmStatic
    fun length(index: Int, span: Int, cell: Int, border: Int, drawn: Int, count: Int): Int {
        val last = start(index + span - 1, cell, border, drawn, count)
        return last - start(index, cell, border, drawn, count) + maxOf(cell, drawn)
    }

    /** Average distance from one cell's start to the next. */
    @JvmStatic
    fun step(cell: Int, border: Int, drawn: Int, count: Int): Float {
        val overlap = (drawn - cell).coerceAtLeast(0)
        return cell + border - if (count > 1) overlap / (count - 1f) else 0f
    }
}
