# drawer-frequent: extra suggestion rows for very frequently used apps

status: done (2026-10-15) — built on 16's Suggestions; the 15 implementation is not ported (DECISIONS #11)

## What the user sees

The app drawer's suggestions (16's "Suggestions", Lawnchair predictor) show one row as upstream
does, plus up to **two more full rows** when there are apps used almost as much as the ones in
row 1. All rows sit inside the suggestions area above "All apps" and scroll with it.

**Rule** (owner's option 2, 2026-10-15): an app beyond row 1 qualifies if its launch count is
at least **half** that of the least-launched app in row 1 and at least **3**. Only full rows show
(partial rows dropped), at most 2 extra. If row 1 itself is not made of such apps (padded with
fallback suggestions), no extra rows. Counts are launches from the drawer over upstream's last 250
(`all_apps_usage`). Only for the "Lawnchair" suggestions provider (the default); the system
provider sends one row.

## Settings

None new. Upstream's Suggestions screen controls the feature (on/off, provider, blocked apps).

## On 15 (reference)

`e89c4d3f8f` (2024-08-27) + `af90566e12`, `bd53909ff6`: frequent apps from `UsageStatsManager`
(weekly, ≥ 15 min foreground), launch count read from a hidden `UsageStats` field by a Parcel
offset (+76), block of full rows at the top of each tab with an accent divider, always on, needs
Usage access, refreshed only on full model reload. Not ported (DECISIONS #11).

## On 16 (implementation)

- `lawnchair/src/app/lawnchair/predictions/FrequentRows.kt` (new): `frequentSuggestionCount()`,
  the rule above as a pure function.
- `LawnchairPredictionStore.getCounts()` (`DJ`): per-key launch counts.
- `LawnchairAppPredictor.updates()` (`DJ`): requests up to 3 rows of all-apps targets and keeps
  `frequentSuggestionCount(...)` of them.
- `quickstep/.../appprediction/PredictionRowView.java` (`DJ`, upstream file): rows = full rows
  received (max 3); icons laid out as a grid when rows > 1; height = rows × cell height; with one
  row the original `LinearLayout` path is unchanged.
- Check: `.ai/checks/FrequentRowsCheck.kt`, run by `.ai/bin/check` ("frequent rows", 9 cases;
  it failed on two wrong expectations before passing, so it can fail).

## Acceptance results (2026-10-15, VinPix_Dev API 37)

Counts read by decoding the DataStore proto (`all_apps_usage`); an earlier `strings`-based read
undercounted and was discarded.

1. Before any launches: 1 row. `50-suggestions-before.png`.
2. Row 1 weakest 4, next apps at 2 (below the 3 floor): **1 row**. `51-suggestions-1row.png`.
3. Three apps at 3, a fourth at 2: **1 row** (partial row dropped). `52-crop.png`.
4. Four apps at 3: **2 rows** — Contacts/Calendar/Files/Clock, then Gmail/Photos/Drive/Maps.
   `53-crop.png`.
5. Scroll down and back: rows slide under the search bar and return intact. `56-scroll-pair.png`.
6. No crashes in the crash buffer throughout.
7. Owner approved the two-row look (2026-10-15).

Not tested on device: 3 rows (covered by the unit check), the taskbar all-apps and secondary
display, which share `PredictionRowView` and will also show extra rows.
