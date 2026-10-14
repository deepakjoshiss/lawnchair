#!/usr/bin/env bash
# Fact-gathering for the `orient` skill. Emits `key: value` lines only: no judgement, no mutation.
# Must stay fast: never invokes gradle, never fetches. Every field says something; an empty value
# reads as a broken probe.
cd "$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "fatal: not a git repo"; exit 1; }
say() { printf '%s: %s\n' "$1" "$2"; }

say branch       "$(git rev-parse --abbrev-ref HEAD)"
say dirty        "$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')"
say dirty_sample "$(git status --porcelain 2>/dev/null | head -6 | awk '{print $NF}' | tr '\n' ' ')"
say stashes      "$(git stash list 2>/dev/null | wc -l | tr -d ' ')"

# This folder may be a worktree, where .git is a file: ask git for the paths.
gp() { git rev-parse --git-path "$1"; }
if   [ -d "$(gp rebase-merge)" ] || [ -d "$(gp rebase-apply)" ]; then say in_progress REBASE
elif [ -f "$(gp MERGE_HEAD)" ];       then say in_progress MERGE
elif [ -f "$(gp CHERRY_PICK_HEAD)" ]; then say in_progress CHERRY_PICK
else say in_progress none; fi

say submodule "$(git submodule status 2>/dev/null | grep -q '^[-+]' && echo MISMATCH || echo ok)"

# --- upstream (local refs only: as fresh as the last fetch) ---
if git rev-parse --verify -q upstream/16-dev >/dev/null; then
  reviewed=$(awk '/^reviewed-to:/{print $2}' .ai/UPSTREAM.md 2>/dev/null)
  base=$(git merge-base HEAD upstream/16-dev)
  touch=$(git diff --name-only "$base" HEAD -- . ':!.ai' ':!.claude' ':!*.md' | tr '\n' ' ')
  say behind "$(git rev-list --count HEAD..upstream/16-dev)"
  if [ -n "$reviewed" ] && git cat-file -e "$reviewed" 2>/dev/null; then
    if [ -n "$touch" ]; then
      # shellcheck disable=SC2086
      say unreviewed_touchpoint_commits "$(git log --oneline "$reviewed"..upstream/16-dev -- $touch | wc -l | tr -d ' ')"
    else say unreviewed_touchpoint_commits "0 (no touchpoints yet)"; fi
  else say unreviewed_touchpoint_commits "UNKNOWN (reviewed-to missing in .ai/UPSTREAM.md)"; fi
  say upstream_fetched_tip "$(git log -1 --format='%h %cr' upstream/16-dev)"
  say last_review "$(grep -oE '^\| [0-9]{4}-[0-9]{2}-[0-9]{2}' .ai/UPSTREAM.md | tr -d '| ' | sort | tail -1)"
else
  say upstream MISSING
fi

# --- reference checkout ---
REF="$HOME/work/apps/lawnchair-15-ref"
if [ -d "$REF" ]; then say ref_15 "$(git -C "$REF" rev-parse --abbrev-ref HEAD) ($(git -C "$REF" status --porcelain | wc -l | tr -d ' ') modified)"
else say ref_15 MISSING; fi

# --- features ---
say features "$(awk -F'|' '/^\| [0-9]+ \|/{gsub(/ /,"",$6); c[$6]++} END{for(k in c) printf "%s=%d ", k, c[k]}' .ai/ROADMAP.md)"

# --- environment ---
devs=$(adb devices 2>/dev/null | awk 'NR>1 && $2=="device"{print $1}')
dev_list=$(echo $devs); say devices "${dev_list:-none}"
have=""
for d in $devs; do adb -s "$d" shell pm list packages 2>/dev/null | grep -q 'app.lawnchair' && have="$have $d"; done
have=$(echo $have); say lawnchair_on "${have:-none}"
avds=$("$HOME/Library/Android/sdk/emulator/emulator" -list-avds 2>/dev/null | tr '\n' ' ')
say avds "${avds:-none}"
