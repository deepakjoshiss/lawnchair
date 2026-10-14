#!/usr/bin/env bash
# SessionStart: a short live tripwire, not an orientation. /orient does the real work.
set -u
cd "${CLAUDE_PROJECT_DIR:-$(pwd)}" 2>/dev/null || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0

dirty=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
phase=$(grep -m1 '^phase:' .ai/STATE.md 2>/dev/null || echo "phase: unknown")
behind=$(git rev-list --count HEAD..upstream/16-dev 2>/dev/null || echo "?")

git remote get-url upstream >/dev/null 2>&1 || echo "!! no 'upstream' remote — see .ai/UPSTREAM.md"
[ -d "$HOME/work/apps/lawnchair-15-ref" ] || echo "!! ~/work/apps/lawnchair-15-ref is missing — the 15 reference (DECISIONS #3)"
git submodule status 2>/dev/null | grep -q '^[-+]' && echo "!! submodule not at the recorded commit — git submodule update --init"

echo "Lawnchair | branch $(git rev-parse --abbrev-ref HEAD) | $dirty modified | $behind behind upstream/16-dev (last fetch)"
echo "$phase  —  /orient for the full picture"
