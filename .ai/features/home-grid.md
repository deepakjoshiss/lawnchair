# home-grid: home screen grid, spacing, bottom padding, widget sizing

status: spec — approach decided (DECISIONS #14); build next session

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
