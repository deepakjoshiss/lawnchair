# android16-fix: 15-era fix for running on Android 16

status: dropped 2026-10-17 (DECISIONS #22)

## On 15 (reference)

`cf19020393` (2025-09-15, "Fix for Android 16", on the 14-era `dj-dev`, merged into `dj-dev-15`
by `af90566e12`):

1. `RecentsAnimationDeviceState`: `mCanImeRenderGesturalNavButtons` hard-coded `true` instead of
   calling the hidden `InputMethodService.canImeRenderGesturalNavButtons()` (inferred: crashed or
   was denied on Android 16). The merge kept the 15 base line (upstream `95bf5aef9f` had already
   removed the hidden call), so this half never shipped on 15.
2. Submodule bump `282d245043`: legacy icons with almost no transparent margin drawn at 0.85, and
   the wrapper tile from the light vibrant colour. Not Android-16 related; just shipped in the
   same commit.

## On 16

1. Gone: no `canImeRenderGesturalNavButtons` / `mCanImeRenderGesturalNavButtons` anywhere in 16
   (`git grep`, 2026-10-17); 16 decides IME nav buttons from sysui state.
2. Covered by `icon-tweaks` (DECISIONS #12, `d474dee633`): `LegacyIconFit.kt` (0.85),
   `DjLegacyIcons.kt` (light vibrant), without submodule edits.
