package com.xc3fff0e.xmanager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Emulator smoke test: cold-launches the app from the launcher intent, then visits every
 * top-level screen (home, Experiments, Settings, About) and backs out, checking nothing crashes.
 */
@RunWith(AndroidJUnit4.class)
public class SmokeTest {

	private static final String PKG = "com.xc3fff0e.xmanager";
	// 30 s: the 15 s value timed out on a software-rendered emulator (PR2_RESULTS.md).
	private static final long TIMEOUT = 30_000;
	private static final long POLL_MS = 250;
	private UiDevice device;

	private interface Condition {
		boolean holds();
	}

	@Before
	public void setUp() {
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
	}

	private static int visibility(ActivityScenario<MainActivity> s, int id) {
		AtomicInteger v = new AtomicInteger(-1);
		s.onActivity(a -> v.set(a.findViewById(id).getVisibility()));
		return v.get();
	}

	/**
	 * Waits for app state instead of a fixed pause (CI-18): lets the main thread go idle, checks
	 * the condition, and otherwise blocks until the next window update (at most POLL_MS).
	 */
	private boolean waitFor(Condition condition) {
		long deadline = android.os.SystemClock.uptimeMillis() + TIMEOUT;
		while (true) {
			InstrumentationRegistry.getInstrumentation().waitForIdleSync();
			if (condition.holds()) return true;
			if (android.os.SystemClock.uptimeMillis() >= deadline) return false;
			device.waitForWindowUpdate(PKG, POLL_MS);
		}
	}

	private void awaitVisibility(ActivityScenario<MainActivity> s, int id, int expected) {
		assertTrue("view " + InstrumentationRegistry.getInstrumentation().getTargetContext()
						.getResources().getResourceEntryName(id) + " never reached visibility " + expected,
				waitFor(() -> visibility(s, id) == expected));
	}

	@Test
	public void coldLaunch_fromLauncher_reachesMainScreen() {
		Context ctx = ApplicationProvider.getApplicationContext();
		Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(PKG);
		assertNotNull("no launcher intent", launch);
		launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(launch);
		assertTrue("splash not shown", device.wait(Until.hasObject(By.pkg(PKG).depth(0)), TIMEOUT));
		// Splash hands over to MainActivity after ~1 s. The header buttons (box_header) are the
		// first thing on screen once the main view is ready; box_settings sits below the fold
		// on small screens (e.g. the 320x640 default CI AVD), so UiAutomator can't see it there.
		assertTrue("MainActivity not reached",
				device.wait(Until.hasObject(By.res(PKG, "box_switch")), TIMEOUT));
		// Wait until the update-server call has finished: with no api_url (CI) the inline error
		// view or its snackbar shows; with one configured the LATEST label fills in.
		assertTrue("update-server call never finished",
				waitFor(() -> device.hasObject(By.res(PKG, "api_error_retry"))
						|| device.hasObject(By.res(PKG, "snackbar_text"))));
		device.waitForIdle();
		assertEquals("app left foreground (crash?)", PKG, device.getCurrentPackageName());
	}

	@Test
	public void visitAllScreens() {
		FirstRun.markDone();
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			assertTrue("main header not shown",
					device.wait(Until.hasObject(By.res(PKG, "box_switch")), TIMEOUT));
			onView(withId(R.id.box_switch)).check(matches(isDisplayed()));
			onView(withId(R.id.box_settings)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

			// Settings
			onView(withId(R.id.box_switch)).perform(click());
			awaitVisibility(s, R.id.main_scroll_settings, View.VISIBLE);
			onView(withId(R.id.box_settings_icon_close)).perform(click());
			awaitVisibility(s, R.id.main_refresh_layout, View.VISIBLE);

			// Experiments
			onView(withId(R.id.box_experiment)).perform(click());
			awaitVisibility(s, R.id.main_box_16, View.VISIBLE);
			pressBack();
			awaitVisibility(s, R.id.main_refresh_layout, View.VISIBLE);

			// About (scrolled into view by performClick on the UI thread; it sits low on the page)
			s.onActivity(a -> a.findViewById(R.id.box_about).performClick());
			awaitVisibility(s, R.id.main_scroll_about, View.VISIBLE);
			pressBack();
			awaitVisibility(s, R.id.main_scroll_about, View.GONE);

			// Settings dialog (opens app-info shortcuts for the managed apps)
			s.onActivity(a -> a.findViewById(R.id.box_settings).performClick());
			device.waitForIdle();
			pressBack();
			device.waitForIdle();
			assertEquals("app left foreground (crash?)", PKG, device.getCurrentPackageName());
		}
	}

	@Test
	public void rotate_doesNotCrash() throws Exception {
		FirstRun.markDone();
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			assertTrue("main header not shown before rotating",
					device.wait(Until.hasObject(By.res(PKG, "box_switch")), TIMEOUT));
			device.setOrientationLeft();
			device.waitForIdle();
			assertTrue("main header not shown in landscape",
					device.wait(Until.hasObject(By.res(PKG, "box_switch")), TIMEOUT));
			device.setOrientationNatural();
			assertTrue("main header not shown",
					device.wait(Until.hasObject(By.res(PKG, "box_switch")), TIMEOUT));
			onView(withId(R.id.box_switch)).check(matches(isDisplayed()));
			onView(withId(R.id.box_settings)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		} finally {
			device.unfreezeRotation();
		}
	}
}
