package app.lawnchair.gestures.handlers

import android.content.Context
import android.content.Intent
import app.lawnchair.LawnchairLauncher

/** Opens Lawnchair's own settings at the top-level page (feature `gesture-open-settings`). */
class OpenSettingsGestureHandler(
    context: Context,
) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: LawnchairLauncher) {
        // setPackage: our PreferenceActivity, never the system's settings.
        // CLEAR_TASK: an already-open settings task restarts at the top level, not on a sub-page.
        val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
            .setPackage(launcher.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        launcher.startActivity(intent)
    }
}
