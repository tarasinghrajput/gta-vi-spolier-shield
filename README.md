# Spoiler Shield

An experimental Android app that helps you avoid **GTA VI spoilers** while scrolling Instagram and YouTube. When it spots matching text in a post or video card, it covers that item with an opaque overlay. You decide whether to reveal it.

> **Status: unvalidated prototype.** It is not dependable spoiler prevention. Read [Limitations](#limitations) before relying on it.

## Who is this for?

- **GTA VI fans who want to go in blind**: you want the story, endings and character fates to stay a surprise, but you still use Instagram or YouTube.
- **Anyone who wants a self-imposed content filter**: Shield can also block whole apps behind a cover while you focus.
- **Android users comfortable sideloading an APK and building from source**: there is no Play Store release yet.

It is built for **personal use** on **Android 12 or newer**. It is not a parental-control or device-management tool. It has no uninstall protection and no device-owner privileges, and you can turn it off at any time.

## Features

Shield has three protection modes. Pick one in the app.

| Mode | What it does | Best for |
|---|---|---|
| **All GTA VI content** | Covers any item whose text names GTA VI, GTA 6, GTA6 or Grand Theft Auto VI/6/Six | Avoiding the game entirely until release |
| **Potential spoilers only** (default) | Covers items that match spoiler phrases such as *who dies*, *secret ending*, *final mission*, *leak* and *walkthrough*, plus a Hindi/Hinglish phrase set | Seeing normal GTA VI news but not story details |
| **Block apps completely** | Puts a full-screen cover over the apps you select, with **Go home** and **Turn protection off** buttons | A strict break from specific apps |

Other behavior:

- **Reveal / Hide again**: tap Reveal on a cover to view the item. The reveal lasts until you scroll, navigate away or the item changes.
- **YouTube is opt-in**: Instagram is covered by default. Turn on **Filter YouTube too** to inspect YouTube titles and descriptions.
- **Smart matching**: ignores capitalization and punctuation, and recognizes variants such as `G.T.A. 6`, `GTA Six` and hashtags. GTA V, GTA VII and GTA 60 are excluded.
- **Auto-pause**: if a covered video exposes a Pause control, Shield tries to press it once. It never mutes your device or resumes playback.
- **Privacy**: no network permission, no analytics, no screenshots, and no screen text stored. All matching happens on the phone.

## Install

### Requirements
- JDK 17
- Android SDK 35 (set `ANDROID_HOME`)
- An Android 12+ phone (API 31+) with USB debugging enabled

### Build and install
```bash
git clone https://github.com/tarasinghrajput/gta-vi-spolier-shield.git
cd gta-vi-spolier-shield
./gradlew testDebugUnitTest lintDebug assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## How to use

1. **Open Spoiler Shield** and read the disclosure.
2. Tap **I agree to enable Shield**.
3. **Choose a protection mode.**
   - For *Block apps completely*, tap **Select social apps** for a suggested list, or **Show other installed apps** and search. Nothing is selected automatically. Your launcher, calling apps, default SMS app and essential system apps are excluded.
4. *(Optional)* Switch on **Filter YouTube too**.
5. Tap **Open Accessibility settings** and enable **Spoiler Shield**.
   - If Android blocks it for a sideloaded app, open *Settings → Apps → Spoiler Shield → ⋮ → **Allow restricted settings***, then try again.
6. Return to Shield and tap **Enable protection**.
7. Open Instagram or YouTube. Matching items are covered.

**To stop:** turn protection off in the app, or tap **Revoke consent**. All covers disappear immediately. You can also disable the accessibility service in Android settings.

## Limitations

- **Text only.** Shield reads accessibility text (captions, titles, descriptions). It does **not** analyze images, video frames, audio or subtitles, so spoken or visual spoilers get through.
- **Layout-dependent.** Only recognized Instagram feed, photo, video and reel containers and YouTube cards or Shorts are candidates. App updates can change these, and unsupported layouts stay visible.
- **Not instant.** Content can flash before it is covered. During scrolling, covers are removed and re-applied after a short pause (about 140 ms).
- **Can over-block.** Phrases like *who dies* match unrelated games and videos. Gameplay and walkthrough terms are deliberately conservative.
- **Audio may continue** if the app doesn't expose a Pause control.
- **Not covered:** notifications, direct messages, browser versions of the sites, other Android profiles.
- **Whole-app blocking** is an overlay, not a package suspension. It doesn't stop the app from running.

Physical-device testing is still pending. The matrix below is **NOT RUN**. Record your phone, Android version and app version before claiming support.

<details>
<summary>Physical-device acceptance matrix (all NOT RUN)</summary>

- Instagram feed photo/video: a configured phrase is covered, unrelated content is not.
- Reels: a text-bearing reel is covered. Document whether caption and media share the recognized container.
- YouTube home, search, Shorts and watch page; comment fields and account/settings screens left alone.
- Rapid scrolling, reused nodes and multiple visible posts: no stale or misplaced cover.
- Reveal and Hide again: a reveal never carries over to another item.
- Direct messages, login and settings: not inspected.
- Rotation, keyboard, recents and app switching: no cover over another app or Android Settings.
- Permission revocation, process restart, reboot: the health indicator is accurate.
- Off switch and consent revocation: covers disappear at once.
- Whole-app blocking: app launch, deep links, recents, Go home, mode changes while enabled, and reinstall of selected apps.

If container recognition fails, record that surface as unsupported. Don't widen detection to arbitrary ancestors, because that could cover unrelated UI.
</details>

## How matching works

Matching is heuristic. Rockstar's [posting policy](https://support.rockstargames.com/articles/7bNaeoMFTV0iUDGhStTXvz/policy-on-posting-copyrighted-rockstar-games-material) names endings, cutscenes, plot dialogue and major reveals as spoiler material, and the phrase lists follow that guidance. They are warning terms, not verified plot information.

- **Original 34 phrases** are always active in spoiler mode.
- **Expanded cues** (endings, story reveals, character outcomes, missions, leaks, Hindi cues) apply only when a GTA VI alias or *Jason* / *Lucia* appears in the same text.
- Negations such as "no spoilers" don't disable a match.
- Character names can refer to unrelated people.

Phrase lists live in `SpoilerRules.kt`; the alias and context logic is in `KeywordMatcher.kt`.

## Performance notes

Accessibility events are throttled in the service. One bounded snapshot traversal runs off the UI thread, limited to 350 nodes and about 120 ms. Partial trees are reported as partial protection. Detection latency has not been measured on a phone.

## Project layout

```
app/src/main/java/dev/spoilershield/
  MainActivity.kt        Compose UI: consent, modes, app picker
  ShieldService.kt       Accessibility service
  InstagramAdapter.kt    Instagram container recognition
  YouTubeAdapter.kt      YouTube container recognition
  KeywordMatcher.kt      Matching logic
  SpoilerRules.kt        Phrase lists
  TextNormalizer.kt      Alias and Unicode normalization
  OverlayController.kt   Cover drawing
  CoverSession.kt        Reveal / hide state
  ProtectionMode.kt      Mode definitions
app/src/test/            Unit tests (matcher, policy, cover session)
```

Run the tests with `./gradlew testDebugUnitTest`.

## Disclaimer

Spoiler Shield is an independent hobby project. It is not affiliated with Rockstar Games, Take-Two Interactive, Meta or Google. Grand Theft Auto and GTA are trademarks of their respective owners.
