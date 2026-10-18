package app.lawnchair.ui.preferences.components.search

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.lawnchair.allapps.DrawerSearchGlass
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import com.android.launcher3.R

/**
 * DJ(drawer-glass): glass settings for the full-screen drawer's search bar. Hidden in sheet mode,
 * which keeps upstream's pill. "Glass style" needs Android 13+ (AGSL; below that the bar stays
 * Frost), "Scroll apps under search bar" needs Android 12+ (RenderEffect blur).
 */
@Composable
fun DrawerGlassPreference() {
    val prefs2 = preferenceManager2()
    if (prefs2.drawerSheet.getAdapter().state.value) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ListPreference(
            adapter = prefs2.drawerSearchGlass.getAdapter(),
            entries = listOf(
                ListPreferenceEntry(DrawerSearchGlass.FROST) { stringResource(R.string.dj_drawer_glass_frost) },
                ListPreferenceEntry(DrawerSearchGlass.LIQUID) { stringResource(R.string.dj_drawer_glass_liquid) },
            ),
            label = stringResource(R.string.dj_drawer_glass_style),
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SwitchPreference(
            adapter = prefs2.drawerSearchScrollUnder.getAdapter(),
            label = stringResource(R.string.dj_drawer_scroll_under),
            description = stringResource(R.string.dj_drawer_scroll_under_description),
        )
    }
}
