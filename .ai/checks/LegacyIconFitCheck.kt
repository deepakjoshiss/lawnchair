// Standalone check for lawnchair/src/app/lawnchair/icons/LegacyIconFit.kt, run by .ai/bin/check.
import app.lawnchair.icons.isFullBleed
import app.lawnchair.icons.transparentMarginSum

/** A size×size icon that is opaque inside [x0, x1) × [y0, y1). */
fun box(size: Int, x0: Int, y0: Int, x1: Int, y1: Int): (Int, Int) -> Int =
    { x, y -> if (x in x0 until x1 && y in y0 until y1) 255 else 0 }

fun main() {
    check(isFullBleed(192, 192, box(192, 0, 0, 192, 192))) { "fully opaque square is full-bleed" }
    check(isFullBleed(192, 192, box(192, 2, 2, 190, 190))) { "2px margins (sum ~0.04) still full-bleed" }
    check(!isFullBleed(192, 192, box(192, 48, 48, 144, 144))) { "red dummy (48px margins) is padded" }
    check(!isFullBleed(192, 192, box(192, 0, 0, 192, 160))) { "transparent bottom strip (1/6) is padded" }
    check(transparentMarginSum(192, 192, { _, _ -> 0 }) == 4f) { "fully transparent -> 4" }
    check(transparentMarginSum(0, 0, { _, _ -> 255 }) == 4f) { "empty bitmap -> 4" }
    val m = transparentMarginSum(100, 100, box(100, 10, 0, 100, 100))
    check(kotlin.math.abs(m - 0.1f) < 1e-6) { "10px left margin on 100px -> 0.1, got $m" }
    check(isFullBleed(192, 192, { x, y -> if (x == 0 && y == 0) 10 else 255 })) { "faint corner pixel ignored" }
    println("legacy icon fit: ok")
}
