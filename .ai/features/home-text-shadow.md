# home-text-shadow: lighter soft shadow under home-screen text

status: done 2026-10-17 (owner chose from screenshots)

## What the user sees

On dark wallpapers (light home text), the soft halo under icon labels and other home text is
fainter: ambient shadow 25 % → ~6 % black. The sharper key shadow is unchanged. Light wallpapers
(dark text) have no ambient shadow either way.

## Settings

None.

## On 15 (reference)

`e5ed825962` (2024-08-25, part of "SmartSpacer customisation"): `workspaceAmbientShadowColor`
`#40000000` → `#10000000` in the light-text theme, `res/values/styles.xml`.

## On 16

Not covered (still `#40000000`). Ported as the same one value in `res/values/styles.xml`,
marked `DJ(home-text-shadow)`.

## Acceptance results (2026-10-17, VinPix_Dev)

Before/after crops of "Google" and "Settings" labels on a photo wallpaper,
`.ai/shots/ts-pair.png`: halo visibly lighter; largest pixel difference 38/255. Owner kept it.
