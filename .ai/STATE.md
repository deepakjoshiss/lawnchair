# Lawnchair state

phase: 1 — porting features to 16 · 1/14 done · next: #2 smartspace-style · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the AI setup (`c219960f78`) + ported features.
Done: `gesture-open-settings` (double tap by hand still pending). The 15 implementation is in `~/work/apps/lawnchair-15-ref`
(DECISIONS #3). Feature list and order: ROADMAP.

## now

- [ ] Owner: double-tap the home screen once by hand (double tap is set to "Open Lawnchair
      settings" on the emulator). adb cannot inject a double tap here (feature spec, results #2).
- [ ] `/port-feature smartspace-style` (ROADMAP #2): spec first.

Emulator: `VinPix_Dev` (API 37), Lawnchair debug is its home app (DECISIONS #6). Tap helpers:
`adb shell input tap/swipe` work; double tap does not. Wait for the home screen to load
(e.g. the dock's "Phone" label) before sending gestures.

## open questions (owner)

1. **How do we take upstream updates?** Merge `upstream/16-dev` into `dj-dev-16`, or rebase onto
   it. Rebase keeps our changes as a clean stack on top; merge never rewrites pushed history.
2. **Workflows:** 15 deleted `.github/workflows` (`84ab193763`). Delete them again on 16, or
   leave upstream's in place?
