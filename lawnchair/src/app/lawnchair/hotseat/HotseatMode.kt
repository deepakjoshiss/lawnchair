package app.lawnchair.hotseat

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import app.lawnchair.util.isPackageInstalledAndEnabled
import com.android.launcher3.R

sealed class HotseatMode(
    @StringRes val nameResourceId: Int,
    @LayoutRes val layoutResourceId: Int,
) {
    companion object {
        fun fromString(value: String): HotseatMode = when (value) {
            "disabled" -> DisabledHotseat
            "google_search" -> GoogleSearchHotseat
            "poweramp_widget" -> PowerampHotseat
            else -> LawnchairHotseat
        }

        /**
         * @return The list of all hot seat modes.
         */
        fun values() = listOf(
            DisabledHotseat,
            LawnchairHotseat,
            GoogleSearchHotseat,
            PowerampHotseat,
        )
    }

    abstract fun isAvailable(context: Context): Boolean
}

object LawnchairHotseat : HotseatMode(
    nameResourceId = R.string.hotseat_mode_lawnchair,
    layoutResourceId = R.layout.search_container_hotseat,
) {
    override fun toString() = "lawnchair"
    override fun isAvailable(context: Context): Boolean = true
}

object GoogleSearchHotseat : HotseatMode(
    nameResourceId = R.string.hotseat_mode_google_search,
    layoutResourceId = R.layout.search_container_hotseat_google_search,
) {
    override fun toString(): String = "google_search"

    override fun isAvailable(context: Context): Boolean =
        context.packageManager.isPackageInstalledAndEnabled("com.google.android.googlequicksearchbox")
}

object PowerampHotseat : HotseatMode(
    nameResourceId = R.string.hotseat_mode_power_amp,
    layoutResourceId = R.layout.container_hotseat_poweramp,
) {
    override fun toString(): String = "poweramp_widget"

    override fun isAvailable(context: Context): Boolean =
        context.packageManager.isPackageInstalledAndEnabled("com.maxmpz.audioplayer")
}

object DisabledHotseat : HotseatMode(
    nameResourceId = R.string.hotseat_mode_disabled,
    layoutResourceId = R.layout.empty_view,
) {
    override fun toString(): String = "disabled"

    override fun isAvailable(context: Context): Boolean = true
}
