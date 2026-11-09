# xManager enhancement backlog

> **Excluded on purpose.** This copy deliberately leaves out every item about downloading, installing, patching or resuming downloads of APKs (the whole former "Download & install flow" section and related items elsewhere: DL-01, DL-02, DL-03, DL-04, DL-05, DL-06, DL-07, DL-08, DL-09, DL-10, DL-11, DL-12, DL-13, DL-14, DL-15, DL-16, DL-17, DL-18, DL-19, DL-20, DOC-20, NOT-04, NOT-05, NOT-06, SEC-11, SET-11, SET-12, TEST-14, UI-12, UI-20, UPD-02, UPD-15), and anything that would unlock Spotify Premium or bypass its protections. Those are out of scope for this fork; 32 items were removed from the original 258.

**226 distinct items** across 13 categories, reviewed against `revive/build-fix` @ `de64cbe` (xManager Dev's latest) and the findings in [TEST_REPORT.md](TEST_REPORT.md). The machine-readable copy is [ENHANCEMENTS.csv](ENHANCEMENTS.csv); keep the two files in sync by hand.

**Scope:** this backlog covers the manager app only (UI, reliability, build, tests, docs, update notices). By design it contains nothing that unlocks Spotify Premium features, touches ads, DRM, licensing or account checks, or evades detection.

**Legend:** effort S < ½ day, M ≈ 1–3 days, L ≈ a week or more. P0 = fix now (bugs, broken platform behaviour), P1 = next release, P2 = soon, P3 = nice to have.

| Priority | Count | | Effort | Count |
|---|---|---|---|---|
| P0 | 16 | | S | 181 |
| P1 | 62 | | M | 40 |
| P2 | 84 | | L | 5 |
| P3 | 64 | |  |  |

## Top 20 quick wins

Small-effort P0/P1 items, with the bugs from TEST_REPORT listed first.

| # | ID | Pri | Title | Why |
|---|---|---|---|---|
| 1 | A11Y-01 | P0 | Label the Settings and Experiments switches | The UI dump shows all five Switch widgets unlabelled, so TalkBack reads 'Switch, off' with no context. The icon buttons were already fixed in b29202e (TEST_REPORT row 17). |
| 2 | REL-02 | P0 | Semantic version comparison | 5.10 is treated as older than 5.9, which wrongly triggers the maintenance screen, and '5.9.1' crashes the parse (TEST_REPORT #2). |
| 3 | REL-03 | P0 | Handle HTTP error status codes | Bug: a 503 HTML error page is fed to Gson and throws JsonSyntaxException (TEST_REPORT bug #6, MockWebServer test). |
| 4 | REL-06 | P0 | Configurable/non-empty API URL | Bug: api_url is empty in res/values/api.xml, so every list shows N/A with no explanation (TEST_REPORT bug #7, emulator row 3). |
| 5 | UI-08 | P0 | Edge-to-edge and window insets | On Android 15 with targetSdk 35 content draws under the status and navigation bars. |
| 6 | UI-16 | P0 | Empty and error states | With an unreachable or empty API URL the main screen just stays blank. |
| 7 | REL-04 | P0 | Guard JSON parsing | A broken API response currently crashes the app via the global handler. |
| 8 | REL-05 | P0 | Null-safe map access | A missing key causes a NullPointerException with .toString(). |
| 9 | REL-07 | P0 | Report onErrorResponse to the user | Offline users get no feedback at all. |
| 10 | NOT-01 | P0 | Proper notification channels | The channel shows up as 'Null' in system notification settings. |
| 11 | NOT-02 | P0 | POST_NOTIFICATIONS runtime permission | Notifications silently fail on Android 13+ without it. |
| 12 | TEST-05 | P0 | Version comparison tests | Prevents the 5.10 < 5.9 bug from returning. |
| 13 | TEST-13 | P0 | Merge the helper/tests suite into revive/build-fix | The tests are written and pass except for the intentional bug characterizations; they only help once they're on the main branch. |
| 14 | DOC-01 | P0 | Update README for the revived fork | Users and contributors need to know this is the maintained fork. |
| 15 | UI-11 | P1 | Fix biased splash tagline selection | Bug: the chain skews the distribution heavily toward the first entries (TEST_REPORT bug #9). |
| 16 | A11Y-02 | P1 | Minimum 48dp touch targets (14 ATF failures) | Espresso AccessibilityChecks reports 14 TouchTargetSizeCheck errors (TEST_REPORT rows 19–20). |
| 17 | A11Y-19 | P1 | Label clickable container main_box_1 | The UI dump shows a clickable 968x61px view with no text or description (TEST_REPORT row 18). |
| 18 | REL-01 | P1 | Guard theme spinner re-entrancy | Bug: this recursion produces a StackOverflowError under Robolectric. It's latent on device because the framework only fires on a real change (TEST_REPORT bug #4). |
| 19 | REL-08 | P1 | Fix sortListMap numeric comparator | Violates the Comparator contract, so large lists sort incorrectly (TEST_REPORT #1). |
| 20 | REL-09 | P1 | Parse numeric strings robustly | Bug: Integer.valueOf throws NumberFormatException on Gson-produced numbers (TEST_REPORT bug #5). |

## Bugs and test findings (from TEST_REPORT.md)

| ID | Pri | Title | Files |
|---|---|---|---|
| UI-11 | P1 | Fix biased splash tagline selection | `SplashActivity.java` |
| A11Y-01 | P0 | Label the Settings and Experiments switches | `res/layout/main.xml (settings/experiments rows)` |
| A11Y-02 | P1 | Minimum 48dp touch targets (14 ATF failures) | `res/layout/main.xml` |
| A11Y-19 | P1 | Label clickable container main_box_1 | `res/layout/main.xml#main_box_1` |
| REL-01 | P1 | Guard theme spinner re-entrancy | `MainActivity.java#_Theme_UI, select_theme listener` |
| REL-02 | P0 | Semantic version comparison | `MainActivity.java (Server vs local_version)` |
| REL-03 | P0 | Handle HTTP error status codes | `RequestNetworkController.java` |
| REL-06 | P0 | Configurable/non-empty API URL | `res/values/api.xml, MainActivity.java` |
| REL-08 | P1 | Fix sortListMap numeric comparator | `SketchwareUtil.java#sortListMap` |
| REL-09 | P1 | Parse numeric strings robustly | `SketchwareUtil.java#sortListMap` |
| REL-10 | P1 | Fix duplicate 'German' language entry | `MainActivity.java#_Language_Pack` |
| REL-11 | P1 | Restartable, injectable logger | `xManagerLogger.java` |
| CI-18 | P1 | Make emulator CI ANR-resistant | `.github/workflows/android-tests.yml, SmokeTest.java` |

## Category index

- [UI/UX & Material 3](#uiux--material-3): 22
- [Accessibility](#accessibility): 19
- [Performance](#performance): 19
- [Reliability & error handling](#reliability--error-handling): 25
- [Update checking](#update-checking): 14
- [Settings & backup](#settings--backup): 14
- [Notifications](#notifications): 12
- [Internationalization (i18n)](#internationalization-i18n): 16
- [Security & privacy](#security--privacy): 14
- [Code health & architecture](#code-health--architecture): 20
- [Build & CI](#build--ci): 18
- [Testing](#testing): 13
- [Docs & community](#docs--community): 20

## UI/UX & Material 3

### UI-01 · Adopt Material 3 theme  `P1` `M`

Switch the base theme to Theme.Material3.DayNight and map the 21 custom gradients to M3 color roles.

*Why:* Gives consistent, modern components and proper day/night handling for free.  
*Files:* `res/values/styles.xml, MainActivity.java#_Theme_UI`

### UI-02 · Dynamic color (Monet) option  `P2` `S`

Offer a 'System (Material You)' theme using DynamicColors.applyToActivitiesIfAvailable on Android 12+.

*Why:* Matches the user's wallpaper palette, like Droid-ify and Neo Store do.  
*Files:* `xManager.java, MainActivity.java#_Theme_UI`

### UI-03 · Follow system dark/light mode  `P2` `S`

Add an 'Auto' theme that follows UI_MODE_NIGHT instead of always-dark gradients.

*Why:* Respects the system setting and reduces eye strain in daylight.  
*Files:* `MainActivity.java#_Theme_UI`

### UI-04 · Replace ProgressDialog with M3 progress indicators  `P1` `S`

Swap the deprecated ProgressDialog for an inline LinearProgressIndicator or a modal bottom sheet.

*Why:* ProgressDialog has been deprecated since API 26 and blocks the whole UI.  
*Files:* `MainActivity.java (ProgressDialog usages)`

### UI-05 · Use MaterialAlertDialogBuilder everywhere  `P2` `S`

Replace AlertDialog.Builder with MaterialAlertDialogBuilder so dialogs pick up the theme.

*Why:* Today's dialogs look out of place next to the gradient UI.  
*Files:* `MainActivity.java (AlertDialog.Builder)`

### UI-06 · Bottom navigation bar for top-level screens  `P2` `M`

Turn Home, Settings, Experiments and About into a NavigationBar instead of hidden icons.

*Why:* Makes navigation discoverable; the gear and flask icons are easy to miss.  
*Files:* `res/layout/main.xml, MainActivity.java#box_switch/box_experiment/box_about`

### UI-07 · Split screens into Fragments (single Activity + nav graph)  `P1` `L`

Move each visibility-toggled ScrollView (main, settings, about, experiments) into its own Fragment hosted by one Activity with a Navigation graph.

*Why:* Gives proper back-stack, state saving, deep links and testable screens.  
*Files:* `res/layout/main.xml, MainActivity.java, res/navigation/`

### UI-08 · Edge-to-edge and window insets  `P0` `S`

Call EdgeToEdge.enable() and apply WindowInsets padding, which target SDK 35 enforces.

*Why:* On Android 15 with targetSdk 35 content draws under the status and navigation bars.  
*Files:* `MainActivity.java#onCreate, SplashActivity.java`

### UI-09 · Predictive back gesture  `P1` `S`

Use OnBackPressedDispatcher and set android:enableOnBackInvokedCallback="true".

*Why:* The custom _onBackPressed override breaks Android 14+ predictive back.  
*Files:* `MainActivity.java#_onBackPressed, AndroidManifest.xml`

### UI-10 · AndroidX SplashScreen API  `P1` `S`

Replace SplashActivity's 1-second Timer with core-splashscreen and keep-on-screen conditions.

*Why:* Android 12+ shows a system splash anyway, so users currently see two splashes.  
*Files:* `SplashActivity.java`

### UI-11 · Fix biased splash tagline selection  `P1` `S`

Pick the splash title with a single Random.nextInt(n) instead of re-rolling getRandom in each else-branch.

*Why:* Bug: the chain skews the distribution heavily toward the first entries (TEST_REPORT bug #9).  
*Files:* `SplashActivity.java`

### UI-13 · Search/filter in version lists  `P3` `S`

Add a SearchView to filter long version lists by version number.

*Why:* The lists grow with every release and scrolling is tedious.  
*Files:* `MainActivity.java#_Backbone, list_versions.xml`

### UI-14 · Highlight latest and installed versions  `P1` `M`

Show 'Latest' and 'Installed' chips on list rows by comparing with PackageManager.getPackageInfo.

*Why:* Users mostly want to know whether they're up to date, which Obtainium shows prominently.  
*Files:* `MainActivity.java (list adapters)`

### UI-15 · Pull-to-refresh feedback  `P1` `S`

Show a Snackbar with the result (updated, no changes or error) when the swipe refresh finishes.

*Why:* Refreshing currently ends silently, even on failure.  
*Files:* `MainActivity.java#main_refresh_layout`  
*Status:* Done: a user-started refresh reports no-change via toast (`R.string.refresh_no_changes`); errors still use the existing API error view (UI-16).

### UI-16 · Empty and error states  `P0` `S`

Show a dedicated empty or error view with a Retry button when the API can't be loaded.

*Why:* With an unreachable or empty API URL the main screen just stays blank.  
*Files:* `MainActivity.java#_Backbone, onErrorResponse`

### UI-17 · Skeleton/shimmer loading placeholders  `P3` `S`

Show placeholder rows while the API JSON loads.

*Why:* Makes the startup wait feel shorter and look intentional.  
*Files:* `res/layout/main.xml`

### UI-18 · Consistent spacing and typography scale  `P3` `M`

Replace hard-coded textSize and padding values with M3 type and dimen resources.

*Why:* There are hundreds of magic numbers in main.xml, so the visual rhythm is inconsistent.  
*Files:* `res/layout/main.xml, res/values/dimens.xml`

### UI-19 · Landscape and tablet layouts  `P3` `M`

Provide sw600dp and land layouts with a two-pane list and details view.

*Why:* On tablets and Chromebooks the phone layout stretches awkwardly.  
*Files:* `res/layout-sw600dp/`

### UI-21 · Replace custom RippleReveal with MaterialContainerTransform  `P3` `M`

Use the platform/M3 transitions instead of the hand-written RippleReveal view.

*Why:* Means less custom code to maintain, and it respects reduced-motion settings.  
*Files:* `RippleReveal.java`

### UI-22 · App shortcuts  `P3` `S`

Add static and dynamic launcher shortcuts for 'Check for updates' and 'Settings'.

*Why:* Gives quick access for power users without opening the full UI.  
*Files:* `AndroidManifest.xml, res/xml/shortcuts.xml`

### UI-23 · Themed (monochrome) launcher icon  `P2` `S`

Add a <monochrome> layer to the adaptive icon for Android 13 themed icons.

*Why:* The icon otherwise looks out of place on themed home screens.  
*Files:* `res/mipmap-anydpi-v26/ic_launcher.xml`  
*Status:* Already satisfied: `mipmap-anydpi-v26/xmanager.xml` (and the round variant) include a `<monochrome>` layer pointing at `@mipmap/ic_launcher_monet`.

### UI-24 · Explain empty 'LATEST: N/A'  `P1` `S`

When no data has loaded, show 'Couldn't load versions – tap to retry' instead of N/A next to LATEST.

*Why:* N/A reads like 'not available for your device' rather than a load failure.  
*Files:* `MainActivity.java, res/layout/main.xml`

## Accessibility

### A11Y-01 · Label the Settings and Experiments switches  `P0` `S`

Give list_auto_refresh_switch, disable_notification_switch, hide_stock_patched_switch, hide_amoled_patched_switch and cloned_version_switch an accessible name: android:labelFor on the sibling TextView, or a contentDescription.

*Why:* The UI dump shows all five Switch widgets unlabelled, so TalkBack reads 'Switch, off' with no context. The icon buttons were already fixed in b29202e (TEST_REPORT row 17).  
*Files:* `res/layout/main.xml (settings/experiments rows)`

### A11Y-02 · Minimum 48dp touch targets (14 ATF failures)  `P1` `S`

Raise to 48dp with minWidth/minHeight or padding: box_experiment, box_switch, box_update, box_settings_icon_close (32x32dp); box_uninstall, box_settings, box_cache, box_open (32dp high); the 4 settings switches (47x20dp); box_reboot and box_reset_preferences (34dp high).

*Why:* Espresso AccessibilityChecks reports 14 TouchTargetSizeCheck errors (TEST_REPORT rows 19–20).  
*Files:* `res/layout/main.xml`

### A11Y-03 · Color contrast audit across 21 themes  `P1` `M`

Check text/background contrast for each gradient theme against WCAG AA (4.5:1).

*Why:* Several bright gradients put white text on light green or yellow.  
*Files:* `MainActivity.java#_Theme_UI`

### A11Y-04 · Support font scaling  `P1` `M`

Use sp everywhere, remove fixed heights on text containers and test at 200% font scale.

*Why:* Large font settings currently clip text in fixed-height boxes.  
*Files:* `res/layout/main.xml`

### A11Y-05 · Respect reduced motion  `P2` `S`

Skip RippleReveal and the splash animations when ANIMATOR_DURATION_SCALE is 0 or remove-animations is on.

*Why:* Animations can cause discomfort for vestibular-sensitive users.  
*Files:* `RippleReveal.java, SplashActivity.java`  
*Status:* Done: RippleReveal skips the reveal and the splash skips its fade-in when the animator scale is 0 (Developer options, or Accessibility "Remove animations"). Tests in `SmallFixesBatch7Test`.

### A11Y-06 · Accessibility headings  `P2` `S`

Mark section titles with accessibilityHeading=true.

*Why:* Lets screen-reader users jump between sections.  
*Files:* `res/layout/main.xml`  
*Status:* Done: the header, About title, the four version-section titles and Manager Tools are marked as headings (`ViewCompat.setAccessibilityHeading`, works below API 28 too).

### A11Y-07 · Logical focus order  `P1` `S`

Set nextFocusDown/Forward and use importantForAccessibility to hide decorative views.

*Why:* The visibility-toggled screens leave hidden views focusable.  
*Files:* `res/layout/main.xml`

### A11Y-08 · Keyboard / D-pad navigation  `P2` `M`

Make all actions reachable with keyboard and D-pad, with visible focus states.

*Why:* Needed for Chromebook, Android TV and switch-access users.  
*Files:* `res/layout/main.xml, drawables`

### A11Y-09 · Announce state changes  `P2` `S`

Use announceForAccessibility or live regions for 'Refreshing', 'Update available' and errors.

*Why:* Snackbar and toast-only feedback is often missed by TalkBack users.  
*Files:* `MainActivity.java`

### A11Y-10 · Toggle state semantics for switches  `P1` `M`

Use SwitchMaterial with proper labels instead of clickable LinearLayouts with custom images.

*Why:* TalkBack can't tell whether the custom toggles are on or off.  
*Files:* `res/layout/main.xml, MainActivity.java (settings switches)`

### A11Y-11 · Gate CI on AccessibilityChecks  `P2` `S`

Run the new AccessibilityChecksTest in the emulator CI job, with a suppression list for known issues that shrinks as they're fixed.

*Why:* The ATF checks exist now (helper/tests) but only report; they should block regressions.  
*Files:* `app/src/androidTest/.../AccessibilityChecksTest.java, .github/workflows/android-tests.yml`

### A11Y-12 · Don't encode meaning in color alone  `P2` `S`

Add icons or text alongside color for status (latest, outdated, error).

*Why:* Color-blind users can't otherwise tell states apart.  
*Files:* `list_versions.xml`

### A11Y-13 · Readable changelog formatting  `P3` `S`

Render changelogs as bullet lists with proper semantics instead of a single long TextView.

*Why:* Long unstructured text is hard to navigate with a screen reader.  
*Files:* `list_changelogs.xml`

### A11Y-14 · Avoid all-caps text for long labels  `P3` `S`

Remove textAllCaps on long labels; screen readers can spell out all-caps text letter by letter.

*Why:* Improves readability and screen-reader pronunciation.  
*Files:* `res/layout/main.xml`

### A11Y-15 · Sufficient timeout for transient messages  `P3` `S`

Use Snackbars with actions and longer durations instead of 2-second toasts for important info.

*Why:* Users with cognitive or motor impairments need more time.  
*Files:* `MainActivity.java (SketchwareUtil.showMessage)`

### A11Y-16 · Test with Switch Access and Voice Access  `P3` `S`

Add a manual accessibility checklist to the release process.

*Why:* Automated checks don't catch everything.  
*Files:* `docs/`

### A11Y-17 · Don't hide content behind long-press only  `P2` `S`

Make every long-press action also reachable via a visible menu.

*Why:* Long press is hard to discover and hard for motor-impaired users.  
*Files:* `MainActivity.java (setOnLongClickListener)`

### A11Y-18 · Large-screen pointer and hover support  `P3` `S`

Add hover states and right-click context menus for mouse users on ChromeOS and DeX.

*Why:* Better desktop-mode usability.  
*Files:* `res/layout/main.xml`

### A11Y-19 · Label clickable container main_box_1  `P1` `S`

Give main_box_1 a contentDescription, or make it non-clickable if it's decorative.

*Why:* The UI dump shows a clickable 968x61px view with no text or description (TEST_REPORT row 18).  
*Files:* `res/layout/main.xml#main_box_1`

## Performance

### PERF-01 · Replace java.util.Timer with coroutines/Handler  `P1` `M`

Swap the dozens of Timer and TimerTask uses for Handler.postDelayed or lifecycle-aware coroutines.

*Why:* Each Timer creates a thread, and the tasks aren't cancelled on destroy, which leaks the Activity.  
*Files:* `MainActivity.java, SplashActivity.java`

### PERF-02 · RecyclerView instead of ListView  `P2` `M`

Migrate the version and changelog lists to RecyclerView with ListAdapter and DiffUtil.

*Why:* Gives efficient updates and animations and avoids full rebinds on refresh.  
*Files:* `MainActivity.java (ListView adapters), list_versions.xml`

### PERF-03 · Continue flattening main.xml with ConstraintLayout  `P2` `L`

Build on b0e6e74's flattening: convert the remaining nested LinearLayout rows of the 2,000+ line main.xml to ConstraintLayout or merge tags.

*Why:* Deep nesting still dominates measure/layout time at startup.  
*Files:* `res/layout/main.xml`

### PERF-04 · Lazy-inflate secondary screens  `P2` `M`

Use ViewStub or separate fragments so Settings, About and Experiments aren't inflated at startup.

*Why:* The whole UI is inflated up-front even though most of it is hidden.  
*Files:* `res/layout/main.xml`

### PERF-05 · Avoid re-serializing JSON in _Backbone  `P2` `S`

Parse the API once into typed POJOs instead of toJson/fromJson round trips per list.

*Why:* Saves CPU and allocations on every refresh.  
*Files:* `MainActivity.java#_Backbone`

### PERF-06 · Baseline Profiles  `P3` `M`

Add a baseline profile module to precompile startup paths.

*Why:* Typically gives a 20–30% faster cold start.  
*Files:* `baselineprofile/ (new)`

### PERF-07 · Enable R8 full mode and resource shrinking  `P1` `S`

Turn on minifyEnabled and shrinkResources for release with keep rules for Gson models.

*Why:* Smaller APK and faster code.  
*Files:* `app/build.gradle, proguard-rules.pro`

### PERF-08 · Cache API response with ETag/If-Modified-Since  `P1` `S`

Use OkHttp's disk cache and conditional requests.

*Why:* Avoids downloading the same JSON on every launch and pull-to-refresh.  
*Files:* `RequestNetwork.java / RequestNetworkController.java`

### PERF-09 · Debounce refresh  `P2` `S`

Ignore refresh requests made within N seconds of the last successful fetch.

*Why:* Prevents hammering the API server.  
*Files:* `MainActivity.java#main_refresh_layout`  
*Status:* Done: pull-to-refresh is ignored for 15 s after a successful fetch (`REFRESH_DEBOUNCE_MS`), with a short toast (`R.string.refresh_debounced`).

### PERF-10 · Move work off the main thread  `P1` `M`

Make sure the Gson parse, file IO (cache clearing) and PackageManager queries run on background dispatchers.

*Why:* Prevents jank and ANRs on low-end devices.  
*Files:* `MainActivity.java#_Backbone, cache clear`

### PERF-11 · Avoid GradientDrawable allocation per theme switch  `P3` `S`

Define gradients as XML drawables or cache them instead of allocating new ones on every _Theme_UI call.

*Why:* _Theme_UI allocates dozens of drawables each time it runs.  
*Files:* `MainActivity.java#_Theme_UI`

### PERF-12 · StrictMode in debug builds  `P2` `S`

Enable StrictMode thread and VM policies in debug to surface disk and network on the main thread.

*Why:* Finds hidden performance bugs early.  
*Files:* `xManager.java`  
*Status:* Done: debug builds enable StrictMode thread and VM policies with `penaltyLog()` only (never crash); release builds are unchanged.

### PERF-13 · Make the logcat reader opt-in  `P1` `S`

Start xManagerLogger only when the user enables 'Debug logging' in Settings, and stop it when they turn it off.

*Why:* Reading logcat continuously wastes CPU and battery, and collecting logs by default is a privacy concern.  
*Files:* `xManagerLogger.java, xManager.java, Settings`

### PERF-14 · Image loading with Coil/Glide  `P3` `S`

Load remote images (supporter avatars and the like) via Coil with memory and disk caching, if they're used.

*Why:* Prevents decoding full-size bitmaps on the UI thread.  
*Files:* `MainActivity.java`

### PERF-15 · Macrobenchmark startup test  `P3` `M`

Add a Macrobenchmark module measuring cold start and time to the first list render.

*Why:* Puts numbers on performance changes.  
*Files:* `benchmark/ (new)`

### PERF-16 · Remove unused resources and libraries  `P2` `S`

Run lint UnusedResources and drop unused drawables and fonts.

*Why:* Smaller APK and faster builds.  
*Files:* `res/, app/build.gradle`

### PERF-17 · Use ViewBinding  `P2` `M`

Replace the hundreds of findViewById calls with ViewBinding.

*Why:* Faster to write, null-safe, and slightly faster at runtime.  
*Files:* `MainActivity.java#initialize, app/build.gradle`

### PERF-18 · Reduce overdraw  `P3` `S`

Remove the redundant backgrounds stacked by the gradients plus the window background.

*Why:* Profile GPU overdraw shows 3–4x on the main screen.  
*Files:* `res/layout/main.xml, styles.xml`

### PERF-19 · Startup tracing  `P3` `S`

Add androidx.tracing sections around onCreate, inflate, theme and language apply, and the first fetch.

*Why:* Pinpoints what makes cold start slow in Perfetto.  
*Files:* `MainActivity.java#onCreate`

## Reliability & error handling

### REL-01 · Guard theme spinner re-entrancy  `P1` `S`

In select_theme.onItemSelected, return early when the position equals the current theme (or use a 'programmatic change' flag) before calling _Theme_UI, which calls setSelection.

*Why:* Bug: this recursion produces a StackOverflowError under Robolectric. It's latent on device because the framework only fires on a real change (TEST_REPORT bug #4).  
*Files:* `MainActivity.java#_Theme_UI, select_theme listener`

### REL-02 · Semantic version comparison  `P0` `S`

Replace Double.parseDouble(version) with a proper semver comparator.

*Why:* 5.10 is treated as older than 5.9, which wrongly triggers the maintenance screen, and '5.9.1' crashes the parse (TEST_REPORT #2).  
*Files:* `MainActivity.java (Server vs local_version)`

### REL-03 · Handle HTTP error status codes  `P0` `S`

Treat non-2xx responses as errors in RequestNetworkController instead of passing the body to onResponse.

*Why:* Bug: a 503 HTML error page is fed to Gson and throws JsonSyntaxException (TEST_REPORT bug #6, MockWebServer test).  
*Files:* `RequestNetworkController.java`

### REL-04 · Guard JSON parsing  `P0` `S`

Wrap fromJson in try/catch, validate the required keys and show an error state on malformed data.

*Why:* A broken API response currently crashes the app via the global handler.  
*Files:* `MainActivity.java#_Backbone`

### REL-05 · Null-safe map access  `P0` `S`

Use getOrDefault and type checks for every API key (Telegram, Discord, *_Latest, ...).

*Why:* A missing key causes a NullPointerException with .toString().  
*Files:* `MainActivity.java#_Backbone`

### REL-06 · Configurable/non-empty API URL  `P0` `S`

Ship a real default api_url (or a first-run 'choose source' screen) and show an explicit error when it's empty.

*Why:* Bug: api_url is empty in res/values/api.xml, so every list shows N/A with no explanation (TEST_REPORT bug #7, emulator row 3).  
*Files:* `res/values/api.xml, MainActivity.java`

### REL-07 · Report onErrorResponse to the user  `P0` `S`

Show an error message and Retry instead of the no-op onErrorResponse.

*Why:* Offline users get no feedback at all.  
*Files:* `MainActivity.java (RequestListener.onErrorResponse)`

### REL-08 · Fix sortListMap numeric comparator  `P1` `S`

Return 1 when a > b (currently `a<b?-1:a<b?1:0`), or use Integer.compare.

*Why:* Violates the Comparator contract, so large lists sort incorrectly (TEST_REPORT #1).  
*Files:* `SketchwareUtil.java#sortListMap`

### REL-09 · Parse numeric strings robustly  `P1` `S`

Accept '3.0' (Gson doubles) in numeric sorts and version codes by parsing via Double/Long or BigDecimal.

*Why:* Bug: Integer.valueOf throws NumberFormatException on Gson-produced numbers (TEST_REPORT bug #5).  
*Files:* `SketchwareUtil.java#sortListMap`

### REL-10 · Fix duplicate 'German' language entry  `P1` `S`

Remove the second 'German' from the language list and use stable language codes instead of indexes.

*Why:* Bug: the duplicate shifts indexes and can select the wrong translation (TEST_REPORT bug #3).  
*Files:* `MainActivity.java#_Language_Pack`

### REL-11 · Restartable, injectable logger  `P1` `S`

Reset isRunning in finally, allow stop/start, don't throw if already running, and make the logger an injectable instance instead of a static singleton.

*Why:* Bug: a second Application.onCreate throws 'Logger already running' (TEST_REPORT bug #8).  
*Files:* `xManagerLogger.java`

### REL-12 · Cancel timers in onDestroy  `P1` `S`

Keep references to TimerTasks and cancel them in onDestroy/onStop.

*Why:* Late callbacks can touch views of a destroyed Activity and crash.  
*Files:* `MainActivity.java, SplashActivity.java`  
*Status:* Done: `MainActivity` and `SplashActivity` cancel their timer in `onDestroy`; a late `schedule()` after that is ignored instead of crashing (`LifecycleTimer`).

### REL-13 · Survive configuration changes  `P1` `M`

Move state (current screen, fetched data) into a ViewModel and SavedStateHandle.

*Why:* Rotation re-runs network calls and resets to the home screen.  
*Files:* `MainActivity.java`

### REL-14 · Request timeouts and retries  `P1` `S`

Set connect/read timeouts and exponential-backoff retry for idempotent GETs.

*Why:* The defaults can hang for a long time on flaky networks.  
*Files:* `RequestNetworkController.java`

### REL-15 · Network availability check  `P2` `S`

Check ConnectivityManager before fetching and show an offline banner.

*Why:* Gives a clear message instead of a generic failure.  
*Files:* `MainActivity.java`

### REL-16 · Crash handler should not loop  `P1` `S`

Make sure DebugActivity can't itself crash into the handler again, and add a 'restart app' action.

*Why:* Avoids crash loops that require clearing data.  
*Files:* `xManager.java, DebugActivity.java`

### REL-17 · Export crash report  `P2` `S`

Let users share and copy the DebugActivity stack trace plus device info.

*Why:* Makes bug reports actionable.  
*Files:* `DebugActivity.java`

### REL-18 · Stop deleting hard-coded /storage/emulated/0 paths  `P1` `S`

Use getExternalCacheDir()/cacheDir instead of literal paths when clearing the cache.

*Why:* Hard-coded paths break on secondary users and work profiles and are blocked by scoped storage.  
*Files:* `MainActivity.java (clear cache)`

### REL-19 · Handle ActivityNotFoundException for links  `P1` `S`

Fall back to the default browser, and then a 'copy link' option, when Custom Tabs or a browser isn't available.

*Why:* Devices without Chrome or any browser currently crash or do nothing.  
*Files:* `MainActivity.java#_Browser`

### REL-20 · Validate links before opening  `P1` `S`

Only open https URLs from allow-listed hosts; reject intent://, file:// and content:// schemes coming from the API.

*Why:* Protects against a tampered API response launching arbitrary intents.  
*Files:* `MainActivity.java#_Browser`

### REL-21 · Idempotent initialization flag  `P2` `S`

Replace the INITIALIZATION and REFETCH SharedPreferences dance with one well-defined first-run state.

*Why:* The interplay of flags is fragile and hard to reason about.  
*Files:* `MainActivity.java`

### REL-22 · Defensive PackageManager calls  `P1` `S`

Catch NameNotFoundException when querying the installed version of a managed app.

*Why:* Gives graceful 'not installed' display instead of crashes.  
*Files:* `MainActivity.java`  
*Status:* Already satisfied: `_Installed_Version` catches the lookup failure (including `NameNotFoundException`) and returns null, shown as N/A. Covered by the Robolectric launch tests.

### REL-23 · Filter package broadcasts by package name  `P2` `S`

In packageChangeReceiver, read intent.getData().getSchemeSpecificPart() and refresh only for the two managed packages.

*Why:* Today every app install, update or uninstall on the device triggers a PackageManager refresh.  
*Files:* `MainActivity.java#packageChangeReceiver (~line 2802)`  
*Status:* Done: the package receiver only reacts to `com.spotify.music` and `com.spotify.musix` (`MainActivity.isWatchedPackageIntent`).

### REL-24 · Coalesce bursts of package broadcasts  `P3` `S`

Debounce _Refresh_Installed (e.g. 300 ms) so ADDED+REPLACED+CHANGED from one install only refresh once.

*Why:* A single update sends 3–4 broadcasts, each queuing a refresh.  
*Files:* `MainActivity.java#_Refresh_Installed`  
*Status:* Done: package broadcasts are debounced by 300 ms on the main looper, so one install refreshes once; a pending refresh is dropped in `onStop`.

### REL-25 · Don't touch views after destroy in installed refresh  `P2` `S`

Check isFinishing()/isDestroyed() before posting results from installedExecutor to the UI thread.

*Why:* shutdownNow() interrupts the work but a result already posted can still run against a destroyed Activity.  
*Files:* `MainActivity.java#_Refresh_Installed`  
*Status:* Already satisfied: the result is posted only if `!isFinishing() && !isDestroyed()`, and the executor shuts down in `onDestroy`.

## Update checking

### UPD-01 · Background update check with WorkManager  `P1` `M`

Schedule a periodic WorkManager job that checks the API and notifies about new versions.

*Why:* Users learn about updates without opening the app, the core feature of Obtainium.  
*Files:* `new UpdateWorker`

### UPD-03 · Configurable check interval  `P2` `S`

Let users choose the background check frequency (6h, 12h, daily, weekly or off).

*Why:* Balances freshness against battery use.  
*Files:* `Settings`

### UPD-04 · Respect battery saver and Doze  `P2` `S`

Use WorkManager constraints (battery not low, network connected).

*Why:* Better battery citizenship.  
*Files:* `UpdateWorker`

### UPD-05 · Release channels  `P3` `M`

Let users choose stable or beta channels for manager updates.

*Why:* Testers get early builds while normal users stay on stable.  
*Files:* `SelfUpdateChecker, Settings`

### UPD-06 · Ignore/skip a specific version  `P3` `S`

Let users skip a particular update so they aren't nagged about it again.

*Why:* A standard feature in Obtainium and Neo Store.  
*Files:* `UpdateWorker, prefs`

### UPD-07 · Show changelog in the update dialog  `P2` `S`

Render App_Changelogs in the update dialog with markdown formatting.

*Why:* Users want to know what changed before updating.  
*Files:* `MainActivity.java (update dialog)`

### UPD-08 · Last checked timestamp  `P2` `S`

Show 'Last checked: 5 min ago' on the main screen.

*Why:* Gives users confidence that the data is fresh.  
*Files:* `MainActivity.java`

### UPD-09 · Maintenance screen with details  `P2` `S`

Show the server-provided message and an ETA, plus a retry button, instead of a static maintenance view.

*Why:* The current screen is a dead end.  
*Files:* `MainActivity.java (maintenance)`

### UPD-10 · Offline mode with cached data  `P2` `M`

Show the last fetched lists when offline, clearly labelled as cached.

*Why:* The app stays useful without a network connection.  
*Files:* `MainActivity.java, cache`

### UPD-11 · API schema versioning  `P2` `S`

Add a schema_version field to the API and handle unknown or newer schemas gracefully.

*Why:* Lets the backend evolve without breaking old clients.  
*Files:* `API JSON, MainActivity.java#_Backbone`

### UPD-12 · Update check on app start toggle  `P3` `S`

Add a setting to skip the automatic fetch on launch.

*Why:* Some users prefer manual refresh only.  
*Files:* `Settings`

### UPD-13 · Rate-limit aware GitHub API usage  `P2` `S`

Cache GitHub API responses and honor the X-RateLimit headers.

*Why:* Unauthenticated users have only 60 requests an hour.  
*Files:* `SelfUpdateChecker`

### UPD-14 · Signed update manifest  `P1` `M`

Sign the API JSON (e.g. with minisign/Ed25519) and verify it on the client.

*Why:* Prevents a hijacked API host from pushing malicious links.  
*Files:* `API JSON, new ManifestVerifier`

### UPD-16 · Update badge on launcher icon  `P3` `S`

Show a notification-dot/badge count of available updates.

*Why:* A low-friction hint without heads-up notifications.  
*Files:* `UpdateWorker`

## Settings & backup

### SET-01 · Migrate to a PreferenceFragmentCompat screen  `P2` `M`

Rebuild Settings with AndroidX Preference instead of hand-made rows.

*Why:* Gives standard look, accessibility and much less code.  
*Files:* `res/layout/main.xml (main_scroll_settings), MainActivity.java`

### SET-02 · Consolidate SharedPreferences files  `P2` `S`

Merge the 9 separate SharedPreferences files (THEME, LANGUAGE, ...) into one prefs file or DataStore.

*Why:* This simplifies backup, migration and testing.  
*Files:* `MainActivity.java (getSharedPreferences calls)`

### SET-03 · Jetpack DataStore  `P3` `M`

Move settings to Preferences DataStore with typed keys.

*Why:* Asynchronous and type-safe, with no main-thread disk IO.  
*Files:* `new SettingsRepository`

### SET-04 · Export/import settings  `P2` `S`

Export settings to JSON through the Storage Access Framework and import them back.

*Why:* Users can restore their setup on a new device.  
*Files:* `Settings`

### SET-05 · Configure backup rules  `P1` `S`

Replace bare allowBackup=true with dataExtractionRules/fullBackupContent so only the settings prefs are backed up (to cloud and on device transfer), and exclude logs and cache.

*Why:* Today everything, including logs, is in adb/cloud backups; scoped rules still let a new phone restore the theme and language.  
*Files:* `AndroidManifest.xml, res/xml/data_extraction_rules.xml`

### SET-06 · Reset to defaults  `P2` `S`

Add a 'Reset all settings' action with confirmation.

*Why:* Lets users recover from a bad state without clearing app data.  
*Files:* `Settings`

### SET-07 · Store theme/language as stable keys  `P1` `S`

Persist language codes ('de') and theme ids instead of display strings or indexes.

*Why:* Index-based storage breaks when lists change (see the duplicate German entry).  
*Files:* `MainActivity.java (LANGUAGE/THEME prefs)`

### SET-08 · Settings search  `P3` `S`

Make settings searchable once the list grows.

*Why:* Helps users find options quickly.  
*Files:* `Settings`

### SET-09 · Explain each experimental option  `P2` `S`

Add summaries and warnings to the Experiments switches.

*Why:* Users don't know what the experiments do.  
*Files:* `res/layout/main.xml (main_box_16)`

### SET-10 · Version switch A/B clarity  `P2` `S`

Rename the SWITCH_VERSION 'A'/'B' options to descriptive labels.

*Why:* The meaning of 'A' and 'B' is opaque.  
*Files:* `MainActivity.java (SWITCH_VERSION)`

### SET-13 · Per-app language via AppCompat  `P1` `S`

Use AppCompatDelegate.setApplicationLocales and locales_config.xml.

*Why:* Integrates with the Android 13 per-app language settings.  
*Files:* `AndroidManifest.xml, res/xml/locales_config.xml`

### SET-14 · Settings migration framework  `P2` `S`

Version the settings schema and migrate old keys on upgrade.

*Why:* Lets settings be refactored without losing user preferences.  
*Files:* `new SettingsMigrator`

### SET-15 · Clear cache confirmation and size  `P3` `S`

Show how much cache will be freed, and confirm before clearing.

*Why:* Makes the action transparent.  
*Files:* `MainActivity.java (clear cache)`

### SET-16 · About screen: open-source licenses  `P2` `S`

Add an OSS licenses screen (e.g. AboutLibraries).

*Why:* Required for attribution, and good practice.  
*Files:* `About screen`

## Notifications

### NOT-01 · Proper notification channels  `P0` `S`

Replace the channel id 'id 1', named and described 'Null' at IMPORTANCE_HIGH, with named channels: Updates, Errors.

*Why:* The channel shows up as 'Null' in system notification settings.  
*Files:* `MainActivity.java (NotificationChannel, ~line 8824)`

### NOT-02 · POST_NOTIFICATIONS runtime permission  `P0` `S`

Request POST_NOTIFICATIONS on Android 13+ with an explanation.

*Why:* Notifications silently fail on Android 13+ without it.  
*Files:* `AndroidManifest.xml, MainActivity.java`

### NOT-03 · Lower default importance  `P1` `S`

Use IMPORTANCE_DEFAULT or LOW for update notifications instead of HIGH.

*Why:* Heads-up notifications for updates are intrusive.  
*Files:* `MainActivity.java`

### NOT-07 · Notification grouping  `P3` `S`

Group multiple update notifications into one summary.

*Why:* Avoids flooding the notification shade.  
*Files:* `UpdateWorker`

### NOT-08 · Respect DISABLE_NOTIFICATION via channels  `P2` `S`

Map the in-app toggle to channel settings, or deep-link to system channel settings.

*Why:* There is one source of truth for notification preferences.  
*Files:* `MainActivity.java (DISABLE_NOTIFICATION)`

### NOT-09 · Monochrome small icon  `P2` `S`

Provide a proper white-on-transparent small notification icon.

*Why:* Colored icons render as white squares.  
*Files:* `res/drawable/ic_stat_*.xml`

### NOT-10 · FLAG_IMMUTABLE for future PendingIntents  `P3` `S`

When notification actions are added, create every PendingIntent with FLAG_IMMUTABLE (and add a lint check for it).

*Why:* Without the flag, PendingIntent creation crashes on Android 12+. There are no PendingIntents yet, so this is preventive.  
*Files:* `MainActivity.java, UpdateWorker`

### NOT-11 · Quiet hours  `P3` `S`

Don't post update notifications during user-defined quiet hours.

*Why:* Respects users' time.  
*Files:* `UpdateWorker`

### NOT-12 · Notification for maintenance/server status  `P3` `S`

Optionally notify when the server comes back from maintenance.

*Why:* Users don't have to keep checking manually.  
*Files:* `UpdateWorker`

### NOT-13 · Test notifications in instrumented tests  `P3` `S`

Assert channel creation and notification content in androidTest.

*Why:* Prevents a regression of the 'Null' channel.  
*Files:* `app/src/androidTest`

### NOT-14 · Notification permission rationale screen  `P2` `S`

Show an in-app explainer before the system POST_NOTIFICATIONS prompt, with a 'Not now' option.

*Why:* Raises the grant rate and respects users who decline.  
*Files:* `MainActivity.java`

### NOT-15 · Snooze update reminders  `P3` `S`

Add a 'Remind me tomorrow' notification action.

*Why:* Less nagging, and the user stays in control.  
*Files:* `UpdateWorker`

## Internationalization (i18n)

### I18N-01 · Move numbered translations into values-<locale>/ folders  `P0` `L`

1f992e5 moved the text into res/values/strings.xml as ~80 index-suffixed copies per key (e.g. *_23). Move each language into values-xx/strings.xml under the same key, and delete the if/else chain in _Language_UI.

*Why:* Enables the system locale and per-app language, and shrinks _Language_UI from thousands of setText calls to nothing.  
*Files:* `MainActivity.java#_Language_UI, res/values/strings.xml`

### I18N-02 · Crowdsourced translations via Weblate/Crowdin  `P2` `S`

Set up Weblate (free for FOSS) with the string resources.

*Why:* Lets the community translate without touching Java code.  
*Files:* `docs/, res/values-*/`

### I18N-03 · Follow system locale by default  `P1` `S`

Default to the system language instead of English when the user hasn't chosen one.

*Why:* Users get their own language out of the box.  
*Files:* `MainActivity.java (LANGUAGE pref)`

### I18N-04 · Enable RTL support  `P1` `M`

d968c0e switched to start/end attributes but left supportsRtl="false": enable it, mirror the directional icons and test Arabic/Hebrew/Persian.

*Why:* RTL languages are in the language list but render LTR.  
*Files:* `AndroidManifest.xml, res/layout/main.xml`

### I18N-05 · Plurals and formatted strings  `P2` `S`

Use plurals and placeholders instead of string concatenation.

*Why:* Concatenation produces grammatically wrong text in many languages.  
*Files:* `res/values/strings.xml`

### I18N-06 · Localized dates and numbers  `P3` `S`

Format dates and sizes with DateFormat and Formatter.formatShortFileSize.

*Why:* Locale-correct formatting.  
*Files:* `MainActivity.java`

### I18N-07 · Language names in their own language  `P3` `S`

Show 'Deutsch' and '日本語' with the English name as subtitle.

*Why:* Users looking for their own language recognize it immediately.  
*Files:* `MainActivity.java#_Language_Pack`

### I18N-08 · Pseudo-locale testing  `P2` `S`

Enable pseudoLocalesEnabled in debug and test en-XA and ar-XB.

*Why:* Catches truncation and hard-coded strings.  
*Files:* `app/build.gradle`

### I18N-09 · Lint for hard-coded text  `P2` `S`

Make HardcodedText and SetTextI18n lint errors fatal on new code.

*Why:* Prevents regressions after the migration.  
*Files:* `app/build.gradle lint block, lint.xml`

### I18N-10 · Translation completeness report in CI  `P3` `S`

A CI step that reports missing keys per locale.

*Why:* Gives visibility for translators.  
*Files:* `.github/workflows`

### I18N-11 · Localized changelogs from API  `P3` `M`

Support optional per-locale changelog fields in the API schema.

*Why:* Non-English users can read the release notes.  
*Files:* `API schema`

### I18N-12 · Translator credits  `P3` `S`

Show translators in About, sourced from Weblate.

*Why:* Recognising contributors encourages more of them.  
*Files:* `About screen`

### I18N-13 · Remove locale-specific fonts hacks  `P3` `S`

Use font fallback instead of per-language font switching, if present.

*Why:* Simpler and correct for CJK and Indic scripts.  
*Files:* `MainActivity.java#_Language_UI`

### I18N-14 · Locale-aware sorting of language list  `P3` `S`

Sort languages with Collator for the current locale instead of a hard-coded order.

*Why:* Makes the list easier to scan in every language.  
*Files:* `MainActivity.java#_Language_Pack`

### I18N-15 · Translatable splash taglines  `P3` `S`

Move the random splash titles to string-array resources per locale.

*Why:* The splash text is currently English-only.  
*Files:* `SplashActivity.java`  
*Status:* Already satisfied: splash titles come from `R.string.splash_tagline_*` / `app_name` (see `SplashActivity.TAGLINES`). Per-locale overrides can still be added under `values-xx/` later.

### I18N-16 · Lint check that every language has every key  `P2` `S`

Once locales are in values-xx/, enable MissingTranslation as an error for the supported locales.

*Why:* The index-suffixed scheme can silently miss a language's copy of a key.  
*Files:* `lint.xml`

## Security & privacy

### SEC-01 · Remove requestLegacyExternalStorage  `P1` `S`

Drop the legacy storage flag and storage permissions, and use app-specific dirs and SAF.

*Why:* It's ignored on targetSdk 30+ and is a privacy red flag.  
*Files:* `AndroidManifest.xml`

### SEC-02 · Minimize permissions  `P1` `S`

Audit the manifest permissions and remove the unused ones.

*Why:* Fewer permissions mean more user trust and less attack surface.  
*Files:* `AndroidManifest.xml`

### SEC-03 · Certificate pinning for the API host (optional)  `P2` `S`

Pin the API host's public key via network_security_config, with backup pins.

*Why:* Mitigates MITM on hostile networks.  
*Files:* `res/xml/network_security_config.xml`

### SEC-04 · Don't log sensitive data  `P1` `S`

Make sure xManagerLogger doesn't record URLs with tokens or personal data, and cap the log size.

*Why:* Logcat captures can leak data into shared bug reports.  
*Files:* `xManagerLogger.java`

### SEC-05 · Dependency vulnerability scanning  `P1` `S`

Run osv-scanner or OWASP dependency-check against the resolved Gradle dependencies in CI.

*Why:* Catches known-vulnerable library versions (version bumps themselves are covered by Dependabot/Renovate).  
*Files:* `.github/workflows`

### SEC-06 · Secret scanning and no keys in repo  `P1` `S`

Make sure no keystore, API key or token is committed, and add gitleaks to CI.

*Why:* Protects the fork's signing identity.  
*Files:* `.github/workflows`

### SEC-07 · Reproducible release builds  `P2` `M`

Pin dependency versions with Gradle verification metadata, and document the build environment.

*Why:* Lets F-Droid and users verify that the binaries match the source.  
*Files:* `gradle/verification-metadata.xml`

### SEC-08 · Publish the signing certificate fingerprint  `P1` `S`

Document the release cert SHA-256 in README and Releases.

*Why:* Users can verify they have the genuine build (AppVerifier).  
*Files:* `README.md`

### SEC-09 · Privacy policy  `P2` `S`

Add a privacy policy describing network calls, the logs collected and no tracking.

*Why:* Builds trust, and is needed for some stores.  
*Files:* `docs/PRIVACY.md`

### SEC-10 · No analytics / tracker-free badge  `P3` `S`

Check that there are no trackers (Exodus scan) and advertise it.

*Why:* This matters to the target audience.  
*Files:* `README.md`

### SEC-12 · Clipboard hygiene  `P3` `S`

Mark copied links or logs containing device info as sensitive (ClipDescription EXTRA_IS_SENSITIVE).

*Why:* Avoids clipboard previews leaking data on Android 13+.  
*Files:* `MainActivity.java`  
*Status:* Done: changelog long-press copy goes through `_copyToClipboard`, which sets `ClipDescription.EXTRA_IS_SENSITIVE` on Android 13+; the snackbar uses `R.string.changelog_copied`.

### SEC-13 · Remove package visibility over-reach  `P2` `S`

Only query the packages the app actually needs (keep the <queries> list minimal).

*Why:* Fits Android 11+ package visibility expectations.  
*Files:* `AndroidManifest.xml <queries>`

### SEC-14 · Integrity check of the manager's own APK at runtime (informational)  `P2` `S`

Show the manager's own signing cert fingerprint in About so users can compare it with the published value.

*Why:* Helps users spot repackaged or malicious copies of the manager.  
*Files:* `About screen, PackageManager`

### SEC-15 · HTTPS-only enforcement for API overrides  `P2` `S`

Reject http:// custom source URLs in Settings.

*Why:* Keeps the cleartext-traffic protections when multiple sources are added.  
*Files:* `Settings, network_security_config.xml`

## Code health & architecture

### CODE-01 · Break up the 9,677-line MainActivity  `P0` `L`

Split it into feature classes: ThemeManager, LanguageManager, ApiRepository, VersionListAdapter and screen fragments.

*Why:* The god-class makes every change risky and untestable.  
*Files:* `MainActivity.java`

### CODE-02 · Introduce MVVM with ViewModel + Repository  `P1` `L`

Add a MainViewModel exposing UI state from an ApiRepository.

*Why:* Separates logic from views, survives configuration changes, and is unit-testable.  
*Files:* `MainActivity.java`

### CODE-03 · Typed API models  `P1` `M`

Replace HashMap<String,Object> with data classes (ApiManifest, BuildEntry, Changelog).

*Why:* Gives compile-time safety and removes casts and toString calls.  
*Files:* `MainActivity.java#_Backbone`

### CODE-04 · Data-driven themes  `P1` `M`

Replace the 21-branch if/else _Theme_UI with a table of Theme(id, colors) and a single apply method.

*Why:* Turns ~1,000 lines of duplication into ~50.  
*Files:* `MainActivity.java#_Theme_UI`

### CODE-05 · Data-driven languages  `P1` `M`

Replace the nested if/else language selection with a map from locale code to resources.

*Why:* This gets rid of a huge source of bugs, such as the duplicate German entry.  
*Files:* `MainActivity.java#_Language_Pack/_Language_UI`

### CODE-06 · Remove Sketchware boilerplate  `P2` `S`

Delete the unused SketchwareUtil and FileUtil methods, and the getRandom duplicates.

*Why:* Less dead code to maintain.  
*Files:* `SketchwareUtil.java, FileUtil.java, MainActivity.java`

### CODE-07 · Consistent naming conventions  `P3` `M`

Rename _Theme_UI, box_switch and main_box_16 to descriptive camelCase names.

*Why:* Makes the code readable for new contributors.  
*Files:* `MainActivity.java, res/layout/main.xml`

### CODE-08 · Kotlin migration (incremental)  `P3` `M`

Add Kotlin, and write new classes in Kotlin with coroutines.

*Why:* A modern, concise language with null safety.  
*Files:* `app/build.gradle`

### CODE-09 · Replace custom RequestNetwork with Retrofit/OkHttp  `P2` `M`

Use OkHttp directly, or Retrofit with converters, instead of the Sketchware wrapper.

*Why:* Gives a standard, well-tested, cancellable API.  
*Files:* `RequestNetwork.java / RequestNetworkController.java`

### CODE-10 · Dependency injection (Hilt or manual)  `P3` `M`

Provide the repository, OkHttp and prefs via DI.

*Why:* Makes testing and swapping implementations easy.  
*Files:* `xManager.java`

### CODE-11 · Static analysis: Android Lint baseline  `P1` `S`

Create a lint baseline, fix the high-severity issues and fail CI on new ones.

*Why:* Stops the codebase quality from regressing further.  
*Files:* `app/build.gradle, lint-baseline.xml`

### CODE-12 · Error Prone / SpotBugs  `P2` `S`

Add Error Prone or SpotBugs to catch null and comparator bugs at compile time.

*Why:* Would have caught the sortListMap comparator bug.  
*Files:* `app/build.gradle`

### CODE-13 · Formatter (google-java-format / spotless)  `P2` `S`

Add Spotless with a consistent format and a check in CI.

*Why:* No more style churn in PRs.  
*Files:* `build.gradle`

### CODE-14 · Version catalog (libs.versions.toml)  `P2` `S`

Centralize dependency versions in a Gradle version catalog.

*Why:* Makes upgrades easy and works well with Renovate.  
*Files:* `gradle/libs.versions.toml`

### CODE-15 · Gradle Kotlin DSL  `P3` `S`

Convert the build scripts to build.gradle.kts.

*Why:* Gives type-safe build config and IDE completion.  
*Files:* `app/build.gradle`

### CODE-16 · Constants for preference keys  `P2` `S`

Replace string literals like "THEME" with constants.

*Why:* Prevents typos across thousands of lines.  
*Files:* `MainActivity.java`  
*Status:* Done: `Prefs.java` holds SharedPreferences file names and keys; `MainActivity` and `SplashActivity` use them (and `LanguageTags.KEY` for the language value).

### CODE-17 · Remove duplicated click handlers  `P2` `M`

Factor the near-identical OnClickListeners for each version list into one helper.

*Why:* Fewer copy-paste bugs.  
*Files:* `MainActivity.java`

### CODE-18 · Package structure  `P3` `M`

Organize into ui/, data/, domain/, util/ packages.

*Why:* Makes the codebase easier to navigate.  
*Files:* `app/src/main/java/`

### CODE-19 · Remove unused imports and warnings  `P3` `S`

Clean up the compiler warnings (deprecations, raw types).

*Why:* Real problems get lost in the noise.  
*Files:* `all Java files`

### CODE-20 · Document architecture decisions (ADRs)  `P3` `S`

Add docs/adr/ explaining the major refactor choices.

*Why:* Helps future maintainers understand the code.  
*Files:* `docs/adr/`

## Build & CI

### CI-01 · CI: lint and upload SARIF  `P2` `S`

Run lint and upload SARIF to GitHub code scanning.

*Why:* Findings show up inline in PRs.  
*Files:* `.github/workflows`  
*Status:* Done: new "Android lint" CI job runs `lintDebug` (errors fail the job), uploads the SARIF report to code scanning (best effort) and the HTML/XML reports as an artifact.

### CI-02 · Release workflow with signing  `P1` `M`

Tag-triggered workflow that builds a signed release APK using encrypted secrets and attaches it to the GitHub Release.

*Why:* Gives repeatable, auditable releases.  
*Files:* `.github/workflows/release.yml`

### CI-03 · Gradle build cache and config cache  `P2` `S`

Enable org.gradle.caching and configuration-cache.

*Why:* Faster local and CI builds.  
*Files:* `gradle.properties`  
*Status:* Done: `org.gradle.caching=true` and `org.gradle.configuration-cache=true`. Verified for assembleDebug, unit tests and lintDebug.

### CI-04 · Gradle wrapper validation  `P1` `S`

Add gradle/actions/wrapper-validation to CI.

*Why:* Protects against a malicious gradle-wrapper.jar.  
*Files:* `.github/workflows`  
*Status:* Done: the unit-test job runs `gradle/actions/wrapper-validation@v4` before building.

### CI-05 · Dependabot / Renovate  `P1` `S`

Automate dependency and Action version bump PRs.

*Why:* Keeps the toolchain current, which this revive branch shows is needed.  
*Files:* `.github/dependabot.yml`

### CI-06 · Build variants: debug applicationIdSuffix  `P2` `S`

Add .debug applicationIdSuffix and a version name suffix so debug and release can coexist.

*Why:* Developers can test without uninstalling their release build.  
*Files:* `app/build.gradle`

### CI-07 · Upgrade to latest AGP/Gradle  `P2` `S`

Track AGP 8.x/9 and Gradle 8.x/9 updates, and the compileSdk 36 readiness.

*Why:* Stays compatible with new Android Studio releases.  
*Files:* `build.gradle, gradle-wrapper.properties`

### CI-08 · Java 17 toolchain declaration  `P2` `S`

Use java { toolchain { languageVersion = 17 } } so builds don't depend on the host JDK.

*Why:* Gives reproducible builds across machines.  
*Files:* `app/build.gradle`  
*Status:* Done: `java { toolchain { languageVersion = 17 } }` in `app/build.gradle` so the compile JDK is pinned regardless of JAVA_HOME.

### CI-09 · Version code from git  `P3` `S`

Derive versionCode and versionName from git tags.

*Why:* No more manual bumps or mismatches.  
*Files:* `app/build.gradle`

### CI-10 · APK size report in CI  `P3` `S`

Post the APK size diff on PRs.

*Why:* Catches bloat early.  
*Files:* `.github/workflows`

### CI-11 · F-Droid metadata  `P3` `S`

Add fastlane/metadata (descriptions, screenshots) for F-Droid and IzzyOnDroid.

*Why:* Helps distribution through trusted channels, if eligible.  
*Files:* `fastlane/metadata/android/`

### CI-12 · Build reproducibility check  `P3` `M`

A CI job that builds twice and diffs the APKs.

*Why:* Proves the build is deterministic.  
*Files:* `.github/workflows`

### CI-13 · Test result publishing  `P3` `S`

Publish JUnit XML results as a PR check (dorny/test-reporter).

*Why:* Shows failing tests directly in the PR UI.  
*Files:* `.github/workflows/android-tests.yml`  
*Status:* Done: the unit-test job publishes JUnit XML with `dorny/test-reporter@v2.7.0` (pinned by SHA; best effort, does not fail the job).

### CI-14 · Pre-commit hooks  `P3` `S`

Add an optional pre-commit hook running spotless and unit tests.

*Why:* Faster feedback for contributors.  
*Files:* `.githooks/`

### CI-15 · Pin GitHub Actions by SHA  `P2` `S`

Reference actions by commit SHA.

*Why:* Guards against supply-chain attacks on tags.  
*Files:* `.github/workflows`  
*Status:* Done: every action in `android-tests.yml` is pinned to a commit SHA with its tag in a comment.

### CI-16 · Matrix build for minSdk device  `P2` `S`

Add an API 21/23 emulator job to the matrix.

*Why:* minSdk is 21, but nothing tests those versions.  
*Files:* `.github/workflows/android-tests.yml`

### CI-17 · Universal + per-ABI release outputs  `P3` `S`

Confirm the app is pure-Java and ship one universal APK, documenting this in the release notes.

*Why:* Avoids user confusion over which APK to download.  
*Files:* `app/build.gradle`

### CI-18 · Make emulator CI ANR-resistant  `P1` `S`

Disable animations, use the -no-snapshot plus hardware-accel runner, raise the dispatch timeout, and wrap launch in IdlingResources rather than sleeps.

*Why:* On the slow emulator the instrumentation aborted with keyDispatchingTimedOut (TEST_REPORT row 21).  
*Files:* `.github/workflows/android-tests.yml, SmokeTest.java`

## Testing

### TEST-01 · Screenshot tests (Roborazzi/Paparazzi)  `P2` `M`

Capture screenshots for each of the 21 themes and key languages.

*Why:* Catches visual regressions in the theme refactor.  
*Files:* `app/src/test`

### TEST-02 · Theme matrix test  `P2` `S`

Parameterized test that applies every theme and asserts no crash plus contrast thresholds.

*Why:* 21 themes are too many to check by hand.  
*Files:* `app/src/test`  
*Status:* Done: `SmallFixesBatch7Test` applies all 21 themes; `SmallFixesBatch8Test` checks header text contrast (WCAG AA). Themes 17 (Light Orange) and 20 (Light Cyan) are recorded as known failures under A11Y-03.

### TEST-03 · Language matrix test  `P2` `S`

Parameterized test that switches every language and asserts that no view keeps the English default.

*Why:* Catches missing translations.  
*Files:* `app/src/test`  
*Status:* Done: `SmallFixesBatch8Test.TEST03_nonEnglishLanguagesDoNotKeepEnglishLabels` applies all 46 languages and requires settings/about/installed/latest/language labels to differ from English (the app name stays "xManager" on purpose).

### TEST-04 · Fake API server for instrumented tests  `P1` `M`

Read api_url from BuildConfig or an injectable config so androidTest can point it at MockWebServer.

*Why:* Makes the populated lists, update dialog and maintenance screen testable end-to-end. Today the lists are empty on the emulator.  
*Files:* `app/src/androidTest, app/build.gradle, res/values/api.xml`

### TEST-05 · Version comparison tests  `P0` `S`

Exhaustive tests for the new semver comparator.

*Why:* Prevents the 5.10 < 5.9 bug from returning.  
*Files:* `app/src/test`

### TEST-06 · Rotation/process-death tests  `P2` `S`

Use ActivityScenario.recreate() to check state is restored.

*Why:* Catches lost state and leaks.  
*Files:* `app/src/androidTest`  
*Status:* Done (JVM): `SmallFixesBatch8Test.TEST06_recreateRestoresThemeAndLanguage` uses Robolectric `recreate()` and checks the theme and language spinners. An instrumented ActivityScenario version can still be added later.

### TEST-07 · Monkey / stress test in CI  `P2` `S`

Run adb shell monkey with a fixed seed for N events on the emulator.

*Why:* Finds random crash paths cheaply.  
*Files:* `.github/workflows`

### TEST-08 · Code coverage with JaCoCo/Kover  `P2` `S`

Generate coverage reports and track them in CI.

*Why:* Shows where tests are missing.  
*Files:* `app/build.gradle`

### TEST-09 · Leak detection (LeakCanary) in debug  `P2` `S`

Add LeakCanary to debug builds and run it during the UI tests.

*Why:* The Timer-based code likely leaks Activities.  
*Files:* `app/build.gradle`

### TEST-10 · Test orchestrator  `P3` `S`

Use AndroidX Test Orchestrator with clearPackageData.

*Why:* Isolates tests from each other's state.  
*Files:* `app/build.gradle`

### TEST-11 · Known-bug tests tracked separately  `P2` `S`

Keep @Ignore'd tests referencing each bug so that fixing a bug means un-ignoring its test.

*Why:* Turns bug reports into executable specs.  
*Files:* `app/src/test/.../KnownBugsTest.java`

### TEST-12 · Manual QA checklist  `P3` `S`

A docs/QA.md release checklist (install, upgrade, themes, languages, offline).

*Why:* Covers what automation doesn't.  
*Files:* `docs/QA.md`

### TEST-13 · Merge the helper/tests suite into revive/build-fix  `P0` `S`

Bring the 51 JVM tests, Robolectric config, SmokeTest, AccessibilityChecksTest and the CI workflow from helper/tests into the main branch.

*Why:* The tests are written and pass except for the intentional bug characterizations; they only help once they're on the main branch.  
*Files:* `helper/tests f66f856, ad79f20`

## Docs & community

### DOC-01 · Update README for the revived fork  `P0` `S`

Explain the fork's status, the differences from the archived upstream, and the build instructions.

*Why:* Users and contributors need to know this is the maintained fork.  
*Files:* `README.md`

### DOC-02 · BUILDING.md  `P1` `S`

Document the JDK 17, SDK and Gradle setup, the test commands and the emulator setup.

*Why:* Lowers the barrier for new contributors.  
*Files:* `docs/BUILDING.md`

### DOC-03 · Enable GitHub Issues and Discussions on the fork  `P1` `S`

Upstream has issues disabled; turn them on with templates.

*Why:* Users need a place to report bugs.  
*Files:* `.github/ISSUE_TEMPLATE`

### DOC-04 · Pull request template  `P2` `S`

Add a PR template with a testing checklist.

*Why:* Improves PR quality.  
*Files:* `.github/pull_request_template.md`  
*Status:* Done: `.github/pull_request_template.md` with a build/test/lint/CI checklist and the scope reminder.

### DOC-05 · CHANGELOG.md (Keep a Changelog)  `P1` `S`

Maintain a human-readable changelog per release.

*Why:* Users and packagers can see what changed.  
*Files:* `CHANGELOG.md`

### DOC-06 · SECURITY.md  `P1` `S`

Add a responsible disclosure policy and contact.

*Why:* This is the standard way to receive security reports.  
*Files:* `SECURITY.md`

### DOC-07 · Architecture overview doc  `P2` `S`

Describe the screens, data flow, API schema and preference keys.

*Why:* The Sketchware-generated code is very hard to understand without one.  
*Files:* `docs/ARCHITECTURE.md`

### DOC-08 · API schema documentation  `P1` `S`

Document every JSON key the app reads, with types and examples (JSON Schema).

*Why:* Lets mirror operators and the backend evolve safely.  
*Files:* `docs/api-schema.json`

### DOC-09 · Screenshots and demo GIF  `P3` `S`

Add current screenshots to README and the store metadata.

*Why:* First impressions matter.  
*Files:* `README.md, fastlane/`

### DOC-10 · FAQ in-repo  `P3` `S`

Move the FAQ link content into docs/FAQ.md, versioned with the code.

*Why:* Keeps the FAQ available if external hosts go down.  
*Files:* `docs/FAQ.md`

### DOC-11 · Contributor guide refresh  `P2` `S`

Update CONTRIBUTING.md with the branch model, code style and test expectations.

*Why:* The existing guide references the archived upstream workflow.  
*Files:* `CONTRIBUTING.md`

### DOC-12 · Good-first-issue labels  `P3` `S`

Seed issues from this backlog with S-effort items labelled good-first-issue.

*Why:* Attracts new contributors.  
*Files:* `GitHub labels`

### DOC-13 · Translation guide  `P2` `S`

Explain how to add or update a translation once strings are in resources and Weblate.

*Why:* Translators aren't developers.  
*Files:* `docs/TRANSLATING.md`

### DOC-14 · Release process doc  `P2` `S`

A step-by-step release checklist: version bump, tag, CI, notes, cert fingerprint.

*Why:* Releases become consistent and auditable.  
*Files:* `docs/RELEASING.md`

### DOC-15 · Code of conduct enforcement contacts  `P3` `S`

Update CODE_OF_CONDUCT.md with the current maintainer contacts.

*Why:* The current contacts point to the archived team.  
*Files:* `CODE_OF_CONDUCT.md`

### DOC-16 · Credit upstream and other forks  `P2` `S`

Credit the Team-xManager authors and notable forks in README.

*Why:* Good open-source etiquette, and required by the license terms.  
*Files:* `README.md`

### DOC-17 · Roadmap  `P3` `S`

Publish a ROADMAP.md derived from this backlog's P0/P1 items.

*Why:* Sets expectations for the community.  
*Files:* `docs/ROADMAP.md`

### DOC-18 · Feature comparison with similar apps  `P3` `S`

Document how xManager relates to Obtainium, Droid-ify and others for inspiration and scope.

*Why:* Clarifies the scope and avoids reinventing features.  
*Files:* `docs/`

### DOC-19 · In-app 'Report a bug' link  `P2` `S`

Add an About-screen link to the fork's issue tracker, pre-filled with the app version and device.

*Why:* Gives better bug reports with less back-and-forth.  
*Files:* `About screen`

### DOC-21 · Document testing without KVM  `P3` `S`

Note in BUILDING.md how to run the emulator with -accel off, hide_error_dialogs and the click-through tools when hardware acceleration is unavailable.

*Why:* Contributors in containers and cloud VMs often lack KVM.  
*Files:* `docs/BUILDING.md, tools/emu_clickthrough.py`

## Review log

Removed or merged 19 draft items so the count reflects distinct, still-open work:

- Testing: “Accessibility checks in Espresso”, duplicate of A11Y 'Accessibility Scanner in CI'
- Security & privacy: “Debug logging opt-in”, merged into PERF 'Stop the always-on logcat reader thread'
- Code health & architecture: “Remove the static logger singleton”, merged into REL 'Restartable logger'
- Security & privacy: “Disable or scope allowBackup”, merged into SET 'Configure backup rules'
- Settings & backup: “Backup to cloud via Android Auto Backup rules”, merged into SET 'Configure backup rules'
- Security & privacy: “Validate deep links / external URL handling”, duplicate of REL 'Validate links before opening'
- Reliability & error handling: “ANR watchdog in debug”, overlaps PERF 'StrictMode in debug builds'
- Code health & architecture: “Single Activity + Navigation graph”, merged into UI 'Split screens into Fragments'
- Testing: “Configurable API URL for tests”, merged into TEST 'Fake API server for instrumented tests'
- Security & privacy: “Explicit intents and exported=false audit”, already satisfied: all 3 manifest components declare android:exported
- Build & CI: “CI: build + unit tests on every PR”, merged into TEST 'Merge the helper/tests suite' (workflow already written)
- Build & CI: “CI: emulator smoke tests”, merged into TEST 'Merge the helper/tests suite' (workflow already written)
- Update checking: “Installed-version detection for managed apps”, done in b382f6c (event-driven installed refresh)
- Testing: “Unit tests for SketchwareUtil”, already delivered on helper/tests (f66f856)
- Testing: “Unit tests for FileUtil”, already delivered on helper/tests (f66f856)
- Testing: “Network layer tests with MockWebServer”, already delivered on helper/tests (f66f856)
- Testing: “API parsing characterization tests”, already delivered on helper/tests (f66f856)
- Testing: “Robolectric activity tests”, already delivered on helper/tests (f66f856)
- Testing: “Espresso smoke test on emulator”, already delivered on helper/tests (f66f856)
