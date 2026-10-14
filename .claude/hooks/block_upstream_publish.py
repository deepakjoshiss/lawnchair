#!/usr/bin/env python3
"""PreToolUse(Bash): never publish to LawnchairLauncher.

`upstream` exists for `git fetch` and `git log`; its push URL is already disabled. This covers the
rest of the surface: pushing by URL, and `gh`, which on a fork defaults to the PARENT repo for
`pr create` — so a `gh pr create` without an explicit `--repo deepakjoshiss/...` is denied too.
Denies outright. Fails open on unparseable input (the push URL stays disabled regardless).
"""
import json, re, sys

# Anchored to command position: start of string, or after ; & | ( or a newline. Without it the
# hook fires on any heredoc or doc that merely mentions these commands.
AT = r'(?:^|[;&|(\n])\s*'
SEG = r'[^|;&\n]*'
# `push` must be the git subcommand (after global options like -C <path>), not a flag such as
# `git remote get-url --push upstream`, which is read-only and was wrongly denied.
GIT_PUSH = r'git(?:\s+-[Cc]\s+\S+|\s+--?[\w-]+(?:=\S+)?)*\s+push\b'
PUSH_UPSTREAM = re.compile(AT + GIT_PUSH + SEG + r'(\bupstream\b|LawnchairLauncher)', re.I)
GH_TO_UPSTREAM = re.compile(AT + r'gh\s+(pr|issue|release|repo)\b' + SEG + r'LawnchairLauncher', re.I)
GH_PR_CREATE = re.compile(AT + r'gh\s+pr\s+create\b(' + SEG + r')')
OWN_REPO = re.compile(r'(--repo|-R)[\s=]+deepakjoshiss/')


def reason_for(cmd: str):
    if PUSH_UPSTREAM.search(cmd):
        return "`upstream` (LawnchairLauncher) is read-only. Push to `origin` (deepakjoshiss/lawnchair), and only when asked."
    if GH_TO_UPSTREAM.search(cmd):
        return "Never open PRs, issues or releases on LawnchairLauncher repos."
    m = GH_PR_CREATE.search(cmd)
    if m and not OWN_REPO.search(m.group(1)):
        return "On a fork, `gh pr create` targets the parent repo by default. Pass `--repo deepakjoshiss/lawnchair`."
    return None


def main() -> None:
    try:
        cmd = (json.load(sys.stdin).get("tool_input") or {}).get("command") or ""
    except Exception:
        return
    reason = reason_for(cmd)
    if reason:
        print(json.dumps({"hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": reason,
        }}))


if __name__ == "__main__":
    main()
