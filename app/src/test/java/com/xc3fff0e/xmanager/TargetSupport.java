package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.gson.Gson;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.robolectric.Robolectric;

/**
 * Shared helpers for the backlog target tests (see docs/TARGETS.md). They drive MainActivity
 * through its real code paths: the network listener, the SwipeRefreshLayout listener and the
 * spinners. APIs that don't exist yet are looked up by reflection so the tests compile today
 * and fail with a message naming the missing piece.
 */
final class TargetSupport {
	private TargetSupport() { }

	static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	/** MainActivity work hops through real java.util.Timer threads, so wait in real time. */
	static boolean waitUntil(BooleanSupplier condition, long timeoutMs) {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			idle();
			if (condition.getAsBoolean()) return true;
			try { Thread.sleep(40); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
		idle();
		return condition.getAsBoolean();
	}

	static void settle(long ms) {
		waitUntil(() -> false, ms);
	}

	/**
	 * Launches MainActivity as a returning user (first-run optimisation and refetch already
	 * done, regular build selected), i.e. the home screen with the LATEST labels, like
	 * androidTest FirstRun.markDone(). A fresh install shows the optimisation screen instead.
	 */
	static MainActivity launchMain() {
		Context c = androidx.test.core.app.ApplicationProvider.getApplicationContext();
		c.getSharedPreferences("INITIALIZATION", Context.MODE_PRIVATE).edit().putString("INITIALIZATION", "X").commit();
		c.getSharedPreferences("REFETCH", Context.MODE_PRIVATE).edit().putString("REFETCH", "X").commit();
		if (!c.getSharedPreferences("SWITCH_VERSION", Context.MODE_PRIVATE).contains("SWITCH")) {
			c.getSharedPreferences("SWITCH_VERSION", Context.MODE_PRIVATE).edit().putString("SWITCH", "A").commit();
		}
		MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
		idle();
		return a;
	}

	// ---- reflection on MainActivity's private state ----

	@SuppressWarnings("unchecked")
	static <T> T field(Object target, String name) {
		try {
			Field f = target.getClass().getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(target);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("field " + name + " not found on " + target.getClass().getSimpleName(), e);
		}
	}

	static void setField(Object target, String name, Object value) {
		try {
			Field f = target.getClass().getDeclaredField(name);
			f.setAccessible(true);
			f.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("field " + name + " not found on " + target.getClass().getSimpleName(), e);
		}
	}

	/** Feeds a body to the activity exactly as RequestNetworkController would on HTTP 200. */
	static void deliverResponse(MainActivity a, String body) {
		RequestNetwork.RequestListener l = field(a, "_Connection_request_listener");
		l.onResponse("null", body, new HashMap<>());
	}

	static void triggerPullToRefresh(MainActivity a) {
		SwipeRefreshLayout srl = a.findViewById(R.id.main_refresh_layout);
		SwipeRefreshLayout.OnRefreshListener listener = null;
		for (Field f : SwipeRefreshLayout.class.getDeclaredFields()) {
			if (SwipeRefreshLayout.OnRefreshListener.class.isAssignableFrom(f.getType())) {
				try {
					f.setAccessible(true);
					listener = (SwipeRefreshLayout.OnRefreshListener) f.get(srl);
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
			}
		}
		assertNotNull("main_refresh_layout has no OnRefreshListener", listener);
		listener.onRefresh();
	}

	/** Replaces MainActivity.Connection so no real network is used; answers on the main looper. */
	static final class FakeConnection extends RequestNetwork {
		final List<String> urls = new ArrayList<>();
		private final Activity activity;
		volatile String responseBody;
		volatile String errorMessage;

		FakeConnection(Activity activity) {
			super(activity);
			this.activity = activity;
		}

		@Override
		public void startRequestNetwork(String method, String url, String tag, RequestListener listener) {
			urls.add(url);
			new Handler(Looper.getMainLooper()).post(() -> {
				if (errorMessage != null) listener.onErrorResponse(tag, errorMessage);
				else listener.onResponse(tag, responseBody, new HashMap<>());
			});
		}
	}

	static FakeConnection installFakeConnection(MainActivity a) {
		FakeConnection fake = new FakeConnection(a);
		setField(a, "Connection", fake);
		return fake;
	}

	// ---- snackbar ----

	static List<String> snackbarTexts(Activity a) {
		List<String> out = new ArrayList<>();
		collectSnackbars(a.getWindow().getDecorView(), out);
		return out;
	}

	private static void collectSnackbars(View v, List<String> out) {
		if (v.getId() == com.google.android.material.R.id.snackbar_text && v instanceof TextView) {
			out.add(((TextView) v).getText().toString());
		}
		if (v instanceof ViewGroup) {
			ViewGroup g = (ViewGroup) v;
			for (int i = 0; i < g.getChildCount(); i++) collectSnackbars(g.getChildAt(i), out);
		}
	}

	// ---- not-yet-existing APIs (looked up by name, see docs/TARGETS.md) ----

	static int resId(Context c, String type, String name) {
		return c.getResources().getIdentifier(name, type, c.getPackageName());
	}

	static Method requireMethod(Class<?> owner, String name, Class<?>... params) {
		try {
			return owner.getDeclaredMethod(name, params);
		} catch (NoSuchMethodException e) {
			StringBuilder sig = new StringBuilder();
			for (Class<?> p : params) sig.append(sig.length() == 0 ? "" : ", ").append(p.getSimpleName());
			fail("missing API: " + owner.getSimpleName() + "." + name + "(" + sig + ") does not exist yet");
			return null;
		}
	}

	static Object callStatic(Method m, Object... args) {
		try {
			m.setAccessible(true);
			return m.invoke(null, args);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("calling " + m.getName() + " failed", e.getCause() != null ? e.getCause() : e);
		}
	}

	/** Test hook TEST-04 asks for: ApiConfig.setUrlForTesting(String), null resets it. */
	static void setTestApiUrl(String url) {
		callStatic(requireMethod(ApiConfig.class, "setUrlForTesting", String.class), url);
	}

	static void resetTestApiUrlQuietly() {
		try {
			Method m = ApiConfig.class.getDeclaredMethod("setUrlForTesting", String.class);
			m.setAccessible(true);
			m.invoke(null, (Object) null);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	// ---- API fixture ----

	static final String STOCK_LATEST = "8.9.10.611";

	/** A complete, well-formed update-server document with every key MainActivity reads. */
	static Map<String, Object> fullFixture() {
		Map<String, Object> d = new LinkedHashMap<>();
		d.put("Stock_Patched", items("Stock"));
		d.put("Amoled_Patched", items("Amoled"));
		d.put("Stock_Cloned_Patched", items("Stock cloned"));
		d.put("Amoled_Cloned_Patched", items("Amoled cloned"));
		d.put("Other_Patched", items("Other"));
		d.put("Patched_Changelogs", items("Changelog"));
		d.put("Stock_Patched_Latest", STOCK_LATEST);
		d.put("Amoled_Patched_Latest", "8.9.10.612");
		d.put("SCP_Latest", "8.9.10.613");
		d.put("ACP_Latest", "8.9.10.614");
		d.put("Server", "5.9");
		d.put("Update", "https://example.org/update.apk");
		d.put("App_Changelogs", "Fixes");
		d.put("Supporters", "Everyone");
		d.put("Telegram", "https://example.org/telegram");
		d.put("Reddit", "https://example.org/reddit");
		d.put("Donate", "https://example.org/donate");
		d.put("Discord", "https://example.org/discord");
		d.put("Source", "https://example.org/source");
		d.put("Website", "https://example.org/");
		d.put("FAQ", "https://example.org/faq");
		return d;
	}

	private static List<Map<String, Object>> items(String prefix) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (int i = 1; i <= 2; i++) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("Title", prefix + " 8.9.10." + i);
			m.put("Description", prefix + " item " + i);
			m.put("Link_1", "https://example.org/" + i + "/a");
			m.put("Link_2", "https://example.org/" + i + "/b");
			list.add(m);
		}
		return list;
	}

	static String json(Map<String, Object> m) {
		return new Gson().toJson(m);
	}

	/** Delivers the full fixture and waits until _Backbone has filled the LATEST label. */
	static void loadFixture(MainActivity a) {
		deliverResponse(a, json(fullFixture()));
		assertTrue("harness: fixture never reached the LATEST label",
				waitUntil(() -> STOCK_LATEST.contentEquals(((TextView) a.findViewById(R.id.sub_1)).getText()), 5000));
	}

	// ---- repository files (for CI / androidTest targets) ----

	static File repoRoot() {
		File dir = new File("").getAbsoluteFile();
		while (dir != null && !new File(dir, ".github").isDirectory()) dir = dir.getParentFile();
		assertNotNull("repository root (with .github/) not found above " + new File("").getAbsolutePath(), dir);
		return dir;
	}

	static String readRepoFile(String relative) {
		File f = new File(repoRoot(), relative);
		assertTrue("missing file " + relative, f.isFile());
		try {
			return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			throw new AssertionError(e);
		}
	}
}
