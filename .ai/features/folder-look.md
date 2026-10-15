# folder-look: folder icon and open-folder appearance

status: done (2026-10-15) — A/E/F covered or dropped; B, C, D, G ported (DECISIONS #13)

## On 15 (reference) and verdict on 16

From the scout's read of `dj-dev-15` (commits `c2a8d30c16`, `a1909995c4`, `434d61e4d2`,
`bd53909ff6`, `be82c989c6`; no uncommitted folder edits), each checked against 16 on 2026-10-15.
Note: "Folder rows" in `bd53909ff6`'s message means **dock** rows (`dock-rows`), not folders.

| | 15 change | On 16 | Verdict |
|---|---|---|---|
| A | Folder colour + both opacity sliders applied to the open panel and the home tile (via `DeviceProfile` colour fields) | `Folder` uses `resolveFolderBackgroundColor` + opacity; tile uses `resolveFolderPreviewColor`; spring animation respects the colour (`f2897a16b4`) | covered |
| B | Label colour inside the open folder flips light/dark from the background's luminance (< 0.5 → light) | no adaptation; labels follow the theme only | **owner** — only matters with a custom folder colour |
| C | "Show Footer" toggle (`dj_show_folder_footer`, default on): off hides the folder name and page dots; padding `contentPaddingTop × 1.5` instead | not in 16 | **owner** |
| D | "Stack Icon" (`dj_show_folder_stack_icon`, default **on**): home folder icon shows 3 fanned overlapping cards (scale 0.75) instead of the 2×2 grid | not in 16; 16 still `MAX_NUM_ITEMS_IN_PREVIEW = 4` grid | **owner** — biggest change (layout rule, organizer, preview manager, clip, `FolderInfo`) |
| E | Folder tile and preview clip follow Lawnchair's icon shape | 16 draws from its own "Folder shape" setting (`ThemeManager.getFolderShape()`, stable since `817512351b`) | covered |
| F | Open/close animation: different close interpolator, delay on close only, clip-path changes, `translationZ` hack for stack draw order | 16 enabled the expressive folder expansion by default (`195ffc2de3`) and reworked scrim/height (`dfe60b6729`, `4e617999ff`) | drop; anything the stack icon needs is redone with D |
| G | Spacing: cell x padding 9→14dp, top padding 24→8dp; finer sliders (opacities, label size 0.1→0.05) | 16 still 9dp / 24dp and 0.1 steps | **owner** for spacing; port the finer sliders |

## Evidence

- 16 as it is: closed folder `70-crop.png` (2×2 grid), open folder `71-small.png`
  (name "Google" in the footer).

## Proposal

- Covered/drop: A, E, F.
- Port: G's finer slider steps (Lawnchair layer).
- Owner: D (stack icon), C (footer toggle), B (label contrast), G spacing.
- If D is ported: only active in stack mode (15's `translationZ` hack ran always), fix the
  unbalanced canvas save in `FolderIcon.dispatchDraw`, decide the card order (15 put the 3rd app
  in front: [c,a,b]), keep the 15 key names (DECISIONS #5).

## Owner decisions (2026-10-15)

- D stack icon: port, fix the 15 issues, **app 1 in front**, and keep the open/close animation right.
- C footer toggle: port. B label colour: both **auto** and custom colours. G: port the spacing.

## On 16 (implementation)

- Settings (`PreferenceManager2`): `dj_show_folder_stack_icon` (default on) and
  `dj_show_folder_footer` (default on) keep 15 names; `dj_folder_label_color` is new
  (`ColorOption`, default = "Managed by Lawnchair" = auto). Folders settings: "Stack icon",
  "Show footer", "Label color"; finer steps (0.05) for both opacities and label size.
- `lawnchair/src/app/lawnchair/folder/FolderLook.kt` (new): `showFolderFooter`,
  `isFolderStackIcon` (with a no-Context overload via `LawnchairApp.instance`), `folderLabelColor`
  (chosen colour, else Neutral1_50 / Neutral1_900 by background luminance < 0.5).
- Upstream folder files (`DJ(folder-look)`):
  - `ClippedFolderIconLayoutRule`: stack mode (3 cards, scale 0.75, 15's card geometry,
    mirrored for RTL); `getPosition` → stack for cards, `getPositionFan` (upstream) otherwise;
    `getGridPosition` always from the 2×2 layout; spring animation starts the 3 cards from their
    card positions; `maxPreviewItems()`.
  - `FolderGridOrganizer`: page 0 preview = ranks 0–2 in stack mode (upstream: 2×2 quadrant).
  - `PreviewItemManager.draw`: cards clipped to the folder shape in stack mode (replaces 15's
    outer clip, which left an unbalanced canvas save).
  - `FolderIcon`: drop-into-folder animation uses the real preview count.
  - `FolderPagedView.arrangeChildren`: `translationZ` on ranks 0–2 (app 1 highest) only in
    stack mode, so the opening/closing cards keep the stack's order (15 ran its z hack always).
  - `Folder`: footer hidden + `contentPaddingTop × 1.5` when off; folder name colour.
  - `FolderPagedView.createNewView`: label colour for home-screen folder apps.
  - `res/values/dimens.xml` / `styles.xml`: cell x padding 9→14dp, top padding 24→8dp.
  - `ClipRevealData.kt`: in stack mode the content reveal starts/ends exactly at the folder
    shape (no 12.5 % `EXTRA_FOLDER_REVEAL_RADIUS_PERCENTAGE` margin), so the cards are clipped
    gradually during open/close instead of in one frame at the end (owner report 2026-10-15).
- Not changed: app-drawer folders' label colour (different background).

## Acceptance results (2026-10-15, VinPix_Dev API 37)

1. Stack icon: Google (app 1) front bottom-left, Gmail middle, Maps back, clipped to the
   circle. `72-crop.png`, `82-crop.png`.
2. Open animation (screen recording, every frame): cards leave the stack with Google over Gmail
   over Maps, other apps fade in. `75-open-sheet.png`, zoom `76-open-zoom.png`.
3. Close animation: apps shrink back into the stack in the same order. `75-close-sheet.png`.
4. Stack off → upstream 2×2 grid; footer off → no folder name. `80-off-pair.png`.
5. Label colour: auto gives light labels on the dark folder; custom yellow applies to labels
   and folder name, stored `custom|#ffffbb00`. `81-crop.png`. Reset to auto afterwards.
6. Spacing visibly tighter at the top of the open folder (`80-off-pair.png` vs `71-small.png`).
7. No crashes throughout.
8. Owner saw the cards clipped in one frame at the end of the close. Measured on the strip just
   outside the circle where the back card overflows (mean |diff| vs the final static frame, last
   8 frames): before the fix 47→50→53→54→56→57→56→**0** (snap); after 6.7→4.6→4.6→4.4→3.1→2.9→
   2.7→0 (gradual). `84-close2-zoom.png`.

## Known, not fixed

- End of close: in recordings the cards ended ~3 % larger than the settled stack (front card),
  then snapped. Upstream's own 2×2 grid shows the same, smaller (~1.5 %). Logged targets match
  exactly (animation and preview both 117.75 px, scale 0.6886), so the cause was not found.
  Owner could not reproduce it on the build after the clip fix (2026-10-15); investigation
  stopped, debug logging removed. Revisit if it shows up again.
