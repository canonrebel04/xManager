package com.xc3fff0e.xmanager;

import static com.xc3fff0e.xmanager.TargetSupport.*;
import static org.junit.Assert.*;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

/**
 * Backlog targets that need the app running (Robolectric). Every test here except
 * HARNESS_fullFixture_loadsVersionList is expected to FAIL until its backlog item is done;
 * see docs/TARGETS.md for what makes each one pass.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class BacklogTargetsTest {

	private static final String TEST_URL = "https://api.test.invalid/api.json";

	@After
	public void tearDown() {
		resetTestApiUrlQuietly();
	}

	private static boolean apiAvailable(MainActivity a) {
		return TargetSupport.<Boolean>field(a, "apiAvailable");
	}

	private static Context ctx() {
		return ApplicationProvider.getApplicationContext();
	}

	// ---------------------------------------------------------------- harness control (passes)

	/** Control: proves the fixture + listener harness works today, so the REL-04/05 failures are real. */
	@Test
	public void HARNESS_fullFixture_loadsVersionList() {
		MainActivity a = launchMain();
		loadFixture(a);
		assertTrue(apiAvailable(a));
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_refresh_layout).getVisibility());
	}

	// ---------------------------------------------------------------- REL-04 Guard JSON parsing

	/**
	 * REL-04: a well-formed JSON object that lacks the required keys (the six version lists,
	 * the four *_Latest values and Server) is rejected up front as a bad response: the
	 * api_bad_response message is shown, apiAvailable stays false and the home screen stays up
	 * (no "Invalid API Response" maintenance screen).
	 */
	@Test
	public void REL04_missingRequiredKeys_isRejectedAsBadResponse() {
		MainActivity a = launchMain();
		deliverResponse(a, "{}");
		settle(1500);
		assertFalse("a document without the required keys was accepted (apiAvailable=true)", apiAvailable(a));
		assertEquals("home screen was replaced by the maintenance screen",
				View.VISIBLE, a.findViewById(R.id.main_refresh_layout).getVisibility());
		assertTrue("api_bad_response not shown; snackbars: " + snackbarTexts(a),
				snackbarTexts(a).contains(a.getString(R.string.api_bad_response)));
	}

	/**
	 * REL-04: a required key with the wrong type (a string where a version list belongs) is
	 * rejected the same way, before any of it is used.
	 */
	@Test
	public void REL04_wrongTypeForRequiredKey_isRejectedAsBadResponse() {
		MainActivity a = launchMain();
		Map<String, Object> d = fullFixture();
		d.put("Stock_Patched", "not a list");
		deliverResponse(a, json(d));
		settle(1500);
		assertFalse("a document with a mistyped Stock_Patched was accepted", apiAvailable(a));
		assertEquals("home screen was replaced by the maintenance screen",
				View.VISIBLE, a.findViewById(R.id.main_refresh_layout).getVisibility());
		assertTrue("api_bad_response not shown; snackbars: " + snackbarTexts(a),
				snackbarTexts(a).contains(a.getString(R.string.api_bad_response)));
	}

	// ---------------------------------------------------------------- REL-05 Null-safe map access

	/**
	 * REL-05: optional keys (Supporters, App_Changelogs, Update, and the Telegram, Discord,
	 * Reddit, Donate, Source, Website and FAQ links) may be missing or null. The version list
	 * still loads, and tapping a link whose key is missing doesn't crash.
	 */
	@Test
	public void REL05_missingOrNullOptionalKeys_stillLoadVersionList() {
		MainActivity a = launchMain();
		Map<String, Object> d = fullFixture();
		d.remove("Supporters");
		d.remove("App_Changelogs");
		d.put("Update", null);
		d.remove("Telegram");
		d.remove("Discord");
		d.put("Reddit", null);
		deliverResponse(a, new com.google.gson.GsonBuilder().serializeNulls().create().toJson(d));
		boolean loaded = waitUntil(() -> STOCK_LATEST.contentEquals(((TextView) a.findViewById(R.id.sub_1)).getText()), 4000);
		settle(500);
		assertTrue("version list not loaded when only optional keys were missing; snackbars: " + snackbarTexts(a), loaded);
		assertEquals("missing optional keys sent the app to the maintenance screen",
				View.VISIBLE, a.findViewById(R.id.main_refresh_layout).getVisibility());
		assertFalse("'Invalid API Response' shown for missing optional keys",
				snackbarTexts(a).contains("Invalid API Response"));
	}

	// ---------------------------------------------------------------- REL-06 Configurable API URL

	/**
	 * REL-06: the update-server source can be chosen in the app. ApiConfig.setUserUrl(Context,
	 * String) saves an https URL and returns true; ApiConfig.url(Context) then returns it (and
	 * falls back to BuildConfig.API_URL when nothing is saved).
	 */
	@Ignore("Out of scope: a user-chosen API source (setUserUrl / settings field) was declined because the API supplies the modified-Spotify download list")
	@Test
	public void REL06_userChosenSource_overridesBuildUrl() {
		Method set = requireMethod(ApiConfig.class, "setUserUrl", Context.class, String.class);
		Method get = requireMethod(ApiConfig.class, "url", Context.class);
		assertEquals("with nothing saved url(Context) must fall back to the build URL",
				ApiConfig.url(), callStatic(get, ctx()));
		assertEquals(Boolean.TRUE, callStatic(set, ctx(), "https://example.org/api.json"));
		assertEquals("https://example.org/api.json", callStatic(get, ctx()));
	}

	/**
	 * REL-06: Settings has an in-app source field (view id settings_api_source) inside the
	 * Settings screen, so a build without -PxmanagerApiUrl is still usable.
	 */
	@Ignore("Out of scope: a user-chosen API source (setUserUrl / settings field) was declined because the API supplies the modified-Spotify download list")
	@Test
	public void REL06_settingsScreen_hasApiSourceField() {
		MainActivity a = launchMain();
		int id = resId(a, "id", "settings_api_source");
		assertNotEquals("no view id 'settings_api_source' (in-app source picker) exists", 0, id);
		View field = a.findViewById(id);
		assertNotNull("settings_api_source is not in MainActivity's layout", field);
		View settings = a.findViewById(R.id.main_scroll_settings);
		boolean inside = false;
		for (View v = field; v != null && v.getParent() instanceof View; v = (View) v.getParent()) {
			if (v.getParent() == settings) { inside = true; break; }
		}
		assertTrue("settings_api_source must live inside main_scroll_settings", inside);
	}

	// ---------------------------------------------------------------- SEC-15 HTTPS-only overrides

	/**
	 * SEC-15: a custom source entered in the app must be https. setUserUrl() returns false for
	 * http:// and leaves the active URL unchanged.
	 */
	@Ignore("Out of scope: a user-chosen API source (setUserUrl / settings field) was declined because the API supplies the modified-Spotify download list")
	@Test
	public void SEC15_httpUserSource_isRejected() {
		Method set = requireMethod(ApiConfig.class, "setUserUrl", Context.class, String.class);
		Method get = requireMethod(ApiConfig.class, "url", Context.class);
		String before = (String) callStatic(get, ctx());
		assertEquals("http:// source was accepted", Boolean.FALSE, callStatic(set, ctx(), "http://example.org/api.json"));
		assertEquals("HTTP:// (upper case) source was accepted", Boolean.FALSE, callStatic(set, ctx(), "HTTP://example.org/api.json"));
		assertEquals("rejected source still became active", before, callStatic(get, ctx()));
	}

	// ---------------------------------------------------------------- REL-10 Stable language codes

	private static int indexOf(Spinner s, String label) {
		for (int i = 0; i < s.getAdapter().getCount(); i++) {
			if (label.equals(String.valueOf(s.getAdapter().getItem(i)))) return i;
		}
		fail("language '" + label + "' not in the picker");
		return -1;
	}

	private static String selectLanguage(MainActivity a, String label) {
		Spinner s = a.findViewById(R.id.select_language);
		int i = indexOf(s, label);
		s.getOnItemSelectedListener().onItemSelected(s, null, i, i);
		idle();
		return a.getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE).getString("LANGUAGE", "");
	}

	/**
	 * REL-10: the saved language is a stable language tag (e.g. "de", "de-x-formal"), not a
	 * spinner index, so adding or reordering languages can't change a user's choice. Both German
	 * entries get distinct tags that start with "de".
	 */
	@Test
	public void REL10_selectedLanguage_isSavedAsLanguageTag() {
		MainActivity a = launchMain();
		String informal = selectLanguage(a, "German (informal, du)");
		String formal = selectLanguage(a, "German (formal, Sie)");
		String russian = selectLanguage(a, "Russian");
		for (String code : new String[] {informal, formal, russian}) {
			assertFalse("language saved as a spinner index: '" + code + "'", code.matches("\\d+"));
			assertTrue("not a language tag: '" + code + "'", code.matches("[a-z]{2,3}([-_][A-Za-z0-9]{1,8})*"));
		}
		assertTrue(informal.startsWith("de"));
		assertTrue(formal.startsWith("de"));
		assertNotEquals("both German variants saved the same code", informal, formal);
		assertTrue("Russian should be saved as 'ru', got '" + russian + "'", russian.startsWith("ru"));
	}

	/**
	 * REL-10: an existing install that saved an index (old format) is migrated on launch to the
	 * matching language tag, and the same language stays selected.
	 */
	@Test
	public void REL10_legacyIndexPreference_isMigratedToTag() {
		MainActivity first = launchMain();
		Spinner s = first.findViewById(R.id.select_language);
		int russian = indexOf(s, "Russian");
		SharedPreferences p = ctx().getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE);
		p.edit().putString("LANGUAGE", String.valueOf(russian)).commit();
		MainActivity a = launchMain();
		settle(300);
		String saved = p.getString("LANGUAGE", "");
		assertTrue("legacy index '" + russian + "' not migrated to a language tag (now '" + saved + "')", saved.startsWith("ru"));
		Spinner s2 = a.findViewById(R.id.select_language);
		assertEquals("Russian", String.valueOf(s2.getSelectedItem()));
	}

	// ---------------------------------------------------------------- PERF-09 Debounce refresh

	/**
	 * PERF-09: a pull-to-refresh within the debounce window (measured with SystemClock, a window
	 * of at most 30 s) after a successful fetch is ignored: no request goes out and the spinner
	 * stops at once instead of replaying the ~2.7 s list animation.
	 */
	@Test
	public void PERF09_refreshRightAfterSuccessfulFetch_isIgnored() {
		MainActivity a = launchMain();
		FakeConnection fake = installFakeConnection(a);
		fake.responseBody = json(fullFixture());
		loadFixture(a);
		triggerPullToRefresh(a);
		idle();
		SwipeRefreshLayout srl = a.findViewById(R.id.main_refresh_layout);
		assertEquals("refresh inside the debounce window sent a request", 0, fake.urls.size());
		assertFalse("refresh inside the debounce window still ran (spinner on)", srl.isRefreshing());
		assertFalse("refresh inside the debounce window still ran; snackbars: " + snackbarTexts(a),
				snackbarTexts(a).contains("Refreshing Data List..."));
	}

	// ---------------------------------------------------------------- UI-15 Pull-to-refresh feedback

	/** Loads data, points the app at TEST_URL with a fake connection, then moves past PERF-09's window. */
	private static FakeConnection loadedAndOutsideDebounce(MainActivity a) {
		FakeConnection fake = installFakeConnection(a);
		loadFixture(a);
		setTestApiUrl(TEST_URL); // needs TEST-04's hook; fails with "missing API" until it exists
		ShadowLooper.idleMainLooper(10, TimeUnit.MINUTES);
		return fake;
	}

	/**
	 * UI-15: a pull-to-refresh on loaded data fetches from the server again, and when that fetch
	 * fails the Snackbar reports the error (it contains the HTTP status) instead of
	 * "Data List Updated".
	 */
	@Test
	public void UI15_refreshFailure_reportsErrorInsteadOfUpdated() {
		MainActivity a = launchMain();
		FakeConnection fake = loadedAndOutsideDebounce(a);
		fake.errorMessage = "HTTP 503 Service Unavailable";
		triggerPullToRefresh(a);
		settle(3500);
		List<String> shown = snackbarTexts(a);
		assertEquals("refresh did not fetch from the server", 1, fake.urls.size());
		for (String s : shown) {
			assertFalse("failed refresh was reported as success: " + shown, s.startsWith("Data List Updated"));
		}
		assertTrue("failed refresh didn't explain the error; snackbars: " + shown,
				shown.stream().anyMatch(s -> s.contains("503")));
		assertFalse(((SwipeRefreshLayout) a.findViewById(R.id.main_refresh_layout)).isRefreshing());
	}

	/**
	 * UI-15: when the server returns the same data, the Snackbar says so with
	 * R.string.refresh_no_changes instead of claiming the list was updated.
	 */
	@Test
	public void UI15_refreshWithSameData_reportsNoChanges() {
		MainActivity a = launchMain();
		FakeConnection fake = loadedAndOutsideDebounce(a);
		fake.responseBody = json(fullFixture());
		triggerPullToRefresh(a);
		settle(3500);
		assertEquals("refresh did not fetch from the server", 1, fake.urls.size());
		int id = resId(a, "string", "refresh_no_changes");
		assertNotEquals("no string resource 'refresh_no_changes'", 0, id);
		List<String> shown = snackbarTexts(a);
		assertTrue("unchanged refresh not reported as 'no changes'; snackbars: " + shown, shown.contains(a.getString(id)));
	}

	// ---------------------------------------------------------------- UI-16 Empty / error state

	/**
	 * UI-16: when the version list can't be loaded (here, no API URL), a dedicated error view
	 * (id api_error_view) is visible with a Retry button (id api_error_retry) that is clickable
	 * and has a text or content description.
	 */
	@Test
	public void UI16_apiUnavailable_showsErrorViewWithRetry() {
		MainActivity a = launchMain();
		ShadowLooper.idleMainLooper(5, TimeUnit.SECONDS);
		int viewId = resId(a, "id", "api_error_view");
		int retryId = resId(a, "id", "api_error_retry");
		assertNotEquals("no view id 'api_error_view' exists", 0, viewId);
		assertNotEquals("no view id 'api_error_retry' exists", 0, retryId);
		View errorView = a.findViewById(viewId);
		View retry = a.findViewById(retryId);
		assertNotNull("api_error_view not in MainActivity's layout", errorView);
		assertNotNull("api_error_retry not in MainActivity's layout", retry);
		assertEquals("error view hidden while nothing is loaded", View.VISIBLE, errorView.getVisibility());
		assertTrue("Retry is not clickable", retry.isClickable() && retry.isEnabled());
		CharSequence label = retry instanceof TextView ? ((TextView) retry).getText() : retry.getContentDescription();
		assertTrue("Retry has no visible label", label != null && label.length() > 0);
	}

	/**
	 * UI-16: tapping Retry sends a new request and hides the error view once data arrives.
	 */
	@Test
	public void UI16_retry_requestsAgainAndHidesErrorView() {
		MainActivity a = launchMain();
		ShadowLooper.idleMainLooper(5, TimeUnit.SECONDS);
		int viewId = resId(a, "id", "api_error_view");
		int retryId = resId(a, "id", "api_error_retry");
		assertNotEquals("no view id 'api_error_view' exists", 0, viewId);
		assertNotEquals("no view id 'api_error_retry' exists", 0, retryId);
		FakeConnection fake = installFakeConnection(a);
		fake.responseBody = json(fullFixture());
		setTestApiUrl(TEST_URL);
		a.findViewById(retryId).performClick();
		boolean loaded = waitUntil(() -> STOCK_LATEST.contentEquals(((TextView) a.findViewById(R.id.sub_1)).getText()), 4000);
		assertEquals("Retry did not send a request", 1, fake.urls.size());
		assertTrue("data from Retry never loaded", loaded);
		assertEquals("error view still visible after a successful retry", View.GONE, a.findViewById(viewId).getVisibility());
	}

	// ---------------------------------------------------------------- UI-24 LATEST label

	/**
	 * UI-24: with no data loaded, the visible LATEST value no longer reads "N/A"; it explains
	 * the problem (e.g. "Couldn't load versions – tap to retry").
	 */
	@Test
	public void UI24_noData_latestLabelExplainsInsteadOfNA() {
		MainActivity a = launchMain();
		ShadowLooper.idleMainLooper(5, TimeUnit.SECONDS);
		idle();
		int checked = 0;
		for (int id : new int[] {R.id.sub_1, R.id.sub_2, R.id.sub_3, R.id.sub_4}) {
			TextView t = a.findViewById(id);
			if (t.getVisibility() != View.VISIBLE) continue;
			checked++;
			String text = t.getText().toString().trim();
			assertNotEquals("LATEST label " + a.getResources().getResourceEntryName(id) + " still reads N/A", "N/A", text);
			assertFalse("LATEST label is blank", text.isEmpty());
		}
		assertTrue("no LATEST label visible", checked > 0);
	}

	// ---------------------------------------------------------------- TEST-04 Fake API server

	/**
	 * TEST-04: tests can point the app at a fake server at run time with
	 * ApiConfig.setUrlForTesting(String) (null resets; the SEC-15 https rule does not apply to it and is
	 * only for tests). With a MockWebServer serving the fixture, MainActivity requests it and
	 * shows its LATEST version.
	 */
	@Test
	public void TEST04_mockWebServerFixture_populatesVersionList() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setResponseCode(200).setBody(json(fullFixture())));
			server.start();
			setTestApiUrl(server.url("/api.json").toString());
			MainActivity a = launchMain();
			boolean loaded = waitUntil(() -> STOCK_LATEST.contentEquals(((TextView) a.findViewById(R.id.sub_1)).getText()), 8000);
			assertTrue("app never requested the fake server", server.getRequestCount() >= 1);
			assertTrue("fixture from the fake server never reached the LATEST label", loaded);
		}
	}
}
