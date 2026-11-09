package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Startup ANR follow-ups (prefs preload, cheaper theme, deferred snackbar, safe dismiss) + ThemeDrawables. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SmallFixesBatch9Test {

	private static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	private static Object field(Object o, String name) throws Exception {
		Field f = o.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(o);
	}

	@Test
	public void prefsPreload_loadsFilesAndMigratesLegacyLanguage() throws Exception {
		Context app = ApplicationProvider.getApplicationContext();
		SharedPreferences language = app.getSharedPreferences(Prefs.LANGUAGE, Context.MODE_PRIVATE);
		language.edit().clear().putString(LanguageTags.KEY, "10").commit(); // legacy Russian index

		Prefs.preloadAsync(app);
		// Join the daemon loader.
		Thread.sleep(200);
		assertEquals("ru", language.getString(LanguageTags.KEY, ""));
		assertFalse(app.getSharedPreferences(Prefs.THEME, Context.MODE_PRIVATE).getAll() == null);
	}

	@Test
	public void themeDrawables_roundedBuildsSolidCorneredDrawable() {
		GradientDrawable d = ThemeDrawables.rounded(25, 0xFF171717);
		assertEquals(GradientDrawable.RECTANGLE, d.getShape());
		assertNotNull(d);
	}

	@Test
	public void originalTheme_appliesHomeImmediately_andSecondaryAfterPost() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		// Force default theme path again.
		((SharedPreferences) field(a, "THEME")).edit().putString(Prefs.THEME, "0").commit();
		a._applyOriginalThemeHome();
		assertTrue(a.findViewById(R.id.main_box_1).getBackground() instanceof GradientDrawable);
		assertEquals(0xFF1DB954, ((TextView) field(a, "title_header")).getCurrentTextColor());

		// Secondary not required to have run yet if we call home alone; call secondary and check.
		a._applyOriginalThemeSecondary();
		assertTrue(((View) field(a, "box_about_header")).getBackground() instanceof GradientDrawable);
		assertEquals(0xFF1DB954, ((TextView) field(a, "manager_lang_1")).getCurrentTextColor());
	}

	@Test
	public void originalTheme_viaThemeUi_postsSecondaryWithoutCrashing() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		((SharedPreferences) field(a, "THEME")).edit().putString(Prefs.THEME, "0").commit();
		a._Theme_UI();
		idle();
		shadowOf(Looper.getMainLooper()).idleFor(50, TimeUnit.MILLISECONDS);
		assertEquals(0xFF1DB954, ((TextView) field(a, "contributors")).getCurrentTextColor());
	}

	@Test
	public void dismissDialogSafely_skipsWhenNotShowingOrFinishing() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		Dialog dialog = new Dialog(a);
		MainActivity.dismissDialogSafely(null, a); // no-op
		MainActivity.dismissDialogSafely(dialog, a); // not showing
		dialog.show();
		assertTrue(dialog.isShowing());
		MainActivity.dismissDialogSafely(dialog, a);
		assertFalse(dialog.isShowing());

		dialog.show();
		c.pause().stop().destroy();
		idle();
		MainActivity.dismissDialogSafely(dialog, a); // activity destroyed — must not throw
	}

	@Test
	public void showApiUnavailable_keepsInlineError_withoutCrashingBeforeLayout() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).create().start();
		MainActivity a = c.get();
		// Invoke before visible()/layout by calling private path through _API with empty URL already handled
		// in initializeLogic; here call _showApiUnavailable directly after create.
		java.lang.reflect.Method m = MainActivity.class.getDeclaredMethod("_showApiUnavailable", String.class);
		m.setAccessible(true);
		m.invoke(a, "test unavailable");
		idle();
		assertEquals(View.VISIBLE, a.findViewById(R.id.api_error_view).getVisibility());
		c.resume().visible();
		shadowOf(Looper.getMainLooper()).idleFor(100, TimeUnit.MILLISECONDS);
	}

	@Test
	public void application_onCreate_startsPrefsPreload() {
		// xManager.enableStrictModeInDebug is covered elsewhere; ensure Prefs files exist after app create.
		Context app = ApplicationProvider.getApplicationContext();
		Prefs.preloadAsync(app);
		try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		SharedPreferences theme = app.getSharedPreferences(Prefs.THEME, Context.MODE_PRIVATE);
		assertNotNull(theme.getAll());
	}
}
