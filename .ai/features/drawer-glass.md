# drawer-glass: glass search bar in the full-screen app drawer

status: wip (step 1 Frost done 2026-10-18) — new on 16, no 15 version. DECISIONS #29.

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
2. Liquid glass light effects: AGSL shader on the pill (rim, specular sheen, edge glow).
3. Scroll-under: list and suggestion rows extend under the bar; backdrop view blurs the strip.
4. Liquid glass refraction of the scroll-under backdrop.

## Acceptance

- Frost: pill visible and text readable in dark and light themes (measured: light hint 5.3:1,
  pill 222 vs 238 + edge; dark hint ~5:1 on a dark wallpaper; known exception in DECISIONS #29).
  `72-light-rest.png`, `73-dark-rest.png`. Icon 18dp.
- Each later step: emulator screenshots or recording, owner judges; frame timings with
  `dumpsys gfxinfo` with scroll-under off/on.
