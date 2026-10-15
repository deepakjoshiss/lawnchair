# icon-tweaks: legacy icon fill, legacy tile colour, softer icon shadow

status: done (2026-10-15) — tweaks 1–3 ported outside the submodule; tweak 4 covered by 16 (DECISIONS #12)

## On 15 (reference)

Uncommitted edits inside the systemui submodule of `lawnchair-15-ref` (`iconloaderlib`, submodule
HEAD `970a3b2`); in no commit anywhere. `5b7e3e6337` ("…Adaptive Icon improvements") is grid
work, not icons.

| # | Change | Who sees it |
|---|---|---|
| 1 | Legacy icon that fills its square (transparent margins summing ≤ 0.05) drawn at 0.85 inside the wrapper instead of upstream's ~0.47 | non-adaptive icons, "wrap" on |
| 2 | Wrapper tile tinted from the icon's light vibrant colour instead of dominant | same; visible only with tile lightness < 100 % |
| 3 | Ambient icon shadow alpha 25 → 10 (key shadow 7 unchanged) | every icon with shadows on |
| 4 | Clock icon background built as `CustomAdaptiveIconDrawable` (follows the chosen shape) | Clock |

## On 16

- "Wrap non-adaptive icons" (`prefs_wrapAdaptive`) defaults **on** in 16; tile lightness
  (`pref_coloredBackgroundLightness`) defaults **1.0** (white tiles); shadows
  (`pref_shadowBGIcons`) default on.
- No submodule edits (we have no fork of it; DECISIONS #12). Implementation:
  - `lawnchair/src/app/lawnchair/icons/LegacyIconFit.kt` (new, pure): `transparentMarginSum`,
    `isFullBleed`, `FULL_BLEED_SCALE` 0.85, `FULL_BLEED_MAX_MARGIN` 0.05. Check:
    `.ai/checks/LegacyIconFitCheck.kt` (8 cases; a mutant with threshold 0.5 fails it).
  - `lawnchair/src/app/lawnchair/icons/DjLegacyIcons.kt` (new): `wrapLegacyIcon` — upstream's
    legacy wrap path with tweaks 1 and 2; returns null for adaptive / icon-pack / wrap-off.
  - `lawnchair/src/com/android/launcher3/icons/DjShadowGenerator.java` (new): ambient alpha 10;
    copies upstream's `drawShadow` and package-private `addPathShadow` (hence the package).
  - `src/com/android/launcher3/icons/LauncherIcons.kt` (`DJ`, upstream file): overrides
    `normalizeAndWrapToAdaptiveIcon` (→ `wrapLegacyIcon`, else super) and `getShadowGenerator`.
- Tweak 4: **covered**. With the Diamond shape the Clock icon's background is a diamond like the
  others on stock-structure 16 (`67-crop.png`). Not ported.
- Watch on upstream updates: `DjShadowGenerator` duplicates `ShadowGenerator`'s two draw methods;
  `wrapLegacyIcon` duplicates the legacy branch of `normalizeAndWrapToAdaptiveIcon`.

## Test apps

Three code-free dummy apps with PNG-only (legacy) icons, built by a scratch script
(`make-dummy-apps.sh`, not in the repo) and installed on `VinPix_Dev`: `com.dj.dummy.blue`
(full-bleed blue, white mark), `.green` (full-bleed dark green, yellow corner), `.red` (red square,
transparent margin).

## Acceptance results (2026-10-15, VinPix_Dev API 37, icon cache cleared after install)

1. Tweak 1: Dummy Blue and Green fill the circle; Dummy Red (padded) unchanged.
   `64-dummy-before-after.png` (top stock 16, bottom tweaked).
2. Tweak 3: per-pixel max difference stock vs tweaked: 2 (Camera cell), 4 (Dummy Red cell),
   0 (search bar control), 192 (status bar control, clock changed). The amplified difference is a
   ring at each icon edge (`66-shadow-diff.png`): the shadow is lighter, barely visible on a dark
   wallpaper.
3. Tweak 2: code only. With 16's default lightness 1.0 every tile is white, and full-bleed icons
   cover their tile, so nothing to see by default.
4. Tweak 4: covered by 16 (see above).
5. No crashes.
6. Owner approved the result (2026-10-15).
