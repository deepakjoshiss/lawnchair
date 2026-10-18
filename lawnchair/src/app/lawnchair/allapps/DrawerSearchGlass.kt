package app.lawnchair.allapps

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.lawnchair.LawnchairLauncher
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached
import app.lawnchair.qsb.QsbStyle
import app.lawnchair.qsb.buildQsbStyle
import app.lawnchair.theme.UiColorMode
import app.lawnchair.theme.color.tokens.DayNightColorToken
import app.lawnchair.theme.color.tokens.StaticColorToken
import app.lawnchair.theme.color.tokens.setAlpha
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.Themes

/**
 * DJ(drawer-glass): glass looks for the app drawer search bar, full-screen drawer only.
 * The sheet keeps upstream's pill, which already sits on its own background. DECISIONS #29.
 */
object DrawerSearchGlass {

    const val FROST = "frost"
    const val LIQUID = "liquid"

    // White frost on dark themes; on light themes a white tint vanishes into the blurred wallpaper
    // (measured 240 vs 238), so it flips to a dark tint and edge.
    private val White = StaticColorToken(0xFFFFFFFF)
    private val Black = StaticColorToken(0xFF000000)
    private val FrostTint = DayNightColorToken(lightToken = Black, darkToken = White)
    private val FrostEdge = DayNightColorToken(lightToken = Black.setAlpha(.12f), darkToken = White.setAlpha(.24f))

    // The theme's textColorTertiary can be dark in a dark-mode drawer (bright wallpaper → dark
    // workspace text): measured 130 on a 132 pill. The glass pill sets its own.
    private val HintColor = DayNightColorToken(lightToken = Black.setAlpha(.6f), darkToken = White.setAlpha(.8f))

    // Tint strength in percent (LawnQsbUi replaces the colour's own alpha with this). Without a
    // blurred wallpaper behind it the pill needs about double the tint to stay readable.
    private const val FROST_ALPHA_DARK = 14
    private const val FROST_ALPHA_LIGHT = 6

    // Liquid glass: a faint white tint; the edges come from the shader (LiquidGlass.kt).
    private const val LIQUID_ALPHA_DARK = 6
    private const val LIQUID_ALPHA_LIGHT = 30

    private val SEARCH_ICON_SIZE = 18.dp // owner's choice; the pill's default is 24dp

    fun isActive(launcher: LawnchairLauncher) = !launcher.deviceProfile.shouldShowAllAppsOnSheet()

    fun hintColor(launcher: LawnchairLauncher) = HintColor.resolveColor(launcher)

    /** Liquid glass needs AGSL (Android 13+); below that, or when not chosen, Frost. */
    private fun isLiquid(launcher: LawnchairLauncher) = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        PreferenceManager2.getInstance(launcher).drawerSearchGlass.firstCached() == LIQUID

    private fun isDark(launcher: LawnchairLauncher) = UiColorMode(Themes.getAttrInteger(launcher, R.attr.uiColorMode)).isDarkTheme

    fun qsbStyle(launcher: LawnchairLauncher, themed: Boolean): QsbStyle {
        val dark = isDark(launcher)
        if (isLiquid(launcher)) {
            return buildQsbStyle(
                context = launcher,
                themed = themed,
                backgroundAlpha = if (dark) LIQUID_ALPHA_DARK else LIQUID_ALPHA_LIGHT,
                backgroundColor = White.resolveColor(launcher),
                cornerRadius = 1f,
                strokeColor = null,
                strokeWidth = 0f,
            ).copy(startIconSize = SEARCH_ICON_SIZE)
        }
        return frostStyle(launcher, themed, dark)
    }

    /** Frost: tint over the window blur, thin edge. Stays the same while typing. */
    private fun frostStyle(launcher: LawnchairLauncher, themed: Boolean, dark: Boolean): QsbStyle {
        val blurBehind = launcher.isAllAppsBackgroundBlurEnabled &&
            PreferenceManager2.getInstance(launcher).wallpaperDepthEffect.firstCached()
        val alpha = (if (dark) FROST_ALPHA_DARK else FROST_ALPHA_LIGHT) * (if (blurBehind) 1 else 2)
        return buildQsbStyle(
            context = launcher,
            themed = themed,
            backgroundAlpha = alpha,
            backgroundColor = FrostTint.resolveColor(launcher),
            cornerRadius = 1f,
            strokeColor = FrostEdge.resolveColor(launcher),
            strokeWidth = Utilities.dpToPx(1f).toFloat(),
        ).copy(startIconSize = SEARCH_ICON_SIZE)
    }

    /** Extra drawing on the pill: the Liquid glass shader, or nothing for Frost. */
    fun pillModifier(launcher: LawnchairLauncher, style: QsbStyle): Modifier {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !isActive(launcher) || !isLiquid(launcher)) return Modifier
        return if (isDark(launcher)) {
            Modifier.liquidGlass(style.cornerRadiusPx, highlight = .65f, shade = .25f, sheen = .22f)
        } else {
            Modifier.liquidGlass(style.cornerRadiusPx, highlight = .9f, shade = .22f, sheen = .35f)
        }
    }
}
