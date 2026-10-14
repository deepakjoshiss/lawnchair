#!/usr/bin/env bash
# Stop: run the gate. Exit 2 hands the failure back to Claude instead of ending the turn.
# Skips when no code changed since the last green run — docs-only and read-only turns would
# otherwise pay for a full Gradle build. Fails open if git is unavailable.
set -u
cd "${CLAUDE_PROJECT_DIR:-$(pwd)}" 2>/dev/null || exit 0

# The hook fires again after Claude responds to us. Without this it never terminates.
grep -q '"stop_hook_active"[[:space:]]*:[[:space:]]*true' <<<"$(cat)" && exit 0
[ -x .ai/bin/check ] || exit 0

# Fingerprint of everything the build can see: HEAD, tracked changes, untracked files' contents,
# and the submodule's own edits. Docs and agent config are excluded.
P=(. ':!.ai' ':!.claude' ':!*.md')
fp=$( { git rev-parse HEAD; git diff HEAD -- "${P[@]}"
        git ls-files -o --exclude-standard -z -- "${P[@]}" | xargs -0 shasum 2>/dev/null
        git submodule foreach --quiet 'git rev-parse HEAD; git diff HEAD'; } 2>/dev/null | shasum | cut -c1-40)
stamp=$(git rev-parse --git-path ai-check-green 2>/dev/null) || exit 0
[ -f "$stamp" ] && [ "$(cat "$stamp")" = "$fp" ] && exit 0

if out=$(.ai/bin/check 2>&1); then echo "$fp" > "$stamp"; exit 0; fi
echo "$out" >&2
echo >&2
echo ".ai/bin/check failed — the work is not done. Fix it, or say plainly why it is being left." >&2
exit 2
