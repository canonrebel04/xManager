package com.xc3fff0e.xmanager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import android.view.View;
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
	private static final long TIMEOUT = 15_000;
	private UiDevice device;

	@Before
	public void setUp() {
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
	}

	private static int visibility(ActivityScenario<MainActivity> s, int id) {
		AtomicInteger v = new AtomicInteger(-1);
		s.onActivity(a -> v.set(a.findViewById(id).getVisibility()));
		return v.get();
	}

	private static void sleep(long ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
	}

	@Test
	public void coldLaunch_fromLauncher_reachesMainScreen() {
		Context ctx = ApplicationProvider.getApplicationContext();
		Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(PKG);
		assertNotNull("no launcher intent", launch);
		launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(launch);
		assertTrue("splash not shown", device.wait(Until.hasObject(By.pkg(PKG).depth(0)), TIMEOUT));
		// Splash hands over to MainActivity after ~1 s; the main header has id box_header.
		assertTrue("MainActivity not reached",
				device.wait(Until.hasObject(By.res(PKG, "box_settings")), TIMEOUT));
		sleep(3000); // give the (failing) API call and timers time to run
		assertEquals("app left foreground (crash?)", PKG, device.getCurrentPackageName());
	}

	@Test
	public void visitAllScreens() {
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			sleep(1500);
			onView(withId(R.id.box_settings)).check(matches(isDisplayed()));

			// Settings
			onView(withId(R.id.box_switch)).perform(click());
			sleep(800);
			assertEquals(View.VISIBLE, visibility(s, R.id.main_scroll_settings));
			onView(withId(R.id.box_settings_icon_close)).perform(click());
			sleep(800);
			assertEquals(View.VISIBLE, visibility(s, R.id.main_refresh_layout));

			// Experiments
			onView(withId(R.id.box_experiment)).perform(click());
			sleep(800);
			assertEquals(View.VISIBLE, visibility(s, R.id.main_box_16));
			pressBack();
			sleep(800);
			assertEquals(View.VISIBLE, visibility(s, R.id.main_refresh_layout));

			// About (scrolled into view by performClick on the UI thread; it sits low on the page)
			s.onActivity(a -> a.findViewById(R.id.box_about).performClick());
			sleep(800);
			assertEquals(View.VISIBLE, visibility(s, R.id.main_scroll_about));
			pressBack();
			sleep(800);
			assertEquals(View.GONE, visibility(s, R.id.main_scroll_about));

			// Settings dialog (opens app-info shortcuts for the managed apps)
			s.onActivity(a -> a.findViewById(R.id.box_settings).performClick());
			sleep(800);
			pressBack();
			sleep(500);
		}
	}

	@Test
	public void rotate_doesNotCrash() throws Exception {
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			sleep(1000);
			device.setOrientationLeft();
			sleep(1500);
			device.setOrientationNatural();
			sleep(1500);
			onView(withId(R.id.box_settings)).check(matches(isDisplayed()));
		} finally {
			device.unfreezeRotation();
		}
	}
}
