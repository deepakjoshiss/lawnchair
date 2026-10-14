# Decisions

Short ADR log. **Append-only**; never renumber, never delete. Each entry: decision, why, and what
it costs. A reversed decision gets a *new* entry saying so. Cite from code as `DECISIONS #N`.

Next entry: **#7**.


## 1. Base is upstream `16-dev`

**Decided (2026-10-14):** our features move onto LawnchairLauncher/lawnchair `16-dev` (upstream's
default branch, active daily) on branch `dj-dev-16`. `15-dev` and `14-dev` are left behind.

**Why:** 15-dev is no longer where upstream works; staying there means porting their fixes by
hand forever.

**Cost:** a one-time re-implementation of every feature (ROADMAP).

## 2. Port by spec, not by cherry-pick

**Decided (2026-10-14):** each feature is described in `.ai/features/<id>.md` from the 15
implementation, then written fresh against 16. No `git cherry-pick` or rebase of `dj-dev-15`.

**Why:** measured 2026-10-14: between our 15 base `ccb2f7b83b` and upstream `16-dev`
`85335d904a`, upstream rewrote most files we touch (e.g. `DeviceProfile.java` 1,740 changed lines,
`Workspace.java` 1,352) and deleted two (`GridSizeMigrationUtil.java`,
`LauncherPreviewRenderer.java`). Replayed hunks would land in code that no longer means the same
thing.

**Cost:** slower per feature than a cherry-pick that happens to apply; offset by fewer upstream
edits afterwards.

## 3. Two checkouts: `dj-dev-16` here, `dj-dev-15` as a read-only worktree

**Decided (2026-10-14):** this folder is `dj-dev-16`. `dj-dev-15` lives in the worktree
`~/work/apps/lawnchair-15-ref`, with its 8 uncommitted edits (3 app files, 5 in the systemui
submodule) moved there intact, verified byte-identical against patches taken before the move.

**Why:** the uncommitted edits are part of the old features and the owner wants them kept as-is;
the main folder is where the future work is.

**Cost:** two working trees share one `.git`; a branch can only be checked out in one of them.
The pre-move stashes (`move-to-15-ref`, in this repo and in the submodule's git dir) were kept as a
spare copy.

## 4. `upstream` remote is fetch-only, `16-dev` only, no tags

**Decided (2026-10-14):** `upstream` = `https://github.com/LawnchairLauncher/lawnchair.git`, push
URL `DISABLED-read-only-remote`, refspec limited to `16-dev`, `tagOpt --no-tags`. The fork's own
`origin/16-dev` mirror is not used as a base (it was 97 commits behind on 2026-10-14).

**Why:** reading upstream must never become publishing to it; syncing the mirror would be a push.

**Cost:** none.

## 5. Settings keys: same effect keeps the same name

**Decided (2026-10-14, owner):** a setting ported from 15 keeps its 15 key name when it has the
same effect on 16. If its meaning, type or range changes, it gets a new name.

**Why:** an in-place update from the 15 build keeps the owner's settings wherever that is safe;
a changed meaning under an old name would read stale values as the wrong thing.

**Cost:** some 15 names stay that don't follow the `dj_` prefix (e.g. `pref_hotseat_rows`).

## 6. Test emulator is `VinPix_Dev`

**Decided (2026-10-14, owner):** Lawnchair is tested on the existing `VinPix_Dev` emulator.
VinPix pauses its use of it meanwhile.

**Why:** no second emulator to create or maintain.

**Cost:** a Lawnchair debug build installed there can take over the home screen; if VinPix
resumes, set its home app back or split the emulators.
