package app.lawnchair.folder

import android.content.Context
import androidx.core.graphics.ColorUtils
import app.lawnchair.LawnchairApp
import app.lawnchair.preferences2.PreferenceManager2
import app.lawnchair.preferences2.firstCached
import app.lawnchair.theme.color.tokens.ColorTokens
import app.lawnchair.util.resolveFolderBackgroundColor

// DJ(folder-look): settings read by the folder code (Folder, FolderPagedView, PreviewItemManager).

/** "Show footer": the open folder's name and page dots. */
fun showFolderFooter(context: Context): Boolean = PreferenceManager2.getInstance(context).showFolderFooter.firstCached()

/** "Stack icon": the home folder icon shows the first apps as overlapping cards. */
fun isFolderStackIcon(context: Context): Boolean = PreferenceManager2.getInstance(context).folderStackIcon.firstCached()

/** [isFolderStackIcon] for folder code that has no Context (layout rule, grid organizer). */
fun isFolderStackIcon(): Boolean = isFolderStackIcon(LawnchairApp.instance)

/**
 * Label colour inside an open home-screen folder: the chosen colour, or automatically light on a
 * dark folder background and dark on a light one (luminance threshold 0.5, as on 15).
 */
fun folderLabelColor(context: Context): Int {
    val chosen = PreferenceManager2.getInstance(context).folderLabelColor.firstCached()
        .colorPreferenceEntry.lightColor(context)
    if (chosen != 0) return chosen
    val background = resolveFolderBackgroundColor(context)
    return if (ColorUtils.calculateLuminance(background) < 0.5) {
        ColorTokens.Neutral1_50.resolveColor(context)
    } else {
        ColorTokens.Neutral1_900.resolveColor(context)
    }
}
