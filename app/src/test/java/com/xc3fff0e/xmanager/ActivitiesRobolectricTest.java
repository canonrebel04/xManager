package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Screen-level smoke tests on the JVM. A plain Application is used because xManager.onCreate()
 * starts a static logcat thread that can only be started once per JVM (see TEST_REPORT).
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class ActivitiesRobolectricTest {

	private static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	@Test
	public void debugActivity_showsErrorExtra() {
		Intent i = new Intent(ApplicationProvider.getApplicationContext(), DebugActivity.class);
		i.putExtra("error", "java.lang.RuntimeException: boom");
		DebugActivity a = Robolectric.buildActivity(DebugActivity.class, i).setup().get();
		ScrollView root = (ScrollView) ((android.view.ViewGroup) a.findViewById(android.R.id.content)).getChildAt(0);
		String text = ((TextView) root.getChildAt(0)).getText().toString();
		assertTrue(text.contains("boom"));
	}

	@Test
	public void debugActivity_noExtra_showsFallback() {
		DebugActivity a = Robolectric.buildActivity(DebugActivity.class).setup().get();
		ScrollView root = (ScrollView) ((android.view.ViewGroup) a.findViewById(android.R.id.content)).getChildAt(0);
		assertTrue(((TextView) root.getChildAt(0)).getText().toString().contains("No error details"));
	}

	@Test
	public void splash_launchesMainAfterDelay() throws Exception {
		SplashActivity a = Robolectric.buildActivity(SplashActivity.class).setup().get();
		TextView title = a.findViewById(R.id.title_splash);
		assertFalse(title.getText().toString().isEmpty());
		// SplashActivity uses a real java.util.Timer (1000 ms) and then posts to the UI thread.
		Intent next = null;
		long deadline = System.currentTimeMillis() + 5000;
		while (next == null && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
			idle();
			next = shadowOf(a).getNextStartedActivity();
		}
		assertNotNull("Splash never started MainActivity", next);
		assertEquals(MainActivity.class.getName(), next.getComponent().getClassName());
		assertTrue(a.isFinishing());
	}

	private MainActivity launchMain() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		return c.get();
	}

	@Test
	public void main_launchesWithoutCrash_withEmptyApiUrl() {
		MainActivity a = launchMain();
		assertNotNull(a.findViewById(R.id.box_settings));
		assertFalse(a.isFinishing());
	}

	@Test
	public void main_emptyApiUrl_stopsLoaderInsteadOfSpinning() {
		MainActivity a = launchMain();
		// Let the startup timers (_API_Token runs after the gate keeper) fire.
		org.robolectric.shadows.ShadowLooper.idleMainLooper(5, java.util.concurrent.TimeUnit.SECONDS);
		idle();
		androidx.swiperefreshlayout.widget.SwipeRefreshLayout refresh = a.findViewById(R.id.main_refresh_layout);
		assertFalse("loader left spinning with no API URL", refresh.isRefreshing());
		assertFalse(a.isFinishing());
	}

	@Test
	public void main_openAndCloseSettings() {
		MainActivity a = launchMain();
		a.findViewById(R.id.box_switch).performClick();
		idle();
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_scroll_settings).getVisibility());
		assertEquals(View.GONE, a.findViewById(R.id.main_refresh_layout).getVisibility());
		a.findViewById(R.id.box_settings_icon_close).performClick();
		idle();
		assertEquals(View.GONE, a.findViewById(R.id.main_scroll_settings).getVisibility());
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_refresh_layout).getVisibility());
	}

	@Test
	public void main_openExperimentsScreen() {
		MainActivity a = launchMain();
		a.findViewById(R.id.box_experiment).performClick();
		idle();
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_scroll_settings).getVisibility());
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_box_16).getVisibility());
	}

	@Test
	public void main_openAndCloseAbout() {
		MainActivity a = launchMain();
		a.findViewById(R.id.box_about).performClick();
		idle();
		assertEquals(View.VISIBLE, a.findViewById(R.id.main_scroll_about).getVisibility());
		a.findViewById(R.id.box_icon_close).performClick();
		idle();
		assertEquals(View.GONE, a.findViewById(R.id.main_scroll_about).getVisibility());
	}

	@Test
	public void main_backFromAbout_returnsHome() {
		MainActivity a = launchMain();
		a.findViewById(R.id.box_about).performClick();
		idle();
		a.getOnBackPressedDispatcher().onBackPressed();
		idle();
		assertEquals(View.GONE, a.findViewById(R.id.main_scroll_about).getVisibility());
		assertFalse(a.isFinishing());
	}

	@Test
	public void main_themeSpinnerHas21Entries() {
		MainActivity a = launchMain();
		Spinner theme = a.findViewById(R.id.select_theme);
		assertEquals(21, theme.getAdapter().getCount());
	}

	/** BUG (TEST_REPORT #3): "German" appears twice in the language picker. */
	@Test
	public void main_languageSpinnerHasNoDuplicates() {
		MainActivity a = launchMain();
		Spinner lang = a.findViewById(R.id.select_language);
		List<String> items = new ArrayList<>();
		for (int i = 0; i < lang.getAdapter().getCount(); i++) items.add(String.valueOf(lang.getAdapter().getItem(i)));
		assertEquals("duplicate language entries: " + items, items.size(), new HashSet<>(items).size());
	}
}
