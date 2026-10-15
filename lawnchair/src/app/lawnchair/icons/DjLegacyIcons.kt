package app.lawnchair.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import app.lawnchair.icons.ExtendedBitmapDrawable.Companion.isFromIconPack
import com.android.launcher3.icons.BaseIconFactory.DEFAULT_WRAPPER_BACKGROUND
import com.android.launcher3.icons.BaseIconFactory.LEGACY_ICON_SCALE
import com.android.launcher3.icons.IconNormalizer
import com.android.launcher3.icons.IconProvider

/**
 * DJ(icon-tweaks): wraps a legacy (non-adaptive) icon the way upstream's
 * `BaseIconFactory.normalizeAndWrapToAdaptiveIcon` does, with two changes:
 * - an icon that fills its square ([isFullBleed]) is drawn at [FULL_BLEED_SCALE] instead of
 *   upstream's small legacy scale, so it fills the shape instead of sitting on a tile;
 * - the tile is tinted from the icon's light vibrant colour instead of its dominant colour.
 *
 * Returns null when upstream's path should handle the icon (adaptive, icon pack, wrapping off).
 */
fun wrapLegacyIcon(context: Context, iconBitmapSize: Int, icon: Drawable, outScale: FloatArray): Drawable? {
    if (icon is AdaptiveIconDrawable || !IconProvider.ATLEAST_OREO) return null
    if (icon.isFromIconPack || !shouldWrapAdaptive(context)) return null

    val bitmap = softwareBitmap(drawableToBitmap(icon))
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val fullBleed = isFullBleed(bitmap.width, bitmap.height) { x, y -> Color.alpha(pixels[y * bitmap.width + x]) }

    val normalizer = IconNormalizer(iconBitmapSize)
    val scale = normalizer.getScale(icon)
    val foreground = FixedScaleDrawable().apply {
        drawable = icon
        // setScale multiplies by LEGACY_ICON_SCALE; full-bleed icons get FULL_BLEED_SCALE instead.
        setScale(if (fullBleed) scale * FULL_BLEED_SCALE / LEGACY_ICON_SCALE else scale)
    }
    val wrapper = CustomAdaptiveIconDrawable(ColorDrawable(legacyTileColor(context, bitmap)), foreground)
    outScale[0] = normalizer.getScale(wrapper)
    return wrapper
}

/** Upstream's `getWrapperBackgroundColor`, with light vibrant in place of dominant. */
private fun legacyTileColor(context: Context, bitmap: Bitmap): Int {
    if (context.shouldTransparentBGIcons()) return Color.TRANSPARENT
    val color = Palette.from(bitmap).generate().getLightVibrantColor(DEFAULT_WRAPPER_BACKGROUND)
    if (color == DEFAULT_WRAPPER_BACKGROUND) return color
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color, hsl)
    hsl[2] = context.prefs.getFloat("pref_coloredBackgroundLightness", 1f)
    return ColorUtils.HSLToColor(hsl)
}

private fun softwareBitmap(bitmap: Bitmap): Bitmap = if (bitmap.config == Bitmap.Config.HARDWARE) bitmap.copy(Bitmap.Config.ARGB_8888, false) else bitmap
