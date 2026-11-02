package com.xc3fff0e.xmanager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.accessibility.AccessibilityChecks;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Runs the Accessibility Test Framework (ATF) over the whole view hierarchy on every Espresso
 * action. Any ERROR-level finding (missing label, touch target < 48dp, low contrast, ...) fails
 * the test with a list of offending views.
 */
@RunWith(AndroidJUnit4.class)
public class AccessibilityChecksTest {

	@BeforeClass
	public static void enable() {
		AccessibilityChecks.enable().setRunChecksFromRootView(true);
	}

	@Before
	public void skipFirstRunRelaunch() {
		FirstRun.markDone();
	}

	@Test
	public void mainScreen_passesAtf() {
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			onView(withId(R.id.box_update)).check((v, e) -> { });
			onView(withId(R.id.box_switch)).perform(click()); // runs ATF on main screen
		}
	}

	@Test
	public void settingsScreen_passesAtf() {
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			s.onActivity(a -> a.findViewById(R.id.box_switch).performClick());
			onView(withId(R.id.box_settings_icon_close)).perform(click()); // runs ATF on settings
		}
	}
}
