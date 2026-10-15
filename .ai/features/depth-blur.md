# depth-blur: wallpaper zoom and blur during launcher transitions

status: dropped (owner, 2026-10-15; DECISIONS #9). 16's own zoom and blur stand; nothing from 15 ported.

## What the user sees

When the app drawer, overview/recents, an app, or edit mode opens, the wallpaper zooms out and the
launcher's background blurs. 15 changed how strong both are. Gated by the existing Home screen
setting "Wallpaper depth effect" (`enable_wallpaper_depth_effect`, default on).

## Settings

None new.

## On 15 (reference)

Commit `be82c989c6` (2025-09-23, "…depth animation…"), the only commit for this; no reason given.

| Change | Code | Effect on 15 |
|---|---|---|
| A. Depth capped at 0.3 | `BaseDepthController.setDepth`: `if (depthF > 0.3f) depthF = 0.3f;` | zoom and blur stop growing at 30 % depth |
| B. Zoom ×1.75 | `setWallpaperZoomOut(windowToken, depth * 1.75f)` (flag-on branch) | with A: max zoom 0.525 (stock 0.6 drawer, 0.7 overview); small states zoom more (edit mode 0.26 vs 0.15) |
| C. Max blur 23 → 60 px | `quickstep/res/values/config.xml` `max_depth_blur_radius` | blur 54 px vs stock 23 px at drawer/overview |
| D. No early-wakeup on API 36+ | `Build.VERSION.SDK_INT <= VANILLA_ICE_CREAM` guard | avoided a crash on Android 16 |
| E. Logging | catch block prints; a commented debug print | none |

## On 16

- **C is covered.** 16 uses `max_depth_blur_radius_enhanced` = 30dp when `allAppsBlur` or
  `enableOverviewBackgroundWallpaperBlur` is on; both default `true` in `FeatureFlagsImpl`. That is
  ~79 px at 420 dpi (stronger than 15's 60 px), and blur still maps to full at 30 % depth
  (`mapDepthToBlur` = `clampToProgress(depth, 0, 0.3)`). **Not ported.**
- **D is covered.** Upstream `6fd747790d` wraps the early-wakeup calls in
  `try/catch (NoSuchMethodError)` and the call body is commented out. **Not ported.**
- **E: not ported** (debug noise).
- **A + B: open.** On 16 zoom = `depth` (flag-on branch). Porting A changes `mDepth` itself, which
  16's newer stuck-blur fixes (`c32af3c8c5`, `26606067e1`) also read. So if the zoom feel is wanted,
  port it as a **zoom-only** change: `setWallpaperZoomOut(windowToken, min(depth * 1.75f, 0.525f))`,
  leaving `mDepth` and the blur alone. Same zoom curve as 15, no side effects on blur state.

Emulator supports background blur (`ro.surface_flinger.supports_background_blur=1`), so a
side-by-side comparison is possible.

## Acceptance (only if the zoom part is ported)

1. Screen recordings on `VinPix_Dev` of opening the app drawer and overview: stock 16 vs zoom-only
   change, frames at the same timestamps. Owner judges the feel.
2. Wallpaper depth effect off: no zoom in either build.
3. No stuck blur after closing the drawer or a folder (the case 16's fixes target). Evidence:
   screenshot after close.
