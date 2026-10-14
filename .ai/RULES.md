# Rules

The only always-in-context doc. Everything else is read on demand.

## What this repo is

Upstream Lawnchair (`16-dev`) plus our features, kept as a thin layer. Lawnchair is itself
AOSP Launcher3 (`src/`, `quickstep/`, `res/`) plus its own layer (`lawnchair/`) and a submodule
(`platform_frameworks_libs_systemui`, icon loading). Every line we change in upstream code is a
line we re-resolve on every upstream update, so **the fewer upstream edits, the better the
feature**.

## Which doc, when

| Doc | Read it when |
|---|---|
| `.ai/STATE.md` | start of session. **NOW only**, hard cap 150 lines, rewritten never appended |
| `.ai/ROADMAP.md` | you need the plan, the feature list, or a feature's status |
| `.ai/features/<id>.md` | working on that feature. What it does, settings, acceptance checks |
| `.ai/DECISIONS.md` | a choice looks arbitrary. Numbered, append-only, cited from code as `DECISIONS #N` |
| `.ai/UPSTREAM.md` | updating from upstream, or checking what upstream changed under us |
| `.ai/journal/` | archaeology only. Closed phases; never loaded by default |

When `STATE.md` nears 150 lines, move the closed part to `.ai/journal/<phase>.md` and cut.
`DECISIONS.md` is never renumbered; a reversal is a new entry.

## Checkouts

| Path | Branch | Use |
|---|---|---|
| this folder | `dj-dev-16` | all work |
| `~/work/apps/lawnchair-15-ref` | `dj-dev-15` + uncommitted edits (incl. in its submodule) | read-only reference, via `lawnchair-scout` |

Both share one `.git`. Never `git switch` either folder to the other's branch, and never
`git stash`/`reset` in the reference folder — its uncommitted edits are part of the old features.

## Code constraints

- **Mark every edit to upstream code** (anything outside files we created):
  `// DJ(<feature-id>): <why>` in Java/Kotlin, `<!-- DJ(<feature-id>): <why> -->` in XML.
  Upstream's own markers are `LC-Note` / `LC-Ignored`; ours stay distinct so
  `git grep 'DJ('` lists every touchpoint.
- **Prefer the Lawnchair layer.** Put logic in `lawnchair/` (or a new file) and call it from a
  one-line hook in AOSP code, rather than editing AOSP methods in place.
- **New settings keys** are prefixed `dj_` and live in `PreferenceManager2` unless the feature
  spec says otherwise.
- **Port behaviour, not diffs.** The 15 implementation is a reference for what the feature does
  and which edge cases it handles; the 16 code is written against 16. See `/port-feature`.
- **Formatting** is upstream's: `spotlessCheck` (ktlint for Kotlin) is part of the gate.
- Release signing files (`keystore.properties`, `*.jks`) are secrets: never read or print them.

## Evidence

- **Compiling is not done.** A feature is done when its spec's acceptance checks pass on the
  emulator, with a screenshot or screen recording as evidence.
- **An empty or identical result is suspect** until a positive control shows the check can fail.
- **A failed build leaves the last APK in place.** Install only after a green build in the same
  run.
- Screenshots, recordings and frame dumps go in `.ai/shots/` (git-ignored). Device side:
  `/data/local/tmp`. Empty it at the end of a session.
- Test on the **emulator** only, never the owner's phone.

## The gate

`.ai/bin/check`: `spotlessCheck` + `assembleLawnWithQuickstepGithubDebug`. Needs JDK 21+;
the script uses Android Studio's bundled JBR when the default `java` is older. It runs from a
`Stop` hook, which skips when nothing outside `.ai/`, `.claude/` and `*.md` changed since the
last green run. A check waits for any running check or `./gradlew` here; it cannot see Android
Studio builds, so don't build from Studio while an agent session is working — concurrent builds
corrupt the KSP caches.
