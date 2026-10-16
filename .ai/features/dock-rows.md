# dock-rows: dock pages and rows, horizontal spacing, widgets in the dock

status: done 2026-10-16 (DECISIONS #18)

## What the user sees

Dock settings: "Dock rows" (1–2) and "Dock pages" (1–5) from upstream 16; widgets can be dropped
on the dock (upstream 16); "Horizontal spacing" (ours): the dock's side padding, 0–170 % of 16dp,
default 100 %, portrait only. The Dock settings preview follows all of them.

## Settings

| key | type | default | where in Settings |
|---|---|---|---|
| `hotseat_horizontal_factor` | float 0–1.7 | 1.0 | Dock → Horizontal spacing |
| `dj_dock_row_height_factor` | float 0.5–1.5 | 1.0 | Dock → Dock row height (new in 16) |

Key kept from 15 (DECISIONS #5); upstream's `pref_hotseatRows`, `pref_dockPages` cover rows/pages.

## On 15 (reference)

`1b282a69cf` "Added Poweramp widget, Spacing fixes": `hotseatHorizontalFactor` (default borrowed
from the bottom-space factor, 1.0), slider 0–1.7 step 0.05, `Hotseat.setInsets` side padding =
factor × `dynamic_grid_hotseat_side_padding` (set to 16dp). Rows and the migration fix are in the
same feature on 15.

## On 16

- Rows (1–2) and pages (1–5): covered by upstream (`6741c3416d`, `99a4e77713`); widgets on the dock:
  upstream `6d084bfa43`, works as the owner wants (owner, 2026-10-16).
- 15's dock migration fix: `GridSizeMigrationUtil.java` is gone on 16; not ported.
- Horizontal spacing: ported. Hook in `Hotseat.setInsets` (read with `firstBlocking`, so a change
  is never applied from the stale cache), 16dp in `dj_hotseat_side_padding`, preview re-renders on it.

## Acceptance results (2026-10-16, VinPix_Dev)

1. Phone (first dock icon) left edge: 0 % → 0px, 100 % → 40–43px (16dp = 42px), 170 % → 73px.
2. Applies as soon as settings are left (0 % applied live).
3. Dock settings preview follows the slider (`.ai/shots/dockprev-pair.png`, 100 % vs 5 %).

## Dock row height (new in 16, owner 2026-10-17, DECISIONS #26)

Dock → "Dock row height", 50–150 % in 5 % steps, 100 % = upstream. Scales the dock row height
(`hotseatCellHeightPx`), so the gap between rows and the dock's height follow. Below about 70 % a
row is shorter than its icon and, at 50 %, the bottom row is drawn over the dock search bar;
owner chose to leave that as is. Measured on VinPix_Dev: 193px at 100 %, 290 at 150 %, 97 at
50 % (`.ai/shots/drh-sheet.png`, taken with the earlier −50..+50 scale, same values).
