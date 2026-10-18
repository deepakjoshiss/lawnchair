# drawer-glass: glass search bar in the full-screen app drawer

status: wip (steps 1–4 built 2026-10-19; owner's Pixel sign-off open) — new on 16, no 15 version. DECISIONS #29–#32.

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
4. Liquid glass refraction of the scroll-under backdrop — done (DECISIONS #32), 1.2 × 24dp.

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
- Status bar tint (DECISIONS #31): at ~⅓ box-height scroll the strip read 22,22,28 vs 25,26,31
  unscrolled (≈12 % black, as expected); `97`–`99-tint-*.png`.
- Refraction (DECISIONS #32): an exaggerated 2.4 × 40dp build changed only the rim band (diff 53
  at the edge, 0 in the middle and outside), so the lens is applied and local; `f51/refract-3.png`.
- Light theme (2026-10-19): Liquid hint 5.6:1 at rest, 5.4:1 worst over frosted apps; status bar
  tint is white (strip 231 → 245 when scrolled), status icons stay black. `104`–`106-light-liquid-*`.
