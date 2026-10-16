package app.lawnchair.wallpaper

import kotlin.math.roundToInt

/**
 * DJ(wallpaper-shift): where the wallpaper sits on the first page, and a steady scroll from there.
 *
 * `shift` (0..1) puts the first page at that point of the wallpaper (0 = left edge, 0.5 = centred,
 * 1 = right edge); later pages move on over the remaining part only. In RTL the first page is at
 * `1 - shift` and pages move towards the left edge. Upstream's own pinned positions (scrolling
 * off, one page, locked to the default page) map onto the same first-page position, so the
 * wallpaper doesn't jump between them. `shift = 0` is exactly upstream.
 */
object WallpaperShift {

    /** Remaps upstream's offset (0..1; RTL counts down from 1) for the given shift. */
    @JvmStatic
    fun shifted(offset: Float, shift: Float, rtl: Boolean): Float {
        val s = shift.coerceIn(0f, 1f)
        return if (rtl) (1f - s) * offset else s + (1f - s) * offset
    }

    /** Applies [shifted] to an upstream int ratio `out[0] / out[1]`, in place. */
    @JvmStatic
    fun applyTo(out: IntArray, shift: Float, rtl: Boolean) {
        val offset = if (out[1] == 0) 0f else out[0].toFloat() / out[1]
        out[1] = RESOLUTION
        out[0] = (shifted(offset, shift, rtl) * RESOLUTION).roundToInt()
    }

    private const val RESOLUTION = 10_000
}
