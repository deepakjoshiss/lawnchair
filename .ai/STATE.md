# Lawnchair state

phase: 1 — porting features to 16 · 10/15 done, 4 dropped, widget-host built · next: owner checks widget-host on a device; then phase 2 · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the AI setup + ported features, all committed
(last feature commit: wallpaper-shift). Not pushed.
Done: `gesture-open-settings`, `smartspace-style`, `drawer-look` (sliders only), `drawer-frequent`
(extra suggestion rows), `icon-tweaks`, `folder-look`, `home-grid`, `dock-rows`, `wallpaper-shift`, `home-text-shadow`; `widget-host` built, unverified. Dropped: `depth-blur`, `dock-poweramp`, `android16-fix`, `feed-bridge`.
The 15 implementation is in `~/work/apps/lawnchair-15-ref` (DECISIONS #3). Order: ROADMAP.

## now — NEXT ACTION: verify widget-host

- [ ] **widget-host** built (DECISIONS #21) but its look is unverified: the emulator's Google
      Calendar is not signed in, so the agenda widget shows a "Sign in" card, not the list the
      styling targets. Owner will check it on a device with a signed-in Calendar (2026-10-17).
      Test widget placed on home page 3 of the emulator.
- After that, phase 1 (porting) is complete: all 15 ROADMAP features are done or dropped.
- Watch: widgets found moved twice with no matching test touch (home-grid spec, "Long-press
      edit mode"). Grid sliders sometimes apply only on the next reload (PreferenceManager2
      cache refreshed async, `83658ce7eb`).

## emulator (`VinPix_Dev`, API 37, DECISIONS #6)

- Lawnchair debug is the home app; reinstalls can drop the role:
  `adb shell cmd role add-role-holder android.app.role.HOME app.lawnchair.debug`.
- Current state: grid **15 × 22** ("Increase maximum grid size limit" on), horizontal padding
  25 %, vertical 0 % (owner's); World clock, Lawnchair and Chrome Dino (page 2) widgets, folders. Grid can be set by editing `pref_workspaceColumns/Rows` in
  `shared_prefs/com.android.launcher3.prefs.xml` via `run-as` (app stopped). Dummy legacy-icon apps
  `com.dj.dummy.blue/.green/.red`. Drawer launch history seeded (two suggestion rows).
  At a Glance styled green / 50 % / condensed (reset before comparing with upstream).
  AIDL Bridge installed, Show feed on. Folder stack on, footer on, label colour auto.
- Helpers in `.ai/bin/`: `emu-ui` (texts / tap "<text>" / waitfor / shot), `emu-launches`
  (launch apps from the drawer N times), `pred-counts` (decode the predictions DataStore —
  not `strings`), `make-dummy-apps` (build the dummy apps), `emu-drag` (long-press drag),
  `emu-slider` (set a settings slider by tapping), `emu-wallpaper-x` (wallpaper offset). ffmpeg/ffprobe: see
  `~/.claude/CLAUDE.md` (not on PATH).
- Gestures: `adb shell input tap/swipe` work; double tap does not; wait for the dock's "Phone"
  label before gestures. Open the drawer via long-press on home → "Apps list" (a swipe was
  unreliable). Settings deep links: `lawnchair://settings/<route>` (routes in
  `PreferenceRoutes.kt`). Screen recordings: warm up `screenrecord` before acting; it only
  writes frames when the screen changes.

## lessons from session 1 (2026-10-14/16)

- Bounded waits only (an unbounded `until` hung once). zsh doesn't word-split `$var` — use
  bash scripts or functions. Verify readers (a `strings` read undercounted; a saturated diff
  image showed nothing) before trusting a result.
- Incremental Kotlin built a crashing APK once; the gate is non-incremental (DECISIONS #8).
- 16's folder close has a small (~1.5–3 %) end-of-animation size snap, upstream too; not
  reproduced by the owner after the clip fix (folder-look spec, "Known, not fixed").

## open questions (owner)

None. Upstream updates: rebase (DECISIONS #24, UPSTREAM.md).
