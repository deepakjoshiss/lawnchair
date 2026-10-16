# wallpaper-shift: where the wallpaper sits, and a steady scroll between pages

status: done 2026-10-17 (DECISIONS #20)

## What the user sees

Settings → Home screen → Wallpaper, right after "Scroll wallpaper": a slider "Wallpaper center
shift", 0–100 % in 5 % steps, default 50 %. It sets where the wallpaper sits on the first page:
0 % = its left edge, 50 % = centred, 100 % = its right edge. Swiping to later pages moves the
wallpaper on from there towards its right edge (over the remaining part only), so the last page
never runs past the edge.

With one page, with "Scroll wallpaper" off, or while the launcher pins the wallpaper to the
default page, the wallpaper stays put at the slider position (upstream: pinned to the left edge).

Worked numbers (static wallpaper, 50 %; upstream spreads the scroll over at least 4 pages):

| pages | 15 / this port | upstream 16 |
|---|---|---|
| 1 | 0.50 | 0.00 |
| 3 | 0.50 → 0.67 → 0.83 | 0.00 → 0.33 → 0.67 |
| 5 | 0.50 → 1.00 | 0.00 → 1.00 |

(0 = wallpaper's left edge on screen, 1 = its right edge.)

## Settings

| key | type | default | where in Settings |
|---|---|---|---|
| `dj_pref_wallpaperShift` | float 0–1 | 0.5 | Home screen → Wallpaper → Wallpaper center shift |

Key and store kept from 15 (the old SharedPreferences `PreferenceManager`, next to
`pref_wallpaperScrolling`), so a 15 backup carries over (DECISIONS #5).

## On 15 (reference)

- `3fac2fc31f` (2024-08-11): pref, string, slider, first formula (centred only with fewer than 4
  pages; adding a second page made the wallpaper jump).
- `a1909995c4` (2024-08-13) "Stable wallpaper shift": final formula in
  `WallpaperOffsetInterpolator.wallpaperOffsetForScroll`: `offset = s + (1 − s) × upstream`; the
  pinned / single-page / zero-range early returns give `s`. `c2a8d30c16` only touched a debug line.
- No uncommitted edits for this feature in the reference folder or its submodule.
- Quirks: RTL is inconsistent (while scrolling, page 0 lands on the right edge whatever `s` is,
  but pinned it lands on `s`, so it jumps on pin/unpin); no change listener (a new value shows on
  the next scroll).

## On 16

Overlap: none. 16 has only "Scroll wallpaper" and still pins to the left edge / spreads over at
least 4 pages (`WallpaperOffsetInterpolator.java:74-131`). → full port.

Hook: the same method, one call into a Lawnchair-layer helper (`app.lawnchair.wallpaper`) that
remaps upstream's offset; upstream's own early returns and its newer wrap-aware scroll for
infinite scrolling (`c92a59b0ae`) stay as they are. `DJ(wallpaper-shift)` edits:
`WallpaperOffsetInterpolator.java`, `HomeScreenPreferences.kt`, `PreferenceManager.kt`, strings.

## Improvements (owner confirmed 2026-10-16)

1. **RTL mirrored and consistent:** in RTL the slider mirrors (first page at `1 − s`), scrolling
   and pinned agree, so no jump. 15's RTL behaviour looks unintended.
2. **Applies immediately:** moving the slider re-positions the wallpaper at once, instead of on
   the next swipe.

## Acceptance

Emulator `VinPix_Dev`; offsets read from `adb shell dumpsys wallpaper` (`mWallpaperX`).

1. Slider present: Home screen → Wallpaper, after "Scroll wallpaper"; 0–100 %, 5 % steps,
   default 50 %. Evidence: settings screenshot.
2. 3 pages, 50 %: `mWallpaperX` ≈ 0.50 / 0.67 / 0.83 on pages 1–3. Evidence: dumpsys per page.
3. 1 page, and "Scroll wallpaper" off: offset = slider (try 0 %, 50 %, 100 %). Evidence: dumpsys
   + screenshots.
4. Moving from 1 page to 2 pages does not make the wallpaper jump on page 1 (stays at `s`).
5. 0 % reproduces upstream exactly (positive control): same offsets as with the feature off.
6. (if improvement 2) slider change moves the wallpaper without a swipe.
7. (if improvement 1) RTL locale: first page at `1 − s`, no jump between pinned and scrolling.
8. No crashes.

## Acceptance results (2026-10-17, VinPix_Dev, 3 pages, `dumpsys window` → `mWallpaperX`)

1. Slider under "Scroll wallpaper", 0–100 %, 5 % steps, default 50 % (`.ai/shots/ws-settings.png`).
2. 50 %: 0.50 / 0.6667 / 0.8333; 25 %: 0.25 / 0.50 / 0.75.
3. Scroll wallpaper off, 25 %: 0.25 on all three pages.
4. Scroll wallpaper back on: page 1 stays 0.25 (pinned and scrolling agree, no jump). A real
   1-page → 2-page change was not run (emulator has 3 pages); same code path, by the formula.
5. 0 % = upstream: 0.00 / 0.3333 / 0.6667.
6. Slider 50 % → 0 % moved page 1 from 0.50 to 0.00 with no swipe; 0 % → 25 % likewise.
7. Hebrew locale (RTL), 25 %: 0.75 / 0.50 / 0.25. RTL with scrolling off not run (Hebrew settings
   labels); calculated 0.75.
8. No Lawnchair entries in `logcat -b crash`.

Found and fixed on the way: upstream's offset handler starts at 0.5 "to force an initial update",
so with a 50 % shift the first offset (0.5) was never sent and the wallpaper sat at 0.0 until the
first swipe; it now starts at −1.
