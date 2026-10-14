---
name: orient
description: Establish where the Lawnchair fork stands — phase and in-flight work, feature progress, how far behind upstream 16-dev we are and whether upstream touched our files, an interrupted git operation, and broken preconditions (reference checkout, submodule, emulator). Use at the start of a session, after switching branches, after time away, or when asked "where are we", "what's next", "am I behind upstream". Not when a concrete task is already named — just do the task.
allowed-tools: Bash(bash .claude/skills/orient/probe.sh), Read
---

# Orient

Fast and cheap: **two tool calls**, no source files, no builds, no fetch.

1. `bash .claude/skills/orient/probe.sh`: `key: value` lines, about a second.
2. Read `.ai/STATE.md` in full (capped at 150 lines).
3. Read nothing else. Print the report, then stop and wait.

Fetching is `/track-upstream`'s job; `upstream_fetched_tip` tells you how stale the answer is.

## Report

Under 15 lines, this shape:

```
LAWNCHAIR ORIENT
branch      <branch> (<n> modified, <n> stashed)
git         <clean | REBASE/MERGE/CHERRY_PICK IN PROGRESS>
phase       <from STATE.md>
features    <counts by status>
in flight   <unchecked items from STATE "now">
upstream    <n> behind; <n> unreviewed commits on our touchpoints; fetched tip <sha> <age>
reference   lawnchair-15-ref <branch> (<n> modified) | MISSING
env         devices <ids|none>; lawnchair on <ids|none>; avds <names>
open        <one line per open question in STATE>

NEXT  1. <most important concrete action>
      2. <next>
```

## Deriving NEXT

Normally: the first unchecked item in STATE's **now**. These override it, in order:

1. `in_progress` ≠ `none` → finish or abort it first; say nothing else.
2. `submodule: MISMATCH` → `git submodule update --init` before any build.
3. `ref_15: MISSING` → stop; the 15 reference is gone (DECISIONS #3). Ask the owner.
4. `upstream: MISSING` → re-add it per `.ai/UPSTREAM.md` / DECISIONS #4.
5. `unreviewed_touchpoint_commits` > 0, or `last_review` over a month old → `/track-upstream`.
6. An open question in STATE that blocks the next item → ask it.
