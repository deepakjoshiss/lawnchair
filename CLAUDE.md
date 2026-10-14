# Lawnchair (DJ fork)

A personal fork of [Lawnchair](https://github.com/LawnchairLauncher/lawnchair) (`origin` =
`deepakjoshiss/lawnchair`). Our features ride on top of upstream's `16-dev`, on branch `dj-dev-16`.
This repo is AI-first: the owner writes little or no code, so the docs below and `.ai/bin/check`
are the review.

Where we stand right now: [`.ai/STATE.md`](.ai/STATE.md) — or run `/orient`.
The plan and the feature list: [`.ai/ROADMAP.md`](.ai/ROADMAP.md).

## Ground rules

1. **Never `git commit` and never `git push` unless asked for that specific action.** "Go ahead",
   "continue" and an approved plan authorise the work, never the commit. Permission covers one
   commit and expires when it lands. Prose, not a hook.
2. **`origin` (our fork, `deepakjoshiss/lawnchair`) is the main repo and the only push target.**
   The base repo (`upstream`, `LawnchairLauncher/lawnchair`) is read-only for us: never push to
   it, never open a PR, issue or release on any `LawnchairLauncher/*` repo. PRs go to our fork
   with `--repo deepakjoshiss/lawnchair`. Enforced: `upstream`'s push URL is disabled and
   `.claude/hooks/block_upstream_publish.py` denies the commands.
3. **Never edit `~/work/apps/lawnchair-15-ref`.** It is the old implementation (`dj-dev-15`, with
   uncommitted changes that are part of it), read through `lawnchair-scout`. Prose, not a hook.
4. `.ai/bin/check` is the gate. It runs on Stop; if it fails, the work is not done.

@.ai/RULES.md
