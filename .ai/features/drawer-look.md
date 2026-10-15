# drawer-look: app drawer appearance

status: done (2026-10-15) — only H ported; owner kept 16's search bar (DECISIONS #10)

## On 15 (reference) and verdict on 16

Sub-changes from the scout's read of `dj-dev-15` (incl. uncommitted edits), each checked against
16 on 2026-10-15.

| | 15 change (commit) | On 16 | Verdict |
|---|---|---|---|
| A | Drawer background colour + opacity; ended up on upstream's `app_drawer_bg_color` and `pref_drawerOpacity` (`3fac2fc31f`, `be82c989c6`) | Both settings exist; same keys, so a 15 value carries over. 16's opacity default is 0.5 (15: 1.0). | covered |
| B | Header band behind the search bar fades with the search bar, not the drawer opacity (`bd53909ff6`) | Setting `all_apps_search_bar_background` (default on): off gives exactly 15's behaviour, on uses drawer opacity | covered by setting |
| C | Colour animation: compare colours before alpha in `drawOnScrimWithScaleAndBottomOffset` (`c6dcab5af1`) | 16 compares `headerWithoutAlpha == backgroundWithoutAlpha`; same fix upstream | covered |
| D | Search pill: dark translucent `#77111111`, thin white outline `#aaffffff`, always visible (no scroll fade); part uncommitted (`be82c989c6` + working tree) | `search_input_fg.xml` is gone; the drawer search bar is Compose now (`44db7c1fd2`) with optional blur (`9382280d9a`) | **owner** |
| E | Search layout: hint/input margin 28→32dp, text 20→18sp, icon 56dp + 17dp padding (`bd53909ff6`) | layout reworked, spacing retuned upstream (`b0adb1d918`) | **owner** (with D) |
| F | Search padding from `dynamic_grid_hotseat_side_padding`; hint set at inflate (`3fac2fc31f`, `bd53909ff6`) | padding now computed in `setupPadding()` | **owner** (with D) |
| G | Short centred accent divider (120dp, alpha 0.4) between sections (`e89c4d3f8f`) | only shows with frequent apps | moves to `drawer-frequent` |
| H | Finer sliders: opacity 0.1→0.025, row height 0.1→0.025, indent 0.05→0.01, icon size 0.1→0.05 (`3fac2fc31f`, `e89c4d3f8f`, `1b282a69cf`) | still the coarse steps | **port** (Lawnchair layer only) |
| — | `DeviceProfile.hideWorkspaceLabelsIfNotEnoughSpace()` body commented out (`c2a8d30c16`, "Drawer row height fix") | logic still active; runs only in multi-window and vertical-bar layouts | drop unless the problem is remembered |
| — | `lawnchair/res/font/sans_regular.ttf` (`a1909995c4`) | referenced nowhere on 15 | drop |
| — | Uncommitted `FontManager.setCustomFont` `GlobalScope` fallback | `setCustomFont` rewritten on 16 (no lifecycle scope); custom fonts verified working in smartspace-style | covered |

## Evidence

- 16's drawer on the emulator: `.ai/shots/40-drawer-16.png` (default settings, 50 % opacity,
  search bar background on).

## Proposal

Port H only. Record A–C and the font items as covered, G under `drawer-frequent`, the row-height
and font-file items as dropped. D–F: keep 16's Compose search bar unless the owner wants 15's
look (outline, darker translucent pill, smaller text) recreated on it.

## Result (2026-10-15)

- Owner: port H only; D–F (15 search bar look) not ported, 16's Compose search bar stays.
- H ported in `AppDrawerPreferences.kt` (4 slider steps, `DJ(drawer-look)`), Lawnchair layer only.
- Verified on `VinPix_Dev`: Background opacity taps at 47.5 % and ~25 % of the track read
  **48 %** and **23 %** (0.475 / 0.225 rounded for display); with the old 0.1 step they would snap
  to 50 % / 20 %. `41-drawer-opacity-step.png`. Row height, indent and icon size use the same
  control; their new steps are checked in code only.
