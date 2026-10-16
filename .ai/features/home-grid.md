# home-grid: home screen grid, spacing, bottom padding, widget sizing

status: done 2026-10-16 (DECISIONS #14, #15); E (compact spacing) still open

## On 15 (reference) and verdict on 16

From the scout's read of `dj-dev-15` (`5b7e3e6337`, `c768289c15`, `50d64f8d3d`, `1b282a69cf`,
`c2a8d30c16`; no uncommitted grid edits), checked against 16 on 2026-10-15. 16 added
"Nova-style padding controls" (`5de4bb09fa`): horizontal padding, vertical padding and widget
padding, each a 0–2× factor (default 1×) on the Home screen settings page.

| | 15 change | On 16 | Verdict |
|---|---|---|---|
| A | Cells never shrink below their natural icon+label size on dense grids; they overlap, spread evenly (first column flush left, last flush right). Big `CellLayout`/`CellLayoutLayoutParams`/`ShortcutAndWidgetContainer` rewrite; dock opts out | not in 16 | **owner** — largest, riskiest part; 15 version has bugs (padding counted twice, RTL drift, multi-span mismatch) |
| B | Larger grid limit: 29 (increased) / 24 (normal) instead of 20 / 10 | still 20 / 10 | port (one line, Lawnchair layer) |
| C | Always scale icons on scalable grids (removes the `shouldScale` gate); label-size factor feeds cell height; never hide labels on cramped grids | not in 16 | goes with A |
| D | "Bottom padding multiplier" slider (`dj_home_bottom_padding_multiplier`, int −100..100, default 1): extra/less space between the grid and the dock, ~1dp per step | 16's vertical padding scales top **and** bottom together | **owner** — try 16's vertical padding first; port D only if bottom-only control is needed |
| E | Dimens: side margin 8→0dp; cell x padding 8→4dp; page indicator 24→16dp; compact drag drop-target bar (56→28dp, gaps 20→1dp, margins → 4dp) | side margin covered by horizontal padding 0×; the rest unchanged | **owner** for cell padding, page indicator, drop-target bar |
| F | Widgets: min padding 8→0dp; tighter resize frame (handle margin 13→4dp, background padding 24→8dp, frame margin 23→8dp); bigger grab area (4× instead of 2× background padding) | widget padding covered by 16's widget padding factor (0× = none; 16's min is 6dp); resize frame unchanged | port the resize-frame look and grab area |
| — | Home icon size and label size sliders step 0.1 → 0.05 | still 0.1 | port |

## Proposal

- Port: B (grid limit), F's resize frame, finer icon/label sliders.
- Use 16's settings instead of: E side margin (horizontal padding 0×), F widget padding (widget
  padding 0×), D unless bottom-only is needed.
- Owner, best decided on the emulator with the owner's grid size: A + C (overlapping cells and
  never shrinking icons), E's cell padding / page indicator / drop-target bar.

## Owner context and decisions (2026-10-16)

- Owner's setup on 15: **15 × 22 grid**. Goal: **free placement** of icons and **finer widget
  resizing**. On 15 this works through the fine grid plus A (icons drawn full size, overlapping
  neighbouring cells).
- Stock 16 can't do it: max rows 20 even with "Increase maximum grid size limit", and at 15 × 20
  home icons shrink to clipped fragments without labels (measured on the emulator 2026-10-15).
- Considered: a subgrid (finer grid, app icons spanning 2×2 / 4×4 cells, like Nova). Rejected for
  now: ~2–3× the work, and the owner's 15 × 22 layout would need converting. A keeps positions as
  whole cells, so a 15 layout carries over unchanged.
- **Build next session:**
  1. A — overlapping cells (cells never shrink below icon+label; overlap spread evenly; dock
     keeps upstream sizing), **with the 15 bugs fixed**: left/top padding counted twice in
     `cellToRect`; `CellLayoutLayoutParams` shift uses `mCellX` instead of the RTL/tmp-aware
     coordinate; LayoutParams vs `cellToRect` shift formulas disagree for multi-span widgets;
     debug printlns. Widgets got only a width floor on 15 — decide deliberately.
  2. B — grid limit 24 (normal) / 29 (increased), so 15 × 22 is allowed.
  3. C — always scale icons on scalable grids; label-size factor feeds cell height; never hide
     labels on cramped grids.
  4. D — "Bottom padding multiplier" slider, keep key `dj_home_bottom_padding_multiplier`
     (DECISIONS #5), int −100..100, default 1, ~1dp per step; check how it combines with 16's
     vertical padding factor.
  5. F — tighter widget resize frame (handle margin 4dp, background padding 8dp, frame margin
     8dp, frame padding = handle margin) and grab area 4× background padding.
  6. Finer home icon size and label size sliders (0.05).
- **Decide after the grid works, from screenshots:** E's compact spacing (cell x padding 8→4dp,
  page indicator 24→16dp, compact drop-target bar).
- Acceptance to write first in the build session: a 15 × 22 layout keeps full-size icons with
  labels; drag/drop and folder creation land in the right cells (LTR and RTL); a multi-cell
  widget and a 1×1 icon line up with their drop outlines; widget resize steps are fine-grained;
  dock unchanged.

## Build notes (2026-10-16, read before building)

- **C, restated from the 15 diff.** Phone grids (all of Lawnchair's phone grid options are
  non-scalable; the emulator's is) never shrink: when the rows don't fit, 16 scales border space
  and padding down and leaves the cells too small for the icon, which is what clips it. 15's change
  meant "only scalable (tablet) grids scale". 16 already applies the label-size factor before the
  cell height is worked out (LC change), and on a portrait phone it only hides labels in
  landscape/multi-window, so "never hide labels" is that path only.
- **A, one geometry for everything on the home screen.** Every cell is drawn at least
  icon + label size. The extra width is taken back evenly, so column 0 is flush left and the last
  column flush right (rows likewise). An item spanning cells covers from its first cell's left
  edge to its last cell's right edge. Icons, folders, widgets and At a Glance all use this, and so
  do the drop outline, hit-testing and the resize frame, from one function, so they cannot
  disagree (15's bugs came from three copies of the formula). **Widgets get the same rule in both
  directions** (15: width only). Otherwise a widget's drop outline and the widget itself would
  have different sizes. Dock and open folders keep upstream sizing.

## Acceptance

Emulator `VinPix_Dev`, 1080 × 2400, 420 dpi. Evidence in `.ai/shots/hg-*`. "Before" =
`hg-before-15x20.png` (stock 16 at 15 × 20: clipped icon fragments, no labels).

1. **Grid limit (B).** Grid settings rows/columns sliders reach 24, or 29 with "Increase maximum
   grid size limit". Set 15 × 22; `dumpsys` shows `inv.numRows: 22`, `inv.numColumns: 15`, and it
   survives a launcher restart. Evidence: settings screenshot + dumpsys.
2. **Full-size icons with labels at 15 × 22 (A, C).** Home icons are the same pixel size as at
   the default grid (`iconSizePx` equal in dumpsys) with their labels visible and not clipped.
   Evidence: home screenshot next to the before.
3. **Even spread (A).** Icons in column 0 and column 14 sit flush with the grid's left and right
   edges; row 0 and row 21 sit at the top and bottom of the grid area, not under the dock.
   Evidence: screenshot with icons in the four corners.
4. **Drag and drop lands where the outline shows (A).** Drag an icon to the left edge, the middle
   and the right edge (columns 0, 7, 14) and a bottom row. During each drag the drop outline sits
   on the target cell, and on release the icon lands exactly there. Evidence: screen recording,
   frames of the outline and the landed icon.
5. **Folder creation vs placing next to (A).** Dropping an icon onto another makes a folder;
   dropping it one cell away places it in that cell, no folder. Evidence: screenshots.
6. **Multi-cell widget lines up (A).** Add a 2-or-more-cell widget: its drop outline, its placed
   bounds and its resize frame match (within ~2 px). A 1 × 1 icon next to it lines up with its own
   outline too. Evidence: recording frames.
7. **Fine widget resizing (F).** The resize frame hugs the widget (handles about 4 dp off its
   edge, not 13 dp); handles are easy to grab; each step of a resize changes the widget by one
   cell of the 15 × 22 grid. Evidence: screenshot of the frame + recording of a resize.
8. **RTL (A).** With an RTL locale, drops still land in the aimed cell and widgets keep their
   bounds. (Upstream never mirrors the home grid, only open folders, which keep upstream sizing;
   so 15's RTL bug cannot show on the home screen.) Evidence: screenshot. Locale reset afterwards.
9. **Dock and open folders unchanged.** Dock crop identical before/after (same position, size and
   icons); an open folder looks the same as before. Evidence: crops side by side.
10. **Default grid unchanged (positive control).** At the stock default grid, the home screen is
    pixel-identical to the build without home-grid (the floor and the no-shrink rule must not kick
    in when cells are already big enough). Evidence: diff of two screenshots, plus a diff that
    *does* show a change (15 × 22) to prove the diff works.
11. **Bottom padding slider (D).** Home screen settings: "Bottom padding multiplier", −100..100,
    default 1, about 1 dp per step, stored under `dj_home_bottom_padding_multiplier`. +40 moves
    the bottom of the grid up by ~40 dp × 2.625 ≈ 105 px, −40 moves it down; the dock does not move.
    Works together with 16's vertical padding (that one scales, this one adds). Evidence: three
    screenshots, measured row positions.
12. **Finer sliders.** Home icon size and label size step by 5 %; at 15 × 22 a bigger label size
    still shows full labels (cells grow to fit). Evidence: settings screenshot + home screenshot.
13. **No crashes** through all of the above (`logcat -b crash` empty).

## Acceptance results (2026-10-16, VinPix_Dev API 37, 15 × 22)

Geometry: slot 69 × 74 px, drawn 182 × 239 px (overlap 113 / 165), so cell *i* starts at
`69i − 113i/14` (rows: `74i − 165i/21`). Every measured bound below matches that formula.

1. Grid sliders accept 22 rows; `inv.numRows: 22`, `inv.numColumns: 15` after restart.
2. Icons 171 px (same as default grid) with labels under them; `hg-home-before-after.png`.
   Found and fixed on the way: upstream pulls the label towards the icon by half the cell's spare
   height, which is negative on a dense grid (−75 px) and put labels across the icons; clamped
   at 0, which is what 15 effectively did.
3. Column 0 at x=23 (flush with the 21 px padding + 2 px rounding), column 14 ends at 1057; row 21
   ends at y=1792, above the dock. At a Glance spans row 0 from y=164. `hg-corners.png`.
4. Drops at column 14 / row 21, column 14 / row 2, column 0 / row 21, column 7 / row 10 each landed in
   the cell the outline showed. `hg-drag-sheet.png`, `hg-drag-*-mid.png`.
5. One cell next to Settings → placed in column 8, no folder; onto Settings → folder in column 7.
   `hg-folder-pair.png`.
6. Chrome Dino 5 × 5: placed at [23,1024]–[449,1528] (cell area), = formula; mid-drag outline matches
   with upstream's usual inset. Found and fixed: widgets were told their slot size (69 × 74 per
   cell) and picked tiny layouts; size reports and default/min/max spans now use the drawn size.
   `hg-widget-pair.png`, `hg-widgets-v2.png`.
7. Resize frame on the clock: one handle step = one column (host 333 → 394 px).
   `hg-resize-steps.png`.
8. Hebrew locale: drop landed at column 14 / row 15 as aimed. `hg-rtl-1.png`.
9. Dock crop identical to the before (max diff 0). Open folder: not diffed (folders take the
   upstream path, `drawn = 0`).
10. 4 × 6, bottom padding 0: pixel-identical to the pre-home-grid build (max diff 0); at the
    default 1 the same diff shows a change (max 255).
11. Bottom padding −40 / −9 / +31 → workspace bottom padding 412 / 493 / 598 px (0 = 517);
    dock fixed at y=1855. Found and fixed: upstream `pxFromDp` returns −1 for negative sizes.
12. Label size 115 % and icon size 105 % reachable; at 115 % labels fit (cell 239 → 245 px).
13. No Lawnchair entries in `logcat -b crash` (one uiautomator crash from the test tooling).

## Open (owner)

- ~~Grab area~~: 32 dp kept (owner, DECISIONS #15).
- **Slider changes apply on the next reload, not always live.** The grid reload reads
  PreferenceManager2's in-memory cache, which is refreshed asynchronously (`83658ce7eb`); seen with
  the bottom padding slider. Same mechanism as upstream's own padding sliders. Not fixed.
- Once, the first long-press on the clock widget after a reinstall moved it from column 9 / row 3
  to column 10 / row 1. Not reproduced in four more tries at different grab points.
- E (compact spacing) to decide from the screenshots.
