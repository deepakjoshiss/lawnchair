# Feature specs

One file per ROADMAP feature: `<id>.md`. Written before any 16 code, from the 15 implementation
(via `lawnchair-scout`) and the owner's intent. It describes behaviour, not code, so it outlives
any one implementation.

## Template

```markdown
# <id>: <one-line name>

status: spec | wip | done | dropped | obsolete

## What the user sees
What it does, where the setting lives, defaults. Behaviour only.

## Settings
| key | type | default | where in Settings |

## On 15 (reference)
Commits (sha, subject). Files and the hook points used, one line each.
Quirks found: odd branches and why (commit sha).

## On 16
Overlap: does upstream 16 already do this, fully or partly? → port / adapt / drop.
Hook points chosen, and which upstream files get a `DJ(<id>)` edit.

## Acceptance
Numbered checks on the emulator; each says what evidence proves it (screenshot, recording,
adb output).

## Improvements
What changes compared with 15, and why.
```
