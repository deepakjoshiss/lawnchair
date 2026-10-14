---
name: track-upstream
description: Review what upstream Lawnchair 16-dev changed since our last review — fetch, triage every commit touching files we changed, flag anything that breaks or replaces a DJ feature, and log the review in .ai/UPSTREAM.md. Use when asked to check, sync or catch up with upstream, when /orient reports unreviewed touchpoint commits, or when a month has passed since the last review.
allowed-tools: Bash, Read, Edit, Agent
---

# Track upstream

## Hard limits

- **Never push to `upstream`, never open anything on LawnchairLauncher** (hook-enforced).
- **Never merge or rebase upstream into `dj-dev-16` until the owner has decided how**
  (`.ai/STATE.md` open question 1). Until then this skill triages and reports only.
- Never commit.

## Procedure

1. `git fetch upstream`.
2. Read `reviewed-to:` from `.ai/UPSTREAM.md`. Touchpoints = files changed on our branch:
   `git diff --name-only "$(git merge-base HEAD upstream/16-dev)" HEAD -- . ':!.ai' ':!.claude' ':!*.md'`.
3. `git log --oneline <reviewed-to>..upstream/16-dev -- <touchpoints>`, plus a keyword pass of the
   full log for features still `todo` in ROADMAP (an upstream feature may make ours unnecessary).
4. One line per commit: sha, subject, verdict:
   - **CONFLICT**: changes code at a `DJ(` site or a hook we rely on. Name the feature.
   - **OVERLAP**: upstream now does what a DJ feature does → candidate to drop ours.
   - **WATCH**: reshapes a file we touch (split, rename, move) without breaking us yet.
   - **IGNORE**: everything else.
5. **Loudest:** an upstream file we touch was deleted or renamed, or the logic moved elsewhere.
6. Show the triage; get agreement before acting on any of it.
7. Append a Review log row to `.ai/UPSTREAM.md` and bump `reviewed-to:` to the reviewed tip,
   **including when nothing needed action**.
