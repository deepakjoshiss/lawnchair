# Roadmap

## Goal

Rebuild every DJ feature on upstream Lawnchair `16-dev` (branch `dj-dev-16`), improving each
one on the way, so the fork can follow upstream with little friction from then on.

"Improving" means, in priority order:

1. **Drop it** if upstream 16 now does the same thing (record the decision).
2. **Fewer upstream edits:** logic in `lawnchair/`, one-line hooks into AOSP code (RULES.md).
3. **Fix what was rough** in the 15 version, only where the spec names it.

## Method

Per feature, via `/port-feature <id>`: spec → overlap check against 16 → implement → gate →
emulator check → status here. One feature at a time, smallest risk first.

Why not cherry-pick: between our 15 base (`ccb2f7b83b`) and upstream 16 the files we touch were
largely rewritten, and two are gone entirely (`GridSizeMigrationUtil.java`,
`LauncherPreviewRenderer.java`). See DECISIONS #2.

## Features

Inventory from `dj-dev-15` (commits since 2024-08 plus uncommitted edits). The grouping and the
file lists were inferred from commit messages and touched files; each spec confirms its own.

Status: `todo` → `spec` (spec written) → `wip` → `done` (acceptance passed on emulator) ·
`dropped` (upstream covers it, see DECISIONS) · `obsolete`. · `parked` (owner: do last, after every other row).

| # | id | What it does | Main files on 15 | Status |
|---|---|---|---|---|
| 1 | `gesture-open-settings` | Gesture action that opens Lawnchair settings | `gestures/config/*`, `gestures/handlers/GestureHandler.kt` | done |
| 2 | `smartspace-style` | At a Glance font, colour and opacity settings; card tweaks | `smartspace/*`, `SmartspacePreferences.kt` | done |
| 3 | `feed-bridge` | Changes to the Google feed connection (purpose to confirm) | `FeedBridge.kt`, `BaseClientService.java` | parked |
| 4 | `depth-blur` | Background depth/blur animation fix | `DepthController.java`, `BaseDepthController.java` | dropped |
| 5 | `drawer-look` | Drawer background colour and animation, row height, divider, search bar layout, bundled `sans_regular` font. Includes the uncommitted `search_input_fg.xml`, `FontManager.kt`, `DrawableTokens.kt` edits | `allapps/*`, `AllAppsSearchInput.kt`, `res/layout/all_apps_divider.xml` | done |
| 6 | `drawer-frequent` | Frequent apps in the drawer, plus the short accent divider shown with them (moved from drawer-look) | `AlphabeticalAppsList.java`, `AppInfo.java`, `LoaderTask.java` | done |
| 7 | `icon-tweaks` | Icon scale, shadow and clock icon changes. **Uncommitted, inside the submodule** on 15 | `platform_frameworks_libs_systemui/iconloaderlib/*` | todo |
| 8 | `folder-look` | Folder rows, stacked folder icon, footer toggle, open/close animation, icon shape fix | `folder/*`, `FolderPreferences.kt` | todo |
| 9 | `home-grid` | Grid size changes, extra bottom padding, widget resize and padding fixes | `CellLayout.java`, `DeviceProfile.java`, `ShortcutAndWidgetContainer.java`, `CellLayoutLayoutParams.java` | todo |
| 10 | `widget-host` | Widget host view: transparent calendar widget, calendar widget by id | `NavigableAppWidgetHostView.java` | todo |
| 11 | `wallpaper-shift` | Steady wallpaper centring and shift between pages | `WallpaperOffsetInterpolator.java` | todo |
| 12 | `dock-rows` | Dock rows and horizontal spacing; dock migration fix | `Hotseat.java`, `HotseatMode.kt`, `DockPreferences.kt`, `GridSizeMigrationUtil.java` (gone on 16) | todo |
| 13 | `dock-poweramp` | Poweramp music widget docked in the hotseat | `PowerampWidgetContainerView.kt`, `PowerampWidgetHostView.kt` | todo |
| 14 | `android16-fix` | 15-era fix for running on Android 16 (`cf19020393`); likely obsolete on a 16 base | to confirm | todo |
| 15 | `home-text-shadow` | Lighter soft shadow on home-screen text (`workspaceAmbientShadowColor` `#40000000` → `#10000000`, light-text theme only). Decide from before/after screenshots | `res/values/styles.xml` (`e5ed825962`) | todo |

Order is the table order: Lawnchair-layer features first, AOSP-heavy ones later, the Poweramp
dock last. Reorder here, not in STATE.
