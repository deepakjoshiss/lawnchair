# Decisions

Short ADR log. **Append-only**; never renumber, never delete. Each entry: decision, why, and what
it costs. A reversed decision gets a *new* entry saying so. Cite from code as `DECISIONS #N`.

Next entry: **#15**.


## 1. Base is upstream `16-dev`

**Decided (2026-10-14):** our features move onto LawnchairLauncher/lawnchair `16-dev` (upstream's
default branch, active daily) on branch `dj-dev-16`. `15-dev` and `14-dev` are left behind.

**Why:** 15-dev is no longer where upstream works; staying there means porting their fixes by
hand forever.

**Cost:** a one-time re-implementation of every feature (ROADMAP).

## 2. Port by spec, not by cherry-pick

**Decided (2026-10-14):** each feature is described in `.ai/features/<id>.md` from the 15
implementation, then written fresh against 16. No `git cherry-pick` or rebase of `dj-dev-15`.

**Why:** measured 2026-10-14: between our 15 base `ccb2f7b83b` and upstream `16-dev`
`85335d904a`, upstream rewrote most files we touch (e.g. `DeviceProfile.java` 1,740 changed lines,
`Workspace.java` 1,352) and deleted two (`GridSizeMigrationUtil.java`,
`LauncherPreviewRenderer.java`). Replayed hunks would land in code that no longer means the same
thing.

**Cost:** slower per feature than a cherry-pick that happens to apply; offset by fewer upstream
edits afterwards.

## 3. Two checkouts: `dj-dev-16` here, `dj-dev-15` as a read-only worktree

**Decided (2026-10-14):** this folder is `dj-dev-16`. `dj-dev-15` lives in the worktree
`~/work/apps/lawnchair-15-ref`, with its 8 uncommitted edits (3 app files, 5 in the systemui
submodule) moved there intact, verified byte-identical against patches taken before the move.

**Why:** the uncommitted edits are part of the old features and the owner wants them kept as-is;
the main folder is where the future work is.

**Cost:** two working trees share one `.git`; a branch can only be checked out in one of them.
The pre-move stashes (`move-to-15-ref`, in this repo and in the submodule's git dir) were kept as a
spare copy.

## 4. `upstream` remote is fetch-only, `16-dev` only, no tags

**Decided (2026-10-14):** `upstream` = `https://github.com/LawnchairLauncher/lawnchair.git`, push
URL `DISABLED-read-only-remote`, refspec limited to `16-dev`, `tagOpt --no-tags`. The fork's own
`origin/16-dev` mirror is not used as a base (it was 97 commits behind on 2026-10-14).

**Why:** reading upstream must never become publishing to it; syncing the mirror would be a push.

**Cost:** none.

## 5. Settings keys: same effect keeps the same name

**Decided (2026-10-14, owner):** a setting ported from 15 keeps its 15 key name when it has the
same effect on 16. If its meaning, type or range changes, it gets a new name.

**Why:** an in-place update from the 15 build keeps the owner's settings wherever that is safe;
a changed meaning under an old name would read stale values as the wrong thing.

**Cost:** some 15 names stay that don't follow the `dj_` prefix (e.g. `pref_hotseat_rows`).

## 6. Test emulator is `VinPix_Dev`

**Decided (2026-10-14, owner):** Lawnchair is tested on the existing `VinPix_Dev` emulator.
VinPix pauses its use of it meanwhile.

**Why:** no second emulator to create or maintain.

**Cost:** a Lawnchair debug build installed there can take over the home screen; if VinPix
resumes, set its home app back or split the emulators.

## 7. At a Glance layout compaction from 15 is not ported

**Decided (2026-10-14, owner):** the 15 tweaks to At a Glance's size and spacing (height 104dp →
92dp, page indicator padding 8dp → 4dp, fixed 16dp left padding, widget side padding; commit
`e5ed825962`) are dropped. 16's own sizes stand.

**Why:** upstream reworked this area after our base (new page indicator, new dimensions,
margins matched to its reference design: `2bd3a2a76d`, `e1cdc78434`, `33432f46ab`).

**Cost:** At a Glance looks as upstream draws it; revisit only from a screenshot.

## 8. The gate compiles Kotlin non-incrementally

**Decided (2026-10-14):** `.ai/bin/check` builds with `-Pkotlin.incremental=false`.

**Why:** an incremental build left `BasePreferenceManager`'s nested pref classes without the
Compose `$stable` field while other classes referenced it; the gate passed and the APK crashed at
start. A forced full recompile fixed it (all six pref classes then had `$stable`, checked with
`dexdump`).

**Cost:** about 35 s more per check (full check measured at 1 min 14 s).

## 9. depth-blur: 16's own wallpaper zoom and blur, nothing from 15

**Decided (2026-10-15, owner):** none of `be82c989c6`'s depth changes are ported. 16's zoom
(`setWallpaperZoomOut(depth)`) and blur stay as upstream ships them.

**Why:** 16 already blurs harder than 15's tweak (30dp ≈ 79 px at 420 dpi vs 60 px, full at 30 %
depth), fixed the Android 16 early-wakeup crash itself (`6fd747790d`), and the owner prefers 16's
zoom to 15's ×1.75-capped-at-0.525 curve. Capping depth would also have touched the value 16's
stuck-blur fixes read.

**Cost:** none.

## 10. drawer-look: only the finer sliders are ported

**Decided (2026-10-15, owner):** from 15's drawer changes, only the finer slider steps are
ported. Not ported: the 15 search bar look (translucent pill, outline, 18sp text, spacing,
always-visible background; partly uncommitted on 15), the `hideWorkspaceLabelsIfNotEnoughSpace`
disable, and `sans_regular.ttf`.

**Why:** colour, opacity, header band behaviour (setting `all_apps_search_bar_background`) and the
colour-animation fix are already in 16; 16 rebuilt the drawer search bar in Compose and the owner
keeps it; the label-hiding rule only affects multi-window/vertical-bar layouts and its original
problem is unknown; the font file is unreferenced.

**Cost:** the drawer search bar looks like upstream's, not like 15.

## 11. drawer-frequent: extra rows on 16's Suggestions, not the 15 frequent-apps block

**Decided (2026-10-15, owner):** the 15 frequent-apps block (usage stats, hidden Parcel offset,
own divider) is not ported. Instead 16's Suggestions row gains up to 2 extra full rows for apps
launched at least half as often as row 1's least-launched app (and ≥ 3 times), shown inside the
suggestions area.

**Why:** 16 already has a suggestions row (on by default, per-launch updates, public APIs,
blocked-apps list). The owner wants more than one row only for very frequently used apps, and
the extra rows are suggestions, so they belong in that area.

**Cost:** one upstream file edited (`PredictionRowView.java`, grid layout when rows > 1); the
taskbar all-apps and secondary-display drawers share it and get the extra rows too. The 15
accent divider is gone (16 uses its "All apps" label).

## 12. No edits inside the systemui submodule; icon tweaks are overrides

**Decided (2026-10-15, owner):** `platform_frameworks_libs_systemui` stays exactly at upstream's
recorded commit. The 15 icon tweaks are re-done from the main repo: `LauncherIcons.kt` overrides
`normalizeAndWrapToAdaptiveIcon` and `getShadowGenerator`, with the logic in `lawnchair/src`.
The clock-shape tweak is not ported (16 already shapes the clock background).

**Why:** the submodule is upstream's repo; carrying edits there needs our own fork of it and a
merge on every upstream bump. Overrides keep the submodule untouched.

**Cost:** two upstream methods are duplicated in our code (`ShadowGenerator.drawShadow` /
`addPathShadow`, the legacy branch of `normalizeAndWrapToAdaptiveIcon`) and must be re-checked
when upstream changes them.

## 13. folder-look: stack icon with app 1 in front; footer toggle; auto/custom label colour

**Decided (2026-10-15, owner):** port the stack folder icon (default on, 15 key), with **app 1 as
the front card** (15 put app 3 in front) and the open/close animation kept in stack order; port
the footer toggle and 15's folder spacing; label colour gets auto (contrast) plus custom colours.
Not ported: 15's folder colour plumbing and folder-icon shape (covered by 16's settings) and its
animation tweaks (16 uses the spring animation).

**Why:** owner's look. App 1 in front reads naturally; the animation fix follows from it because
open-folder views draw in rank order, opposite to the stack.

**Cost:** edits in 7 upstream folder files (layout rule, organizer, preview manager, icon, paged
view, folder, resources); the layout rule and organizer read the setting through
`LawnchairApp.instance`.

## 14. home-grid: overlapping cells on a fine grid, not a subgrid

**Decided (2026-10-16, owner):** for free placement and fine widget resizing, port 15's
overlapping-cells approach (icons drawn full size on a fine grid, e.g. the owner's 15 × 22), with
its bugs fixed, plus the larger grid limit, always-scale/never-hide-labels, the bottom padding
slider, the tighter widget resize frame and finer icon/label sliders. Compact spacing is decided
after the grid works.

**Why:** it is how the owner's 15 setup already works; positions stay whole cells, so the 15
layout carries over with no conversion; about half the work of a subgrid (icons spanning 2×2/4×4
of a finer grid, Nova-style), which stays an option for later.

**Cost:** icons placed in adjacent small cells overlap visually (as on 15); edits to the core
grid placement code (`CellLayout`, `CellLayoutLayoutParams`, `ShortcutAndWidgetContainer`,
`DeviceProfile`).

## 15. home-grid as built: widgets overlap like icons, report their drawn size; 32 dp grab area

**Decided (2026-10-16, owner):** on the home screen every item (icons, folders, widgets, At a
Glance) uses one geometry (`app.lawnchair.util.OverlapGrid`): cells drawn at least icon + label
size, overlap spread evenly, widgets in both directions (15: width only). Widgets are told the
size they are drawn at, and their default/min/max spans are worked out on it (`WidgetSizes`,
`LauncherAppWidgetProviderInfo`); 15 got this from flooring `getCellSize()` globally. Phone grids
never shrink to fit. The resize-frame grab area is 4 × 8 dp = 32 dp, smaller than upstream's
48 dp; kept. Bottom padding is added to the workspace bottom padding directly (15 went through the
page-indicator overlap). Not carried: 15's `DeviceGridState` char encoding (16 parses "15,22"
with `split`), its red debug grid.

**Why:** one formula means the item, its drop outline, hit-testing and the resize frame cannot
disagree, which is where 15's bugs came from; widgets told their slot size (69 × 74 px per cell
at 15 × 22) picked their smallest layouts.

**Cost:** 9 upstream files touched (`CellLayout`, `CellLayoutLayoutParams`,
`ShortcutAndWidgetContainer`, `DeviceProfile`, `AppWidgetResizeFrame`, `WidgetSizes`,
`LauncherAppWidgetProviderInfo`, `dimens.xml`, `app_widget_resize_frame.xml`), all marked
`DJ(home-grid)`.

## 16. Long-press edit mode: veil bar over the page, centre-pinned shrink, edge gutter; dock clamp

**Decided (2026-10-16, owner):** the drop-target bar becomes a full-width veil from the top of
the screen (gradient, pinned dark with pinned light ink, no button outlines) whose bottom 8dp lies
over the page and counts as drop area; the page shrinks as little as the screen allows (0.961 on
the emulator), around its own centre, with a 1px page gap so a sliver of the next page shows and
a 24dp edge gutter for switching pages. The dock's side padding is clamped at 0 on dense grids.

**Why:** on a fine grid, upstream's 0.74 shrink pinned under a tall bar shifted items by more
than a cell between press and drop; the owner rejected an anchored drop (outline would lag the
finger) in favour of less zoom. With almost no shrink, the neighbour page is a sliver, so page
switching needs the gutter. 60% black is the floor for ~4.9:1 light-accent text over white.

**Cost:** one-line hooks in upstream `DropTargetBar` (3), `ButtonDropTarget`, `SpringLoadedState`,
`Workspace` (page gap, gutter), `DeviceProfile` (dock clamp), `drop_target_bar.xml`; the rest is
flavor resources. Top/bottom rows can still shift ~30px (under half a row).

## 17. home-grid E: home-only 4dp cell padding; page indicator via 16's setting

**Decided (2026-10-16, owner):** home icon cells get 4dp side padding (15's value) through a new
`dj_home_cell_padding_x`, leaving upstream's `dynamic_grid_cell_padding_x` (also used by drawer
folder icons) at 8dp. 15's 24→16dp page indicator is not ported: 16's Dock → "Page indicator
height" slider covers it. The compact drop-target bar is DECISIONS #16.

**Why:** owner's look, kept although the gain at 15 × 22 is small (labels 140→161px in a 182px
drawn cell); a setting already exists for the page indicator.

**Cost:** one-line hook in `DeviceProfile`.

## 18. dock-rows: rows, pages and dock widgets from upstream 16; only horizontal spacing ported

**Decided (2026-10-16, owner):** upstream 16's dock rows (1–2), dock pages (1–5) and widgets on
the dock cover what the owner wants; only 15's "Horizontal spacing" is ported (dock side padding
= factor × 16dp, key `hotseat_horizontal_factor`). 15's dock migration fix is not ported: the file
it patched (`GridSizeMigrationUtil.java`) is gone on 16.

**Why:** owner tested the upstream dock and it behaves as wanted; the spacing slider is the one
15 control missing.

**Cost:** one hook in `Hotseat.setInsets`; the rest is Lawnchair layer. At 100 % the dock is
inset 16dp regardless of the home horizontal padding (upstream derives it from the home grid).

## 19. dock-poweramp dropped: the dock takes any widget

**Decided (2026-10-16, owner):** 15's Poweramp music widget docked in the hotseat (a special
`HotseatMode` container with its own host views) is not ported.

**Why:** on 16 the dock accepts any widget (upstream `6d084bfa43`, DECISIONS #18), so the
Poweramp widget can simply be placed in the dock.

**Cost:** none; the 15 code stays in the reference checkout.

## 20. wallpaper-shift: 15's formula, mirrored in RTL, applied live

**Decided (2026-10-16, owner):** port 15's "Wallpaper center shift" (`offset = s + (1 − s) ×
upstream`, pinned cases at `s`; key `dj_pref_wallpaperShift` in the SharedPreferences store) with
two changes: RTL mirrors (`(1 − s) × upstream`, so the first page is at `1 − s` both scrolling and
pinned; 15 jumped between the two), and a slider change re-syncs the wallpaper at once. Upstream's
offset handler now starts at −1 instead of 0.5, else a 50 % first offset was never sent.

**Why:** owner's look from 15; the RTL jump looked unintended; the handler start value assumed
the first real offset could never be 0.5.

**Cost:** two one-line call-site hooks and the handler start value in
`WallpaperOffsetInterpolator`; the formula lives in `app.lawnchair.wallpaper.WallpaperShift`.
