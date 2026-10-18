# drawer-glass: glass search bar in the full-screen app drawer

status: wip (steps 1–3 done 2026-10-19; step 4 open) — new on 16, no 15 version. DECISIONS #29, #30.

## What the user sees

The drawer's search pill is glass over the blurred wallpaper, with the hint shown when empty.
Full-screen drawer only (DECISIONS #28); the bottom sheet keeps upstream's pill.

## Settings (Search bar → App drawer tab)

| Setting | Key | Values | Shown on |
|---|---|---|---|
| Glass style | `dj_drawer_search_glass` | Frost (default) / Liquid glass | Android 13+; below: Frost, row hidden |
| Scroll apps under search bar | `dj_drawer_search_scroll_under` | off (default) / on | Android 12+; below: row hidden |

## Steps

1. Frost — done (DECISIONS #29).
2. Liquid glass light effects — done (DECISIONS #30), `LiquidGlass.kt`.
3. Scroll-under — done (DECISIONS #30), `DrawerScrollUnder.kt`: lists and suggestion rows run to
   the screen top; blurred only in the pill (24dp) and behind the status bar (72dp).
4. Liquid glass refraction of the scroll-under backdrop — open.

## Acceptance

- Frost: pill visible and text readable in dark and light themes (measured: light hint 5.3:1,
  pill 222 vs 238 + edge; dark hint ~5:1 on a dark wallpaper; known exception in DECISIONS #29).
  `72-light-rest.png`, `73-dark-rest.png`. Icon 18dp.
- Each later step: emulator screenshots or recording, owner judges; frame timings with
  `dumpsys gfxinfo` with scroll-under off/on.
- Steps 2–3 (2026-10-19, `VinPix_Dev`): Liquid hint 5.6:1 light / 3.5:1 dark (bright-wallpaper
  exception); scroll-under off leaves every measured position unchanged; on, rest positions
  unchanged, rows and apps pass under pill and status bar (`88`, `89`, `94-sb72.png`); fling to end
  and back is pixel-identical at rest (mean diff 0; control vs scrolled: 31). Spacing measured:
  status bar → pill 63px (24dp), pill → box bottom 32px (12dp). `95-gap-rest.png`.
