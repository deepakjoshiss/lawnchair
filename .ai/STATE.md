# Lawnchair state

phase: 1 — porting features to 16 · 10/15 done, 4 dropped, widget-host built · next: owner checks widget-host and the drawer divider on the Pixel; then phase 2 · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the AI setup + ported features. Pushed to
`origin/dj-dev-16` up to `5d0c66fbe7`; the later commits (dock row height, icon label font,
this closeout) are local until the owner asks for a push. Upstream updates: rebase
(DECISIONS #24, UPSTREAM.md).
Done: `gesture-open-settings`, `smartspace-style`, `drawer-look` (sliders only), `drawer-frequent`
(extra suggestion rows), `icon-tweaks`, `folder-look`, `home-grid`, `dock-rows`, `wallpaper-shift`,
`home-text-shadow`; `widget-host` built, unverified. Dropped: `depth-blur`, `dock-poweramp`,
`android16-fix`, `feed-bridge`. New in 16 (no 15 version): dock row height (DECISIONS #26), one
"Icon labels" font for all icon labels (#27), side-by-side release flag (#25).
The 15 implementation is in `~/work/apps/lawnchair-15-ref` (DECISIONS #3). Order: ROADMAP.

## now — NEXT ACTION: owner's device checks, then phase 2

- [ ] **widget-host** (DECISIONS #21): look unverified, the emulator's Google Calendar is not
      signed in (agenda widget shows "Sign in"). Owner checks on the Pixel. Test widget on home
      page 3 of the emulator.
- [ ] **Drawer divider on the Pixel** showed the "All apps" text instead of the line. Upstream
      behaviour, not a bug: the label shows for the first 20 drawer opens
      (`QuickstepOnboardingPrefs`, counted per install) and always while any accessibility
      service is on. If it stays after 20 opens, offer "always the line" (a one-line hook in
      `AppsDividerView`).
- [ ] **Phase 2**: owner to name what comes next; phase 1 ends with the widget-host check.
- Watch: widgets found moved twice with no matching test touch (home-grid spec, "Long-press
      edit mode"). Grid and dock sliders read through PreferenceManager2's cache and can apply
      only on the next reload (`83658ce7eb`).

## emulator (`VinPix_Dev`, API 37, DECISIONS #6)

- Lawnchair debug is the home app; reinstalls can drop the role:
  `adb shell cmd role add-role-holder android.app.role.HOME app.lawnchair.debug`.
- Current state: grid **15 × 22** ("Increase maximum grid size limit" on), horizontal padding
  25 %, vertical 0 % (owner's); World clock, Lawnchair, Chrome Dino (page 2) and Calendar schedule
  (page 3) widgets, folders; dock 5 columns × 2 rows (rows set to 2 in a test; earlier value
  unknown), YouTube dock search bar (owner's). The side-by-side release `app.lawnchair.dj` is
  installed here too (used to reproduce a release-only crash). Grid can be set by editing `pref_workspaceColumns/Rows` in
  `shared_prefs/com.android.launcher3.prefs.xml` via `run-as` (app stopped). Dummy legacy-icon apps
  `com.dj.dummy.blue/.green/.red`. Drawer launch history seeded (two suggestion rows).
  At a Glance styled green / 50 % / condensed (reset before comparing with upstream).
  AIDL Bridge installed, Show feed on. Folder stack on, footer on, label colour auto.
- Helpers in `.ai/bin/`: `emu-ui` (texts / tap "<text>" / waitfor / shot), `emu-launches`
  (launch apps from the drawer N times), `pred-counts` (decode the predictions DataStore —
  not `strings`), `make-dummy-apps` (build the dummy apps), `emu-drag` (long-press drag),
  `emu-slider` (set a settings slider by tapping), `emu-wallpaper-x` (wallpaper offset). ffmpeg/ffprobe: see
  `~/.claude/CLAUDE.md` (not on PATH).
- Owner's Pixel 7 Pro (Android 17) has the side-by-side build `app.lawnchair.dj` ("Lawnchair DJ",
  DECISIONS #25): `./gradlew assembleLawnWithQuickstepGithubRelease -PdjSideBySide
  -Pkotlin.incremental=false` (JDK 21, e.g. Studio's JBR), then `adb install -r` over wireless
  debugging (owner gives IP:port). **Always non-incremental:** an incremental release build lost
  Compose `$stable` fields and crashed on opening settings (2026-10-17; DECISIONS #8).
- Gestures: `adb shell input tap/swipe` work; double tap does not; wait for the dock's "Phone"
  label before gestures. Open the drawer via long-press on home → "Apps list" (a swipe was
  unreliable). Settings deep links: `lawnchair://settings/<route>` (routes in
  `PreferenceRoutes.kt`). Screen recordings: warm up `screenrecord` before acting; it only
  writes frames when the screen changes.

## lessons (sessions 1–2, 2026-10-14/17)

- Bounded waits only (an unbounded `until` hung once; `uiautomator dump` can stall while the
  launcher animates). zsh doesn't word-split `$var` and aborts on unmatched globs — use bash.
  Verify readers before trusting a result (a `strings` read undercounted; a `| head` cut a
  `git grep` short and hid the file that mattered).
- Kotlin must be built non-incrementally, debug and release alike (DECISIONS #8): incremental
  builds produced a crashing debug APK once and a release APK missing Compose `$stable` fields.
  A Gradle task marked UP-TO-DATE can still hold stale output after an interrupted build.
- Emulator input: a drag starting near the left edge is the back gesture — tap sliders instead
  (`emu-slider`). Long-press edit mode shrinks the page: aim at on-screen positions.
- Scratch files vanish on a reboot; keep reusable helpers in `.ai/bin/`.
- 16's folder close has a small (~1.5–3 %) end-of-animation size snap, upstream too; not
  reproduced by the owner after the clip fix (folder-look spec, "Known, not fixed").

## open questions (owner)

None. Upstream updates: rebase (DECISIONS #24, UPSTREAM.md).
