# Backlog targets: failing tests

Branch `targets/backlog`, based on `verify/pr2` (PR #2 head `c7e0129` plus the verification commits).

Each test below **fails on purpose today** and passes once its backlog item (see `docs/ENHANCEMENTS.md`) is done. They are all JVM tests (JUnit 4 and Robolectric) that run with:

```
./gradlew :app:testDebugUnitTest --continue
```

None of them needs an emulator.

- **`BacklogTargetsTest`** (Robolectric) drives `MainActivity` through its real code paths: the network listener `_Connection_request_listener`, the pull-to-refresh listener and the spinners.
- **`BacklogTargetsRepoTest`** (plain JUnit) checks the CI workflow and the `androidTest` sources. Those only run on an emulator, so from the JVM we check the files that control what CI does.
- **`TargetSupport`** holds the shared helpers:
  - a complete API fixture;
  - `FakeConnection`, which replaces `MainActivity.Connection` so no real network is used;
  - a Snackbar reader;
  - reflection lookups for APIs that don't exist yet. When an API is missing, the test fails with `missing API: ...` instead of breaking the build.
- **`HARNESS_fullFixture_loadsVersionList`** is a control test that **passes**. It shows that the fixture and the listener harness work today, so the REL-04 and REL-05 failures are real.

No production code was changed on this branch. No stubs were added: the missing APIs are looked up by name, so the app can't ship a stub that does nothing.

## Targets

| ID | Test name | What makes it pass |
|---|---|---|
| REL-04 | `BacklogTargetsTest.REL04_missingRequiredKeys_isRejectedAsBadResponse` | The response `{}` (valid JSON but missing the required keys) is rejected before `_Backbone()` runs: `apiAvailable` stays false, the `api_bad_response` Snackbar is shown, and `main_refresh_layout` stays VISIBLE (no "Invalid API Response" maintenance screen). The required keys are the six version lists, the four `*_Latest` values and `Server`. |
| REL-04 | `BacklogTargetsTest.REL04_wrongTypeForRequiredKey_isRejectedAsBadResponse` | The same handling when a required key has the wrong type (`"Stock_Patched": "not a list"`). |
| REL-05 | `BacklogTargetsTest.REL05_missingOrNullOptionalKeys_stillLoadVersionList` | The optional keys can be missing or null and the version list still loads: no maintenance screen and no "Invalid API Response". The optional keys are `Supporters`, `App_Changelogs`, `Update`, and the `Telegram`, `Discord`, `Reddit`, `Donate`, `Source`, `Website` and `FAQ` links. Use getOrDefault-style access with type checks. Today the null `Update` (or missing `Supporters`) throws an NPE inside `_Backbone()`. |
| REL-06 | `BacklogTargetsTest.REL06_userChosenSource_overridesBuildUrl` | New `ApiConfig.setUserUrl(Context, String)` saves an https URL and returns `true`. New `ApiConfig.url(Context)` returns the saved URL, or `BuildConfig.API_URL` when nothing is saved. `MainActivity._API()` should use `url(Context)`. |
| REL-06 | `BacklogTargetsTest.REL06_settingsScreen_hasApiSourceField` | Settings (`main_scroll_settings`) contains an in-app source field with view id `settings_api_source`. |
| REL-10 | `BacklogTargetsTest.REL10_selectedLanguage_isSavedAsLanguageTag` | Picking a language saves a language tag in pref `LANGUAGE/LANGUAGE`, not a spinner index. The two German entries get distinct tags that both start with `de` (e.g. `de`, `de-x-formal`), and Russian is saved as `ru…`. |
| REL-10 | `BacklogTargetsTest.REL10_legacyIndexPreference_isMigratedToTag` | On launch, an old index value (e.g. `"10"` for Russian) is rewritten to its tag, and the same language stays selected. |
| SEC-15 | `BacklogTargetsTest.SEC15_httpUserSource_isRejected` | `ApiConfig.setUserUrl()` returns `false` for `http://` and `HTTP://` URLs and leaves `url(Context)` unchanged. The build-time `-PxmanagerApiUrl` stays as it is, and the existing `ApiConfigTest` still allows http there. Cleartext is already blocked at runtime by `network_security_config.xml`. |
| PERF-09 | `BacklogTargetsTest.PERF09_refreshRightAfterSuccessfulFetch_isIgnored` | A pull-to-refresh right after a successful fetch is ignored: no request goes out, `isRefreshing()` is false at once, and the "Refreshing Data List..." Snackbar doesn't appear. The window must be measured with `SystemClock` (elapsedRealtime or uptimeMillis) and be at most 30 s, because the UI-15 tests move Robolectric's clock 10 minutes ahead to get past it. |
| UI-15 | `BacklogTargetsTest.UI15_refreshFailure_reportsErrorInsteadOfUpdated` | A pull-to-refresh on loaded data fetches from the server again. Today it only replays the in-memory lists. When that fetch fails (`HTTP 503 …`), the Snackbar shows the error (its text contains `503`; `api_unreachable` is a good fit) and never "Data List Updated". The spinner stops afterwards. |
| UI-15 | `BacklogTargetsTest.UI15_refreshWithSameData_reportsNoChanges` | The refresh fetches again, and when the data hasn't changed the Snackbar shows the new string `R.string.refresh_no_changes`. |
| UI-16 | `BacklogTargetsTest.UI16_apiUnavailable_showsErrorViewWithRetry` | When nothing can be loaded (no API URL in unit tests), a dedicated view `api_error_view` is VISIBLE. It contains a clickable, enabled Retry control `api_error_retry` with a text or content description. |
| UI-16 | `BacklogTargetsTest.UI16_retry_requestsAgainAndHidesErrorView` | Tapping `api_error_retry` sends one request, and the error view goes GONE once data loads. |
| UI-24 | `BacklogTargetsTest.UI24_noData_latestLabelExplainsInsteadOfNA` | With no data loaded, the visible LATEST values (`sub_1` to `sub_4`) don't read `N/A`. They explain the problem, e.g. "Couldn't load versions – tap to retry". |
| TEST-04 | `BacklogTargetsTest.TEST04_mockWebServerFixture_populatesVersionList` | New `ApiConfig.setUrlForTesting(String)` (package-private or `@VisibleForTesting`; `null` resets it) overrides the URL at run time, skips the SEC-15 https rule, and is used only by tests. With the hook in place, a MockWebServer fixture reaches the LATEST label. Verified locally with a throw-away three-line hook: this test passed, and the hook was not committed. |
| TEST-04 | `BacklogTargetsRepoTest.TEST04_androidTest_usesMockWebServer` | `app/build.gradle` has `androidTestImplementation 'com.squareup.okhttp3:mockwebserver:…'`, and at least one `androidTest` class uses `MockWebServer` together with `ApiConfig.setUrlForTesting`. On a device that also needs cleartext allowed for `127.0.0.1`/`localhost` in a debug or androidTest network-security config (or MockWebServer with TLS from `okhttp-tls`). |
| A11Y-11 | `BacklogTargetsRepoTest.A11Y11_ciRunsAccessibilityChecksAsBlockingStep` | `.github/workflows/android-tests.yml` names `AccessibilityChecksTest` in its own step (e.g. `-Pandroid.testInstrumentationRunnerArguments.class=com.xc3fff0e.xmanager.AccessibilityChecksTest`), and that step has no `continue-on-error: true`. |
| A11Y-11 | `BacklogTargetsRepoTest.A11Y11_accessibilityChecks_haveSuppressionList` | `AccessibilityChecksTest` sets a suppression list for known issues (`AccessibilityChecks.enable().setSuppressingResultMatcher(...)`), and the list shrinks as issues are fixed. |
| CI-18 | `BacklogTargetsRepoTest.CI18_smokeTest_hasNoFixedSleeps` | `SmokeTest` has no `sleep(...)` or `Thread.sleep(...)` calls (15 today). It waits on state instead, using an IdlingResource, `UiDevice.wait(Until…)`/`waitForIdle`, or Espresso sync. |
| CI-18 | `BacklogTargetsRepoTest.CI18_smokeTest_launchTimeoutAtLeast30s` | `SmokeTest.TIMEOUT` is at least `30_000` ms. It's 15 000 ms today, and that timed out on a software-rendered emulator (`docs/emulator/PR2_RESULTS.md`). |

### Order of work

Some tests depend on APIs from other items:

- **`setUrlForTesting` (TEST-04) comes first.** UI-15 and `UI16_retry_…` need a configured URL to send any request, so they call `ApiConfig.setUrlForTesting` and fail with `missing API: ApiConfig.setUrlForTesting(String)` until TEST-04's hook exists. With the hook added, they fail for their own reasons: "refresh did not fetch from the server" for UI-15, and the missing `api_error_view` for UI-16 (checked locally).
- **REL-06 and SEC-15 share `ApiConfig.setUserUrl(Context, String)` and `ApiConfig.url(Context)`.**

## Checklist: what the JVM can't check

These parts of the items can only be confirmed by running CI on an emulator. The tests above check the inputs (the workflow and the test sources), not the result.

- [ ] **A11Y-11:** The ATF step goes red on a new ERROR-level finding. To check, add a deliberately unlabeled ImageView on a scratch branch and see the step fail. Why it's manual: ATF only runs on a device or emulator through Espresso.
- [ ] **A11Y-11:** Each suppression entry names an issue and a backlog ID, and it is removed when that issue is fixed. Why it's manual: this is a review rule.
- [ ] **CI-18:** `emulator-smoke` passes 5 runs in a row on both API 30 and 34 with no `ANR in com.xc3fff0e.xmanager` in the uploaded `logcat.txt`. Why it's manual: being resistant to ANRs is a property of runs on the CI runner, and we can't run an emulator here.
- [ ] **CI-18:** Animations stay disabled. This is already true (`disable-animations: true` in the test step and `testOptions.animationsDisabled = true`), so it only needs to be kept, and there's no failing test for it. `disable-animations: false` is used only in the snapshot-creation step, which is fine.
- [ ] **CI-18:** `coldLaunch_fromLauncher_reachesMainScreen` passes on the CI emulator. It timed out locally under software rendering (TCG), which may not reflect CI with KVM.
- [ ] **TEST-04:** The new MockWebServer `androidTest` passes under `connectedDebugAndroidTest`. Why it's manual: it needs an emulator, and the static test above only checks that the test exists.
