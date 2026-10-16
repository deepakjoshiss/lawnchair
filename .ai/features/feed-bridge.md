# feed-bridge: feed reconnects after the bridge disconnects

status: dropped 2026-10-17 (owner; DECISIONS #23). Nothing ported: the measurements below showed
Android re-binds the bridge by itself, so the disconnect fix had no effect; changes 1–2 were
already declined (DECISIONS #9).

## What the user sees

With a feed provider (AIDL Bridge) chosen, the left screen shows the Google feed. If the bridge
app disconnects (crash, update, force-stop), the feed comes back the next time it is needed,
instead of staying blank until Lawnchair restarts.

## Settings

None new. Uses upstream's Settings → Feed provider (`pref_feedProvider`) and, only if ever needed,
Debug menu → "Ignore feed whitelist" (`pref_ignoreFeedWhitelist`).

## On 15 (reference)

Commit `3fac2fc31f` (2024-08-11, "…feed fixes…", on the 14 base), three changes:

1. `FeedBridge.shouldUseFeed = true` (upstream: off for debuggable/system builds unless a provider
   is chosen).
2. Every signature check returns `true` (`BridgeInfo.isSigned`, `CustomBridgeInfo.isSigned`,
   `isAvailable` no longer calls `isSigned`).
3. `BaseClientService.onServiceDisconnected` sets `mConnected = false`. Without it, `connect()`
   (`if (!mConnected) bindService(...)`) never rebinds after a disconnect.

## On 16

- Change 3: 16's `onServiceDisconnected` is still empty (checked 2026-10-14, 2026-10-17). Not ported
  (owner, 2026-10-17): no measured effect, see below.
- Changes 1 and 2: **not ported** (DECISIONS #9). The owner's feed app is AIDL Bridge v3
  (`amirz.aidlbridge`); its certificate's `Signature.hashCode()` is `0xb662cc2f`, equal to 16's
  whitelist entry (computed from the APK on the owner's Pixel, Java hash reproduced against two
  known values). AIDL Bridge was whitelisted with the same value on the 14 and 15 bases too. With
  a provider chosen, `resolveBridge` uses it in debug builds as well. The bypass also let any app
  pose as the feed.

No upstream file edited.

## Acceptance

On `VinPix_Dev`, with the AIDL Bridge APK from the owner's Pixel installed and chosen as feed
provider:

1. Feed connects: `dumpsys activity services amirz.aidlbridge` shows a binding from
   `app.lawnchair.debug`; the left screen opens. Evidence: dumpsys lines, screenshot.
2. **Without the fix** (build `9220dfd6ec`): after force-stopping the bridge, the binding does not
   come back. This proves the test can fail. Evidence: dumpsys.
3. **With the fix**: same steps, the binding comes back and the left screen opens again.
   Evidence: dumpsys, screenshot.

## Measurements (2026-10-14, VinPix_Dev API 37, build without the fix, AIDL Bridge v3 from the owner's Pixel)

- The feed itself never shows on this emulator: Lawnchair binds the bridge (2 connections,
  flags `0x21` long-lived `LauncherClientService`, `0x41` per-start `BaseClientService`) but the
  bridge never binds the Google app's `DrawerOverlayService`, which the Google app does offer.
  Not investigated further.
- **Force-stop the bridge**, leave home and return: both connections come back as new records.
- **Crash the bridge** (`am crash`): the long-lived binding stays (same record), Android restarts
  the bridge (new pid), the per-start one is re-made on return.
- So the stale `mConnected` never blocked a reconnect in either case: Android keeps a
  `BIND_AUTO_CREATE` binding alive across a crash and calls `onServiceConnected` again by itself,
  which is also what the `ServiceConnection.onServiceDisconnected` docs say. The 15 fix is
  harmless (a second `bindService` with the same connection reuses the binding) but showed no
  effect here.
