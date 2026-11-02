# xManager test report (helper/tests)

- **Code under test:** `revive/build-fix` @ `de64cbe` (xManager Dev's 7 newest commits, incl. `b382f6c` event-driven installed refresh, `b29202e` a11y labels, `1f992e5` string resources) + test commit on `helper/tests`.
- **Date:** 2026-11-01 (America/Chicago)
- **Emulator:** AVD `helper30` (pixel_4, API 30, x86_64 default image), **software CPU emulation (`-accel off`)**. KVM is exposed on the box, but vCPU creation faults in the host kernel (`kvm_spurious_fault` in dmesg), so hardware acceleration is not usable. TCG makes everything 10–20x slower, so System UI and input-dispatch ANRs are environmental (the dialogs were auto-dismissed with "Wait"). The emulator was also OOM-killed once by other workloads on the box and restarted with 1.5 GB RAM.
- **Dummy packages:** to exercise the installed / not-installed labels, two empty stub APKs (`hasCode=false`, versionName `0.0.1-dummy`) were built with aapt2 under the package names the app queries (`com.spotify.music`, `com.spotify.musix`). They contain no code and were installed and uninstalled only on the emulator.

## 1. Emulator click-through (adb + uiautomator, `tools/emu_clickthrough.py`, `tools/run_*.py`)

Screenshots, UI dumps and logs are in `docs/emulator/`.

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | Splash shows, then hands off to MainActivity | **PASS** | `docs/emulator/01_splash.png`, then MainActivity resumed |
| 2 | Main list rows render (Stock / AMOLED / Other patched, Miscellaneous, Manager tools, socials) | **PASS** | `02_main_top.png`, `main_top.xml` |
| 3 | "Versions" rows tappable | **PASS** (lists empty: `api_url` is empty, so there is no data and no error message) | `03_main_versions_expanded.png` |
| 4 | Main screen scrolls to bottom | **PASS** | `04_main_scrolled_bottom.png` |
| 5 | Settings opens (no StackOverflow on device) | **PASS** | `05_settings_top.png`, `settings_top.xml` |
| 6 | Settings scrolls (top, middle, bottom) | **PASS** | `06_settings_scrolled.png`, `07_settings_bottom.png` |
| 7 | About opens and scrolls (credits list) | **PASS** | `08_about_top.png`, `09_about_scrolled.png`, `10_about_bottom.png` |
| 8 | Back from About returns to main | **PASS** | `10b_after_back_from_about.png` |
| 9 | Experiments opens | **PASS** | `11_experiments.png`, `experiments.xml` |
| 10 | Installed label, nothing installed → `INSTALLED N/A` | **PASS** | `12_installed_none.png` |
| 11 | Install `com.spotify.music` stub while app is in foreground → label updates live to `0.0.1-dummy` (PACKAGE_ADDED receiver) | **PASS** | `13_installed_music_live.png` |
| 12 | Install the cloned stub too → both labels shown (`0.0.1-dummy │ 0.0.1-dummy`) | **PASS** | `14_installed_both_live.png` |
| 13 | Background the app, uninstall `com.spotify.music`, bring it back → only the cloned label shows (onResume refresh) | **PASS** | `15_installed_cloned_only_after_resume.png` |
| 14 | Uninstall the cloned stub while in foreground → back to `N/A` live (PACKAGE_REMOVED receiver) | **PASS** | `16_installed_none_again_live.png` |
| 15 | No app crashes during click-through (`logcat -b crash`) | **PASS** (crash buffer empty) | `logcat_crash.txt` (empty) |
| 16 | TalkBack labels on icon-only buttons (uiautomator dump) | **PASS**: all 16 icon buttons labelled (About, App settings, Check for updates, Clear offline cache, Close, Discord, Donate, Experimental settings, FAQ, Launch app, Reddit, Settings, Source code, Support, Uninstall, Website) | `main_top.xml`, `tools/a11y_audit.py` |
| 17 | TalkBack labels on Switches | **FAIL**: 5 unlabelled `Switch` widgets: `list_auto_refresh_switch`, `disable_notification_switch`, `hide_stock_patched_switch`, `hide_amoled_patched_switch`, `cloned_version_switch` (Experiments). Their label is a sibling TextView with no `labelFor` or contentDescription. | `settings_top.xml`, `experiments.xml` |
| 18 | Clickable container with no label | **FAIL (minor)**: `main_box_1` (main screen, 968x61 px) is clickable but has no text or description | `main_bottom.xml` |
| 19 | Espresso `AccessibilityChecks` (ATF), main screen | **FAIL**: 7 TouchTargetSize errors: `box_experiment`, `box_switch`, `box_update` are 32x32dp; `box_uninstall`, `box_settings`, `box_cache`, `box_open` are 32dp high | `docs/emulator/atf_results.txt` |
| 20 | Espresso `AccessibilityChecks` (ATF), settings | **FAIL**: 7 TouchTargetSize errors: `box_settings_icon_close` is 32x32dp; 4 switches are 47x20dp; `box_reboot` and `box_reset_preferences` are 34dp high | `atf_results.txt` |
| 21 | Espresso `SmokeTest.coldLaunch_fromLauncher_reachesMainScreen` | **ERROR (environment)**: the instrumentation run was aborted by `keyDispatchingTimedOut` on SplashActivity under TCG; the remaining SmokeTest methods didn't run. The same flow passed in the adb click-through (rows 1–9). A rerun of `visitAllScreens` / `rotate_doesNotCrash` was interrupted. | logcat excerpt below, `logcat_connected_tests.txt` |

ANR logcat excerpt (environmental, software-emulated CPU):
```
11-01 21:01:06.413 W/WindowManager(  545): Cannot find window which accessibility connection is added to
11-01 21:01:06.419 V/KeyguardUpdateMonitor( 1646): SubInfo:{id=1 iccId=89014103211118510720 simSlotIndex=0 carrierId=1 displayName=T-Mobile carrierName=T-Mobile nameSource=3 iconTint=-16746133 number=+15555215554 dataRoaming=0 iconBitmap=android.graphics.Bitmap@d2997cb mcc=310 mnc=260 countryIso=us 
11-01 21:01:06.621 I/binder  (    0): release 3962:3975 transaction 155010 in, still active
11-01 21:01:06.621 I/binder  (    0): send failed reply for transaction 155010 to 545:4107
11-01 21:01:06.648 I/ActivityManager(  545): Failure reporting to instrumentation watcher: comp=ComponentInfo{com.xc3fff0e.xmanager.test/androidx.test.runner.AndroidJUnitRunner} results=Bundle[{shortMsg=keyDispatchingTimedOut, longMsg=Input dispatching timed out (5adb8d8 com.xc3fff0e.xmanager/com.xc
11-01 21:01:07.264 I/cmd     ( 4113): oneway function results will be dropped but finished with status OK and parcel size 4
11-01 21:01:08.704 E/TaskPersister(  545): File error accessing recents directory (directory doesn't exist?).
11-01 21:01:19.206 W/livelock(  464): sample 1.868s
```

**Takeaways from the emulator:** the new event-driven installed refresh (`b382f6c`) behaves correctly for all four transitions, both live via the package receiver and via onResume. The a11y-label commit (`b29202e`) covers every icon button. What remains: the switch labels (`android:labelFor`, or contentDescription / `setStateDescription`) and 48dp touch targets (`minWidth`/`minHeight="48dp"` or padding) on the 14 views listed above.

## 2. JVM unit tests (`./gradlew :app:testDebugUnitTest`, JDK 17, Robolectric 4.14.1 sdk=34)

**51 tests, 45 pass, 6 fail.** All 6 failures are intentional characterizations of real bugs.

| Class | Tests | Fail | Notes |
|---|---|---|---|
| SketchwareUtilTest | 12 | 2 | Bug #1, Bug #5 |
| FileUtilTest | 10 | 0 | |
| RequestNetworkControllerTest (MockWebServer) | 8 | 0 | documents that HTTP 503/204 go to `onResponse` (Bug #6) |
| ApiResponseParsingTest | 11 | 2 | Bug #2 |
| ActivitiesRobolectricTest | 10 | 2 | Bug #3, Bug #4 |

### Bugs found
1. **`SketchwareUtil.sortListMap` numeric comparator never returns +1** (`a<b?-1:a<b?1:0`). Small lists sort by luck; a 200-element shuffled list comes out wrong (`index 0 expected:<0> but was:<7>`). Fix: `Integer.compare(a, b)`.
2. **Version check uses `Double.parseDouble`.** "5.10" counts as older than "5.9" (which would show the maintenance screen), and a three-part "5.9.1" throws NumberFormatException. Fix: a semver comparator.
3. **Duplicate language entry**: "German" appears twice in `_Language_Pack`, shifting the indexes of later languages.
4. **Theme spinner re-entrancy**: `select_theme.onItemSelected` → `_Theme_UI()` → `select_theme.setSelection()` → `onItemSelected`… Under Robolectric's synchronous dispatch this recurses to a StackOverflowError (`MainActivity.java` `_Theme_UI` ↔ the `select_theme` listener). **It doesn't reproduce on the API 30 emulator**, because the framework only fires on an actual position change, so it is latent rather than user-facing. It's still worth a guard (`if (pos != current) …`).
5. **Numeric sort on Gson numbers**: `Integer.valueOf("1.0")` throws, and Gson turns JSON numbers into Doubles.
6. **HTTP errors treated as success**: `RequestNetworkController` passes 4xx/5xx bodies to `onResponse`. An HTML error page then hits Gson as a `JsonSyntaxException`, and `onErrorResponse` is a no-op, so the user gets no feedback.
7. **`api_url` is empty** in `res/values/api.xml`, so on device all lists stay N/A and nothing tells the user why.
8. **`xManagerLogger` can only start once per process.** A second `Application.onCreate` throws "Logger already running", and `isRunning` is never reset on failure. The tests work around this with `application=android.app.Application` in `robolectric.properties`.
9. **The splash title selection is skewed**: re-rolling `getRandom` on each else-branch biases the result toward the early entries.

## 3. Files added
- `app/src/test/java/com/xc3fff0e/xmanager/{SketchwareUtilTest,FileUtilTest,RequestNetworkControllerTest,ApiResponseParsingTest,ActivitiesRobolectricTest}.java`, `app/src/test/resources/robolectric.properties`
- `app/src/androidTest/java/com/xc3fff0e/xmanager/{SmokeTest,AccessibilityChecksTest}.java`
- `.github/workflows/android-tests.yml`: unit-test job plus emulator job with `reactivecircus/android-emulator-runner@v2` on API 30 and 34 (KVM udev rule, AVD cache, logcat artifacts). Not pushed.
- `app/build.gradle`: test runner, testOptions, and test/androidTest dependencies (incl. espresso-accessibility)
- `tools/emu_clickthrough.py`, `tools/run_clickthrough.py`, `tools/run_part{2,3,4}.py`, `tools/a11y_audit.py`
- `docs/emulator/*` (17 screenshots, 6 UI dumps, results JSON, ATF output, logcat)

## 4. How to reproduce
```
export JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:/opt/android-sdk/platform-tools:$PATH
./gradlew :app:testDebugUnitTest --continue
emulator -avd helper30 -no-window -no-audio -gpu swiftshader_indirect -accel off   # or -accel on with working KVM
./gradlew :app:connectedDebugAndroidTest
python3 tools/run_clickthrough.py   # needs app installed; stubs from /tmp/dummy for the installed checks
```
