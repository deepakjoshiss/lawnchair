# gesture-open-settings: gesture action that opens Lawnchair settings

status: done (2026-10-14) — double tap itself awaits a by-hand check, see Acceptance results

## What the user sees

A new action, **"Open Lawnchair settings"**, at the end of every gesture action picker: the
Gestures screen (double tap, swipe up, swipe down, home press, back press), the double-tap picker
on the Home screen settings page, and the per-app icon swipe gestures. Doing a gesture set to it
opens Lawnchair's settings at the top-level page. If settings were already open on a sub-page,
they restart at the top level. No gesture uses it by default.

## Settings

No new key. It is a new value for the existing gesture settings.

| key | type | value | where |
|---|---|---|---|
| (existing gesture prefs, e.g. double tap) | `GestureHandlerConfig` JSON | `openSettings` (serial name) | Settings → Gestures; Home screen; icon swipe sheet |

The serial name stays `openSettings` (DECISIONS #5: same effect, same name), so a 15 install
updated in place keeps a gesture already set to this action.

## On 15 (reference)

- Commit `cdc148bdcb` (2025-09-17, "Fix Dock Migration, Added open Settings in Gestures"); its
  dock changes belong to `dock-rows`. Nothing else touched the feature. No uncommitted edits.
- Files, all in Lawnchair's own gesture framework (no AOSP edits):
  - `gestures/config/GestureHandlerConfig.kt`: `@SerialName("openSettings") data object OpenSettings : Simple(...)`
  - `gestures/config/GestureHandlerOption.kt`: `data object OpenSettings : Simple(GestureHandlerConfig.OpenSettings)`
  - `gestures/handlers/GestureHandler.kt`: `OpenSettingsGestureHandler.onTrigger` starts
    `Intent(ACTION_APPLICATION_PREFERENCES).setPackage(launcher.packageName)` with
    `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`.
  - `ui/preferences/components/GestureHandlerPreference.kt`: appended to the picker's list.
  - `res/values/strings.xml`: `gesture_handler_open_settings` = "Open Launcher Settings".
- Quirks: `setPackage` keeps the intent on Lawnchair's own settings (`PreferenceActivity`
  declares `APPLICATION_PREFERENCES`), never the system's. `CLEAR_TASK` is why settings restart at
  the top level.

## On 16

**Overlap: none.** Upstream 16 has no settings action (checked `GestureHandlerConfig.kt` serial
names and the gesture commit log, 2026-10-14). Full port.

What moved on 16, and the hook points chosen:

- The picker list moved to `gestures/config/GestureHandlerOptions.kt` (`gestureHandlerOptions`),
  shared by the gesture pickers and 16's new "Lawnchair actions" screen.
- `GestureHandlerConfig.Simple` now carries an icon (`iconRes`), shown in the picker. Default is
  the Lawnchair home icon; we set `R.drawable.ic_settings_monochrome` (the monochrome layer of
  Lawnchair's settings app icon).
- `isExternallyInvokable()` stays `false`, as for every action except drawer and search: a
  home-screen shortcut to "open settings" adds nothing over the settings app icon.
- `PreferenceActivity` still declares `APPLICATION_PREFERENCES` (`lawnchair/AndroidManifest.xml`).

All edits are in `lawnchair/` (Lawnchair's layer), so there are no `DJ()` markers in AOSP code.
The new lines in upstream's Lawnchair files still get `// DJ(gesture-open-settings)`.

## Acceptance

On `VinPix_Dev`, debug build from a green `.ai/bin/check`:

1. Settings → Gestures → Double tap: the picker lists "Open Lawnchair settings" last, with its
   icon. Evidence: screenshot.
2. Choose it, go home, double-tap empty space: Lawnchair settings open at the top level.
   Evidence: screenshot, plus `dumpsys activity` showing `PreferenceActivity` of
   `app.lawnchair.debug` on top.
3. Open settings → any sub-page, press Home, double-tap: settings open at the top level, not
   the sub-page. Evidence: screenshot.
4. The stored value contains `openSettings`. Evidence: the datastore file read with
   `run-as`, filtered through `strings`, matches `openSettings` (and before step 2, it does not).
5. Long-press an app icon → swipe gesture setting: the action is offered there too.
   Evidence: screenshot.

## Improvements

- Label: "Open Launcher Settings" → **"Open Lawnchair settings"**, matching 16's
  sentence-case labels ("Open app drawer", "Open notification panel"). String key unchanged.
- Icon in the picker (new on 16, required by its option layout).

## Acceptance results (2026-10-14, VinPix_Dev, API 37, build of `c219960f78` + this change)

1. Pass. Last item in the double-tap picker (`.ai/shots/01-picker.png`). That picker shows no
   icons, so the icon is only used where 16 draws action icons (home-screen action shortcuts).
2. Pass **with swipe down instead of double tap**: swipe down set to the action opened
   `PreferenceActivity`; logcat `START u0 {act=android.intent.action.APPLICATION_PREFERENCES
   flg=0x10008000 pkg=app.lawnchair.debug}`. A double tap could not be injected: `input tap` is
   too slow, `sendevent` is denied by SELinux on this user build, and the emulator console
   syntax failed. Gesture detection is upstream code we did not touch; the double tap still
   wants one by-hand check.
3. Pass: settings had been left on the Gestures page; the gesture reopened them at the top level
   (`03-settings-top-level.png`), task size 1, the old activity destroyed.
4. Pass: datastore had no `openSettings` before; after choosing the action it holds
   `{"type":"openSettings"}` (matches 15's serial name).
5. Pass via the same screen opened directly (`LawnchairShortcutActivity`, `04-actions-picker.png`);
   per-app icon gestures use that screen on 16. Not reached through an icon long-press.

Not explained: the first swipe-down after the home screen first loaded did not trigger anything;
the next one did.
