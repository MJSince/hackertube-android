# HackerTube Android

**Version 1.1.0**

Native **Material 3** Android client for [HackerTube](https://hackertube.org/) — the YouTube videos that Hacker News keeps recommending.

This is **not** a WebView shell of the website. It is a Jetpack Compose app that loads the **open HackerTube dataset** (ODC-By 1.0) and presents it with a modern Android UI.

## Features

- **Home** — HN classics strip + mixed feed ranked by lasting recommendations
- **Explore** — 16 topic areas and 150 categories from the official taxonomy
- **Search** — title, channel, tags, topics, and summary
- **Liked / Watch later / History** — stored on-device with DataStore (no accounts)
- **Video detail** — in-app **YouTube privacy-enhanced embed** (same approach as the website), summary, why HN recommends it, pull quote, related talks
- **Open in YouTube** fallback for restricted or unavailable embeds
- **Material 3** — dynamic color (Android 12+), light / dark / system themes, edge-to-edge
- **Offline catalog** — dataset ships inside the APK

## Technology stack

| Piece | Choice |
|--------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min / Target SDK | 26 / 35 |
| Architecture | Single-activity, repository + DataStore |
| Images | Coil |
| Data | Gson + gzipped JSONL assets |
| In-app player | Official YouTube embed via WebView (`youtube-nocookie.com`) |
| CI | GitHub Actions → debug APK artifact |

Package: `org.hackertube.android`

## Dataset

Bundled snapshot from [hackertube.org/data](https://hackertube.org/data), **exported 2026-10-31**:

| Asset | Contents |
|--------|----------|
| `videos.jsonl.gz` | ~6,454 videos (news shelf-life filtered at load) |
| `taxonomy.json` | Areas & categories |
| `related.jsonl` | Co-recommendation graph |

License: **ODC-By 1.0**. Credit HackerTube / the open-index Hacker News archive.
YouTube view counts and durations are **not** redistributed (API terms). Thumbnails use official `i.ytimg.com` URLs.

To refresh the catalog later, replace the assets under `app/src/main/assets/` with a newer export and ship a new build. There is no live HackerTube API endpoint for automatic updates.

## Video playback

- Primary: **official YouTube iframe embed** (privacy-enhanced domain), with fullscreen support via `WebChromeClient`.
- Fallback: **Open in YouTube** button launches the YouTube app or browser.
- The app does **not** scrape streams, bypass ads, DRM, or authentication.

Some videos may be unavailable in certain regions or blocked from embedding by the uploader; use Open in YouTube in those cases.

## Build locally

### Requirements

- Android Studio Ladybug or newer (or JDK 17 + Android SDK 35)
- Network once for Gradle & dependencies

### Commands

```bash
./gradlew assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions

1. Push this project to a GitHub repository.
2. Open **Actions** → workflow **Android CI** (or run **workflow_dispatch**).
3. Download artifact **`hackertube-debug-apk`**.

Workflow file: `.github/workflows/android-build.yml`

## Project layout

```text
hackertube-android/
  app/
    src/main/
      java/org/hackertube/android/
        MainActivity.kt
        HackerTubeApp.kt
        data/          # models, DataStore, repository
        ui/            # theme, components, navigation, screens
      assets/          # videos.jsonl.gz, taxonomy.json, related.jsonl
      res/
  gradle/wrapper/
  .github/workflows/android-build.yml
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
  gradlew / gradlew.bat
  README.md
  LICENSE
  .gitignore
```

## Architecture

- **VideoRepository** — loads gzipped JSONL once on a background dispatcher; caches lists and maps in memory.
- **UserPreferences** — DataStore for likes, watch later, history, theme.
- **Compose navigation** — bottom bar for Home / Explore / Liked / Library / Settings; stack for Search, Category, Video detail.
- **YoutubePlayer** — Compose `AndroidView` wrapping a WebView with the official embed and fullscreen chrome client.

## Known limitations

1. Embeds depend on YouTube availability and uploader embed settings.
2. Semantic search / embeddings from the export are not loaded (keyword search is implemented).
3. Full HN comment threads are not shipped; card quote and "why HN" text are included.
4. Website playlists are not mirrored as first-class screens; categories cover topic browsing.
5. Catalog is a **static snapshot** (2026-10-31).

## Testing report (v1.1)

| Check | Result |
|--------|--------|
| Dataset parse (`videos.jsonl.gz`) | 6,454 valid JSON lines, 0 parse errors |
| Taxonomy / related assets | Present and readable |
| Gradle wrapper JAR | Valid (`GradleWrapperMain` present) |
| Source structure / manifests / workflow | Present |
| Full `assembleDebug` on this machine | **Not run** — Android SDK not installed in the packaging environment |

After extracting the ZIP, run `./gradlew assembleDebug` on a machine with the Android SDK to produce the APK.

## License

App source: provided for this project without warranty.
Dataset: ODC-By 1.0 — credit HackerTube.

Not affiliated with YouTube, Google, Hacker News, Y Combinator, or Andrej Karpathy.
