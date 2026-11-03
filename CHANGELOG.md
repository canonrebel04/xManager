# Changelog

All notable changes to this fork are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Upstream history before the
fork lives in the git log of [Team-xManager/xManager](https://github.com/Team-xManager/xManager).

## [Unreleased]

Revival of the archived upstream project on the `revive/build-fix` branch.

### Build and dependencies
- Build migrated to Gradle 8.9 and Android Gradle Plugin 8.7.3 (JDK 17), with sources and resources that were missing from the repository added back.
- Target SDK 35: explicit `exported` flags, immutable `PendingIntent`s, and a scoped log broadcast.
- Dependencies updated to the latest versions that still support minSdk 21 (OkHttp 4.12, Glide, Gson, AndroidX, Material). The rippleanimation AAR was replaced with an in-app circular reveal.
- AdMob ads, UMP consent and the rewarded-ads setting removed.

### Platform and security
- Trust-all TLS removed and cleartext traffic blocked.
- `QUERY_ALL_PACKAGES` replaced with `<queries>`. Shared-storage access limited to Android 10 and below.
- Opted out of enforced edge-to-edge on Android 15 for now.

### Fixed
- App versions are compared per numeric segment (5.10 is newer than 5.9).
- HTTP 4xx/5xx responses are treated as request failures, and the update-server URL is configurable, with graceful handling when it's empty or invalid.
- `sortListMap` uses a valid numeric comparator and handles Gson `Double` values.
- Theme and language spinners no longer re-enter their UI update.
- The two German translations have distinct names.
- The launcher icon works on Android 5 to 7.1.
- The splash tagline is picked with a single draw, so every title is equally likely.
- The notification channel has a readable name instead of "Null".
- The header buttons keep 48dp touch targets on narrow (320dp) screens.
- Pending timers are cancelled when the main or splash screen is destroyed, so late callbacks can't touch a closed screen, and a splash closed early no longer opens the main screen (REL-12).
- Installed-version refresh reacts only to the two watched packages and runs once per install instead of once per broadcast (REL-23, REL-24).
- Fewer startup freezes on slow devices: the home screen installs its layout animations right after the first frame instead of during the first layout, and views that draw no shadow skip outline work. Nothing looks different.

### Accessibility
- Icon-only buttons, switches and the stock card have labels, and touch targets are 48dp.
- Section titles are marked as headings, so screen-reader users can jump between sections (A11Y-06).
- With animations turned off (Developer options or "Remove animations"), the screen-switch reveal and the splash fade-in are skipped (A11Y-05).

### Performance and code health
- Installed-app polling was replaced with an event-driven refresh. Timers use fixed-delay scheduling.
- Layouts were flattened, and hard-coded UI text was moved to string resources.
- All Android Lint errors are fixed.
- Debug builds log StrictMode violations (main-thread disk/network access, leaks); release builds are unchanged (PERF-12).

### Tests and CI
- JVM unit tests, Robolectric activity tests, and emulator smoke and Accessibility Test Framework tests run in GitHub Actions on API 30 and API 34.
- CI validates the Gradle wrapper checksum before building (CI-04), and pull requests get a template with a testing checklist (DOC-04).
- A new CI job runs Android lint on every push and pull request and uploads its findings to code scanning (CI-01). GitHub Actions are pinned to commit SHAs (CI-15), and the Gradle build cache is on (CI-03).
- Tests apply every theme and every language and check each one takes effect (TEST-02, TEST-03), and cover the deferred layout animations and outline trim.
