package com.xc3fff0e.xmanager;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.accessibility.AccessibilityChecks;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Runs the Accessibility Test Framework (ATF) over the whole view hierarchy on every Espresso
 * action. Any ERROR-level finding (missing label, touch target < 48dp, low contrast, ...) fails
 * the test with a list of offending views.
 *
 * <p>CI runs this class as its own blocking step (A11Y-11). Known findings that can't be fixed
 * yet go in {@link #KNOWN_ISSUES}; every entry names the check, the view, the reason and the
 * backlog ID, and is deleted in the change that fixes it, so the list only shrinks.
 */
@RunWith(AndroidJUnit4.class)
public class AccessibilityChecksTest {

	/**
	 * Known ATF findings that are allowed for now. Empty: the main and settings screens have no
	 * ERROR-level findings on the CI AVDs (API 30 and 34) since the TouchTargetSize fixes in
	 * PR #2. Add an entry only with an open backlog item, e.g.
	 * {@code new Suppression(TouchTargetSizeCheck.class, "some_view", "why it can't be fixed yet", "A11Y-02")}.
	 */
	private static final List<Suppression> KNOWN_ISSUES = Collections.unmodifiableList(Arrays.<Suppression>asList());

	@BeforeClass
	public static void enable() {
		AccessibilityChecks.enable()
				.setRunChecksFromRootView(true)
				.setSuppressingResultMatcher(new KnownIssueMatcher(KNOWN_ISSUES));
	}

	/** One allowed finding: an ATF check on one view id, with the reason and its backlog item. */
	static final class Suppression {
		final Class<? extends AccessibilityCheck> check;
		final String viewIdName;
		final String reason;
		final String backlogId;

		Suppression(Class<? extends AccessibilityCheck> check, String viewIdName, String reason, String backlogId) {
			if (reason == null || reason.trim().isEmpty() || backlogId == null || backlogId.trim().isEmpty()) {
				throw new IllegalArgumentException("every suppression needs a reason and a backlog ID");
			}
			this.check = check;
			this.viewIdName = viewIdName;
			this.reason = reason;
			this.backlogId = backlogId;
		}

		boolean matches(AccessibilityViewCheckResult result) {
			if (result.getSourceCheckClass() != check) return false;
			View v = result.getView();
			if (v == null || v.getId() == View.NO_ID) return false;
			try {
				return viewIdName.equals(v.getResources().getResourceEntryName(v.getId()));
			} catch (android.content.res.Resources.NotFoundException e) {
				return false;
			}
		}
	}

	private static final class KnownIssueMatcher extends TypeSafeMatcher<AccessibilityViewCheckResult> {
		private final List<Suppression> entries;

		KnownIssueMatcher(List<Suppression> entries) {
			this.entries = entries;
		}

		@Override
		protected boolean matchesSafely(AccessibilityViewCheckResult result) {
			for (Suppression s : entries) {
				if (s.matches(result)) return true;
			}
			return false;
		}

		@Override
		public void describeTo(Description d) {
			d.appendText("a known accessibility issue (" + entries.size() + " suppressed)");
		}
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
