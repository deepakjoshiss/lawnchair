# Lawnchair state

phase: 0 — AI setup done, migration to 16 not started · next: feature #1 spec · branch: dj-dev-16

## where things stand

`dj-dev-16` = upstream `16-dev` @ `85335d904a` + the `.ai/` and `.claude/` setup, uncommitted.
No DJ feature code on 16 yet. The 15 implementation is in `~/work/apps/lawnchair-15-ref`
(DECISIONS #3). Feature list and order: ROADMAP.

## now

- [ ] Create a Lawnchair emulator (only `VinPix_Dev` exists; system images android-35/36/37.1 are
      installed). Needed before any feature can reach `done`.
- [ ] `/port-feature gesture-open-settings` (ROADMAP #1): spec first.

## open questions (owner)

1. **How do we take upstream updates?** Merge `upstream/16-dev` into `dj-dev-16`, or rebase onto
   it. Rebase keeps our changes as a clean stack on top; merge never rewrites pushed history.
2. **Settings carry-over:** keep 15's `dj_*` key names so an in-place update keeps your
   settings, or is a fresh install fine (free to rename)?
3. **Workflows:** 15 deleted `.github/workflows` (`84ab193763`). Delete them again on 16, or
   leave upstream's in place?
