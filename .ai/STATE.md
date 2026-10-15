# Lawnchair state

phase: 1 — porting features to 16 · 4/15 done, 1 dropped · next: #7 icon-tweaks · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the AI setup (`c219960f78`) + ported features.
Done: `gesture-open-settings` (double tap by hand still pending), `smartspace-style`, `drawer-look`
(sliders only), `drawer-frequent` (extra suggestion rows). Dropped: `depth-blur`. The 15 implementation is in `~/work/apps/lawnchair-15-ref`
(DECISIONS #3). Feature list and order: ROADMAP.

## now

- [ ] Owner: double-tap the home screen once by hand (double tap is set to "Open Lawnchair
      settings" on the emulator). adb cannot inject a double tap here (feature spec, results #2).
- [ ] `/port-feature icon-tweaks` (ROADMAP #7): spec first; edits are inside the submodule.
- Parked for last: `feed-bridge` (#3), see its spec. Emulator has AIDL Bridge installed, Show feed
  on, provider AIDL Bridge.

Emulator state: drawer launch history seeded for drawer-frequent tests (two suggestion rows show). At a Glance is styled green / 50 % / condensed from the smartspace-style test;
reset it (Settings → At a Glance → Style) before comparing anything against upstream.

Emulator: `VinPix_Dev` (API 37), Lawnchair debug is its home app (DECISIONS #6). Tap helpers:
`adb shell input tap/swipe` work; double tap does not. Wait for the home screen to load
(e.g. the dock's "Phone" label) before sending gestures.

## open questions (owner)

1. **How do we take upstream updates?** Merge `upstream/16-dev` into `dj-dev-16`, or rebase onto
   it. Rebase keeps our changes as a clean stack on top; merge never rewrites pushed history.
2. **Workflows:** 15 deleted `.github/workflows` (`84ab193763`). Delete them again on 16, or
   leave upstream's in place?
