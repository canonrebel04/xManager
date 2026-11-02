package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.view.View;
import androidx.core.view.ViewCompat;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** REL-12, REL-23, REL-24, A11Y-06, PERF-12. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SmallFixesBatch6Test {

	private static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	private static LifecycleTimer timerOf(Object activity) throws Exception {
		Field f = activity.getClass().getDeclaredField("_timer");
		f.setAccessible(true);
		return (LifecycleTimer) f.get(activity);
	}

	private static Intent packageIntent(String action, String pkg) {
		return new Intent(action, Uri.fromParts("package", pkg, null));
	}

	@Test
	public void REL23_onlyWatchedPackagesTriggerRefresh() {
		assertTrue(MainActivity.isWatchedPackageIntent(packageIntent(Intent.ACTION_PACKAGE_ADDED, "com.spotify.music")));
		assertTrue(MainActivity.isWatchedPackageIntent(packageIntent(Intent.ACTION_PACKAGE_REPLACED, "com.spotify.musix")));
		assertFalse(MainActivity.isWatchedPackageIntent(packageIntent(Intent.ACTION_PACKAGE_ADDED, "com.example.other")));
		assertFalse(MainActivity.isWatchedPackageIntent(packageIntent(Intent.ACTION_PACKAGE_ADDED, "com.spotify.music.extra")));
		assertFalse(MainActivity.isWatchedPackageIntent(new Intent(Intent.ACTION_PACKAGE_ADDED)));
		assertFalse(MainActivity.isWatchedPackageIntent(null));
	}

	private static Object field(Object o, String name) throws Exception {
		Field f = o.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(o);
	}

	@Test
	public void REL24_burstOfBroadcasts_coalescesIntoOneDelayedRefresh() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		android.os.Handler handler = (android.os.Handler) field(a, "packageRefreshHandler");
		Runnable refresh = (Runnable) field(a, "packageRefreshRunnable");
		android.content.Context app = a.getApplicationContext();

		// An unrelated package never schedules a refresh (REL-23).
		app.sendBroadcast(packageIntent(Intent.ACTION_PACKAGE_ADDED, "com.example.other"));
		shadowOf(Looper.getMainLooper()).idleFor(10, TimeUnit.MILLISECONDS);
		assertFalse(handler.hasCallbacks(refresh));

		// ADDED + REPLACED + CHANGED from one install: one pending refresh, not run yet.
		for (String action : new String[] {Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED, Intent.ACTION_PACKAGE_CHANGED}) {
			app.sendBroadcast(packageIntent(action, "com.spotify.music"));
		}
		shadowOf(Looper.getMainLooper()).idleFor(100, TimeUnit.MILLISECONDS);
		assertTrue("a debounced refresh should be pending", handler.hasCallbacks(refresh));

		shadowOf(Looper.getMainLooper()).idleFor(MainActivity.PACKAGE_REFRESH_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
		assertFalse("the refresh should have run once the burst settled", handler.hasCallbacks(refresh));

		// Stopping the activity drops a pending refresh.
		app.sendBroadcast(packageIntent(Intent.ACTION_PACKAGE_ADDED, "com.spotify.musix"));
		shadowOf(Looper.getMainLooper()).idleFor(10, TimeUnit.MILLISECONDS);
		assertTrue(handler.hasCallbacks(refresh));
		c.pause().stop();
		assertFalse(handler.hasCallbacks(refresh));
		c.destroy();
	}

	@Test
	public void REL12_mainActivity_cancelsTimerOnDestroy() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		LifecycleTimer timer = timerOf(c.get());
		assertFalse(timer.isCancelled());
		c.pause().stop().destroy();
		assertTrue(timer.isCancelled());
	}

	@Test
	public void REL12_splash_destroyedEarly_doesNotStartMain() throws Exception {
		ActivityController<SplashActivity> c = Robolectric.buildActivity(SplashActivity.class).setup();
		SplashActivity a = c.get();
		c.pause().stop().destroy();
		assertTrue(timerOf(a).isCancelled());
		long deadline = System.currentTimeMillis() + 1800;
		Intent next = null;
		while (next == null && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
			idle();
			next = shadowOf(a).getNextStartedActivity();
		}
		assertNull("a destroyed splash must not start MainActivity", next);
	}

	@Test
	public void A11Y06_sectionTitlesAreHeadings() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		int[] ids = {R.id.title_header, R.id.title_about, R.id.title_1, R.id.title_2, R.id.title_3, R.id.title_6, R.id.title_sub};
		for (int id : ids) {
			View v = a.findViewById(id);
			assertNotNull(v);
			assertTrue(a.getResources().getResourceEntryName(id) + " should be a heading",
					ViewCompat.isAccessibilityHeading(v));
		}
		c.pause().stop().destroy();
	}

	@Test
	public void PERF12_strictModeIsOnInDebugOnly() {
		assertEquals(BuildConfig.DEBUG, xManager.enableStrictModeInDebug());
	}
}
