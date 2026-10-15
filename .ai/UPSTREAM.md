# Upstream

`upstream` = LawnchairLauncher/lawnchair, fetch-only, `16-dev` only (DECISIONS #4).
`origin` = deepakjoshiss/lawnchair, our fork.

## Base

```
base: 85335d904a1c8b9790e40c581226684c59166297
reviewed-to: 85335d904a1c8b9790e40c581226684c59166297
```

`base` is the upstream commit `dj-dev-16` currently sits on. `reviewed-to` is the newest upstream
commit whose changes to our touchpoints have been triaged (`/track-upstream`). Both are read by
`.claude/skills/orient/probe.sh`; keep the `key: <full sha>` shape.

## Touchpoints

Not listed by hand, derived on demand:

```bash
git diff --name-only "$(git merge-base HEAD upstream/16-dev)" HEAD   # files we changed
git grep -n 'DJ('                                                     # every marked edit site
```

## Duplicated upstream code (re-check when upstream changes it)

| Our copy | Upstream original |
|---|---|
| `lawnchair/src/com/android/launcher3/icons/DjShadowGenerator.java` | `ShadowGenerator.drawShadow`, `addPathShadow` (submodule) |
| `lawnchair/src/app/lawnchair/icons/DjLegacyIcons.kt` `wrapLegacyIcon` | legacy branch of `BaseIconFactory.normalizeAndWrapToAdaptiveIcon`, `IconPreferences.getWrapperBackgroundColor` (submodule) |

## How we take upstream changes

Not decided yet (STATE open question): merge `upstream/16-dev` into `dj-dev-16`, or rebase
`dj-dev-16` onto it. Until decided, `/track-upstream` triages and reports only.

## Review log

Newest first. Log every review, including when nothing needed action.

| Date | Range | Result |
|---|---|---|
| 2026-10-14 | branch created at `85335d904a` | baseline, no DJ code yet |
