# smartspace-style: At a Glance text colour, font and opacity

status: done (2026-10-14)

## What the user sees

Settings → At a Glance (Lawnchair's own At a Glance, not Smartspacer mode): a style group right
under the preview, before "What to show", with three settings:

- **Text colour**: the colour picker used elsewhere (dynamic colours plus "default"). Applies
  to the card's title, date, subtitle, action subtitle and icon tint. "Default" = the normal home
  screen text colour, as today.
- **Opacity**: slider 0–100 % in 5 % steps. Fades each At a Glance card as a whole.
- **Font**: the font picker used elsewhere. Applies to the title, date and subtitle.

Defaults leave At a Glance looking exactly like upstream. A change shows on the home screen right
away (the launcher recreates itself on change). Smartspacer mode is untouched.

## Settings

Names kept from 15 (DECISIONS #5: same effect, same name).

| key | store | type | default | control |
|---|---|---|---|---|
| `dj_ss_font_color` | PreferenceManager2 | colour option string | `default` (theme text colour) | colour picker |
| `dj_ss_widget_opacity` | PreferenceManager2 | float 0..1 | 1.0 | slider, step 0.05, shown as % |
| `dj_ss_font_pref` | PreferenceManager (prefs1) | font | system UI text font | font picker |

## On 15 (reference)

- Commit `e5ed825962` (2024-08-25, "SmartSpacer customisation and improvements"). Despite the
  name it styles Lawnchair's built-in At a Glance only; Smartspacer's views are untouched.
  `bd53909ff6` only added an unused import.
- Hook: `BcSmartspaceCard.onFinishInflate()` reads the three settings (`firstBlocking`) and sets
  card `alpha`, the font on three text views (`FontManager.setCustomFont(…, R.id.font_smart_spacer)`),
  and the text colour (`colorPreferenceEntry.lightColor`; `0` means default →
  `workspaceTextColor`).
- `CardPagerAdapter.onBindViewHolder`: the theme-colour line was commented out so it would not
  overwrite our colour on every bind.
- Settings UI: `SmartspaceStylePreferences()` in `SmartspacePreferences.kt`; colour entry in
  `ColorPreferenceModelList.kt`; font id in `FontManager.kt` and `res/values/id.xml`.
- Quirks: colour uses `lightColor` only (text sits on the wallpaper, not on an app theme
  surface, so that is right); opacity default borrowed a folder dimen.

### Also in that commit, not this feature

- **Layout compaction**: At a Glance height 104dp → 92dp, page indicator padding 8dp → 4dp, a
  hard-coded 16dp left padding in `SmartspaceViewContainer` (replacing the device-profile
  padding), 16dp side padding on the home-screen widget layout.
- **Lighter text shadow on the whole home screen**: `workspaceAmbientShadowColor` `#40000000` →
  `#10000000` in `res/values/styles.xml`.
- Calendar widget alpha hack in `NavigableAppWidgetHostView.java` → belongs to `widget-host`.

## On 16

**Overlap: none for the three settings.** 16 has no At a Glance colour, font or opacity setting
(checked `PreferenceManager2` keys and `SmartspacePreferences.kt`, 2026-10-14). Upstream added
automatic wallpaper-based text colour and then reverted it for contrast problems (`e614c19a4e`).
The revert message suggests "a future option to manually change the text color", which is
this feature.

**Overlap for the layout compaction: likely.** Upstream retuned At a Glance since our base: new
page indicator (`2bd3a2a76d`), new dimensions (`e1cdc78434`), margins matched to the reference
design (`33432f46ab`). On 16 the indicator padding is 10dp (15 had 8dp before our change) and
the container uses the device-profile padding again.

Hook points on 16 (same shape as 15):

- `BcSmartspaceCard.onFinishInflate()`: apply font and opacity.
- Colour: `CardPagerAdapter` already applies one colour per bind (`currentTextColor`); compute it
  from our setting there instead of commenting the line out, so there is one place that sets it.
- Settings UI, preference keys, font id, colour model: Lawnchair's layer only. No AOSP edits.

## Acceptance

On `VinPix_Dev`, debug build from a green `.ai/bin/check`. Before building, take a baseline
screenshot of the home screen's At a Glance (upstream look).

1. Defaults: At a Glance matches the baseline screenshot. Evidence: side-by-side screenshots.
2. Settings → At a Glance shows the group (Text colour, Opacity, Font) under the preview.
   Evidence: screenshot.
3. Text colour set to a distinct colour → title, date and icon on the home screen take it.
   Evidence: screenshot.
4. Opacity 50 % → the card is visibly faded; the page indicator is not. Evidence: screenshot.
5. Font changed → card text uses it. Evidence: screenshot.
6. Smartspacer mode: the group is not shown. Evidence: screenshot of its settings.
7. Stored keys are `dj_ss_font_color`, `dj_ss_widget_opacity` (datastore) and `dj_ss_font_pref`
   (shared prefs). Evidence: `run-as` read through `strings`.

## Improvements over 15

- Colour applied in the adapter (one place) instead of disabling the adapter's line.
- Opacity default is its own value (1.0), not a borrowed folder dimen.
- Settings recreate the launcher on change (`recreate`, as 16's own font settings do) instead of
  `reloadGrid`: on 16 a grid reload does not re-inflate At a Glance, so 15's approach left the
  old style on screen until a restart (measured: colour saved, text stayed white until restart).
- Labels in 16's sentence case: "Text colour" (reuses the existing home-screen string), "Opacity", "Font".

## Owner decisions

1. **Layout compaction: not ported** (owner, 2026-10-14; DECISIONS #7). 16's own sizes stand.
2. **Lighter home-screen text shadow**: its own roadmap item, `home-text-shadow` (owner, 2026-10-14). It was ours (`#40000000` → `#10000000` on
   `workspaceAmbientShadowColor`, light-text theme only); upstream 16 still has `#40000000`,
   unchanged since our base.

## Acceptance results (2026-10-14, VinPix_Dev API 37)

1. Pass: At a Glance strip at defaults vs the pre-build baseline, SSIM 1.000000
   (control: baseline vs a settings screen 0.25). `10-glance-baseline.png`, `11-glance-default.png`.
2. Pass: Style group (Text color, Opacity 100%, Font) above "What to show". `12-glance-settings.png`.
3. Pass, live: yellow then green without restart; title pixel `e2e3e9` → `e1c478` → `99c6a5`;
   stored `custom|#ff47b84f`. `15-…`, `16-glance-green-live.png`. No icon on this card, so tint unseen.
4. Pass: 50 % → title `99c6a5` → `bed5cb` (faded), page indicator stays `ffffff`. `18-glance-50pct.png`.
5. Pass: sans-serif-condensed applied, visibly narrower. `20-font-compare.png`.
6. Code only: the group is called only inside `LawnchairSmartspaceSettings`; Smartspacer mode needs
   the Smartspacer app, not installed on the emulator.
7. Pass: datastore holds `dj_ss_font_color`, `dj_ss_widget_opacity`; shared prefs hold `dj_ss_font_pref`.

Found on the way: an incremental build produced an APK that crashed at start
(`NoSuchFieldError $stable` on `BasePreferenceManager$StringPref`) while the gate was green; a full
Kotlin recompile fixed it. The gate now builds Kotlin non-incrementally (DECISIONS #8).
