package app.lawnchair.allapps

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
 * The sheet keeps upstream's pill, which already sits on its own background.
 */
object DrawerSearchGlass {

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

    private val SEARCH_ICON_SIZE = 18.dp // owner's choice; the pill's default is 24dp

    fun isActive(launcher: LawnchairLauncher) = !launcher.deviceProfile.shouldShowAllAppsOnSheet()

    fun hintColor(launcher: LawnchairLauncher) = HintColor.resolveColor(launcher)

    /** Frost: white tint over the window blur, thin white edge. Stays the same while typing. */
    fun frostStyle(launcher: LawnchairLauncher, themed: Boolean): QsbStyle {
        val blurBehind = launcher.isAllAppsBackgroundBlurEnabled &&
            PreferenceManager2.getInstance(launcher).wallpaperDepthEffect.firstCached()
        val dark = UiColorMode(Themes.getAttrInteger(launcher, R.attr.uiColorMode)).isDarkTheme
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
}
