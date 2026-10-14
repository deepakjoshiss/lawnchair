---
name: port-feature
description: Move one DJ feature from the 15 implementation (~/work/apps/lawnchair-15-ref) onto upstream 16 — write its spec, check whether upstream 16 already covers it, re-implement with the fewest upstream edits, verify on the emulator, and record the result. Use when asked to port, migrate, move or rebuild a feature by ROADMAP id or name ("port dock-rows", "next feature", "/port-feature"). Not for upstream updates (/track-upstream) or brand-new features with no 15 version.
---

# Port a feature

One feature per run. Argument: a ROADMAP id. Stop and show the owner at each **⏸**.

## 1. Spec

If `.ai/features/<id>.md` is missing or still `status: todo`:

- Ask `lawnchair-scout` for the feature's 15 implementation: commits, files, hook points, settings
  keys and defaults, odd branches and the commit that added each. Include uncommitted edits in
  the reference folder and its submodule (`git -C ... diff`).
- Write the spec from `.ai/features/README.md`'s template. Behaviour, not code.
- **⏸** Show the spec: what the user sees, settings, acceptance checks. The owner confirms intent
  and names any improvements. This is the one place their judgement shapes the feature.

## 2. Overlap with 16

Search upstream 16 for the same behaviour (settings screens, `PreferenceManager2`, commit log by
keyword). Record the verdict in the spec's **On 16** section:

- **covered** → status `dropped`, a DECISIONS entry saying what replaces it. Done.
- **partly** → build only the gap, on top of upstream's version.
- **none** → full port.

## 3. Implement against 16

- Read the 16 code you will hook into **first**; the 15 hook point may not exist or may mean
  something else now.
- Logic in `lawnchair/` or new files; AOSP files get the smallest possible hook, each marked
  `// DJ(<id>): <why>` (RULES.md).
- Carry over odd branches from 15 unless 16 made them unnecessary. Never drop one you can't explain;
  find its commit (`git -C ~/work/apps/lawnchair-15-ref log -S '<constant>'`).
- Settings keys: same effect keeps the 15 name, changed meaning gets a new one (DECISIONS #5).
- Submodule edits (`icon-tweaks`): the submodule is upstream's repo. Note in the spec how they are
  carried, and ask before committing anything inside it.

## 4. Verify

- `.ai/bin/check` green.
- Install that APK on the emulator (`adb -s <emulator> install -r`), never a physical device.
- Run each acceptance check; evidence into `.ai/shots/`. **⏸** when a check is about look or feel:
  show the screenshot and wait.

## 5. Record

- Spec `status`, ROADMAP status, STATE rewritten to the next item.
- DECISIONS entry for anything dropped, changed in behaviour, or deliberately not carried.
- Report what is uncommitted and propose a commit message. Never commit unasked.
