# Lawnchair state

phase: 1 — porting features to 16 · 7/15 done, 1 dropped, 1 parked · next: #10 widget-host · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the AI setup + ported features, all committed
(last feature commit: home-grid). Not pushed.
Done: `gesture-open-settings`, `smartspace-style`, `drawer-look` (sliders only), `drawer-frequent`
(extra suggestion rows), `icon-tweaks`, `folder-look`, `home-grid` (E open). Dropped: `depth-blur`. Parked: `feed-bridge`.
The 15 implementation is in `~/work/apps/lawnchair-15-ref` (DECISIONS #3). Order: ROADMAP.

## now — NEXT ACTION: long-press edit mode, then widget-host

- [ ] **Long-press edit mode** (owner, 2026-10-16): smaller drop-target buttons at the top and less
      page shrink, so a widget doesn't jump cells when the finger moves slightly. Explore first
      (overlaps home-grid E: 15's compact drop-target bar).
- [ ] home-grid **E** (cell x padding, page indicator, drop-target bar), from screenshots.
- [ ] **widget-host** (ROADMAP #10).
- [ ] Owner, by hand: double-tap the home screen once (gesture-open-settings; adb can't inject
      a double tap here).
- Known, not fixed: grid sliders sometimes apply only on the next reload (PreferenceManager2
      cache refreshed async, `83658ce7eb`); see home-grid spec "Open".
- Parked for last: `feed-bridge` (#3), see its spec.

## emulator (`VinPix_Dev`, API 37, DECISIONS #6)

- Lawnchair debug is the home app; reinstalls can drop the role:
  `adb shell cmd role add-role-holder android.app.role.HOME app.lawnchair.debug`.
- Current state: grid **15 × 22** with "Increase maximum grid size limit" on; Chrome Dino and
  World clock widgets, two folders. Grid can be set by editing `pref_workspaceColumns/Rows` in
  `shared_prefs/com.android.launcher3.prefs.xml` via `run-as` (app stopped). Dummy legacy-icon apps
  `com.dj.dummy.blue/.green/.red`. Drawer launch history seeded (two suggestion rows).
  At a Glance styled green / 50 % / condensed (reset before comparing with upstream).
  AIDL Bridge installed, Show feed on. Folder stack on, footer on, label colour auto.
- Helpers in `.ai/bin/`: `emu-ui` (texts / tap "<text>" / waitfor / shot), `emu-launches`
  (launch apps from the drawer N times), `pred-counts` (decode the predictions DataStore —
  not `strings`), `make-dummy-apps` (build the dummy apps). ffmpeg/ffprobe: see
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

1. **How do we take upstream updates?** Merge `upstream/16-dev` into `dj-dev-16`, or rebase onto
   it. Rebase keeps our changes as a clean stack on top; merge never rewrites pushed history.
2. **Workflows:** 15 deleted `.github/workflows` (`84ab193763`). Delete them again on 16, or
   leave upstream's in place?
