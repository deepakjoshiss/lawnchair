package app.lawnchair.icons

// DJ(icon-tweaks): measuring whether a legacy (non-adaptive) icon fills its square.
// Pure Kotlin so `.ai/checks/LegacyIconFitCheck.kt` can run it without Android.

/** Wrapped legacy icons whose margins add up to at most this are drawn at [FULL_BLEED_SCALE]. */
const val FULL_BLEED_MAX_MARGIN = 0.05f

/** Scale for full-bleed legacy icons inside the wrapper (upstream's legacy scale is about 0.47). */
const val FULL_BLEED_SCALE = 0.85f

/**
 * Sum of the four transparent margins around the visible pixels, each as a fraction of the icon's
 * width or height: 0 for an icon that fills its square, 4 for a fully transparent one.
 *
 * @param alphaAt alpha (0–255) of the pixel at (x, y).
 * @param minAlpha pixels below this alpha count as transparent.
 */
fun transparentMarginSum(width: Int, height: Int, alphaAt: (Int, Int) -> Int, minAlpha: Int = 40): Float {
    if (width <= 0 || height <= 0) return 4f
    var minX = width
    var minY = height
    var maxX = -1
    var maxY = -1
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (alphaAt(x, y) >= minAlpha) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    if (maxX < 0) return 4f
    return minX.toFloat() / width + (width - 1 - maxX).toFloat() / width +
        minY.toFloat() / height + (height - 1 - maxY).toFloat() / height
}

fun isFullBleed(width: Int, height: Int, alphaAt: (Int, Int) -> Int): Boolean = transparentMarginSum(width, height, alphaAt) <= FULL_BLEED_MAX_MARGIN
