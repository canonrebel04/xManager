package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** CODE-16, SEC-12, TEST-02 contrast, TEST-03 leftover English, TEST-06 recreate. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SmallFixesBatch8Test {

	/**
	 * Light Orange (17) and Light Cyan (20) headers fail WCAG AA today against the near-white
	 * title text. Tracked under A11Y-03; the test still measures them so a future colour fix
	 * can drop them from this set.
	 */
	private static final Set<Integer> KNOWN_HEADER_CONTRAST_FAILS =
			new HashSet<>(Arrays.asList(17, 20));

	private static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	private static Object field(Object o, String name) throws Exception {
		Field f = o.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(o);
	}

	/** WCAG 2.x relative luminance of an sRGB colour (alpha ignored). */
	static double relativeLuminance(int color) {
		double r = channel(Color.red(color));
		double g = channel(Color.green(color));
		double b = channel(Color.blue(color));
		return 0.2126 * r + 0.7152 * g + 0.0722 * b;
	}

	private static double channel(int c8) {
		double c = c8 / 255.0;
		return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	/** WCAG contrast ratio of two opaque colours (higher is better; AA body text needs 4.5). */
	static double contrastRatio(int a, int b) {
		double l1 = relativeLuminance(a);
		double l2 = relativeLuminance(b);
		double lighter = Math.max(l1, l2);
		double darker = Math.min(l1, l2);
		return (lighter + 0.05) / (darker + 0.05);
	}

	private static int solidBackground(View view) {
		assertNotNull(view.getBackground());
		assertTrue("expected a solid ColorDrawable, got " + view.getBackground().getClass(),
				view.getBackground() instanceof ColorDrawable);
		return ((ColorDrawable) view.getBackground()).getColor();
	}

	@Test
	public void CODE16_prefsConstantsMatchHistoricalNames() {
		assertEquals("THEME", Prefs.THEME);
		assertEquals("LANGUAGE", Prefs.LANGUAGE);
		assertEquals(LanguageTags.KEY, Prefs.LANGUAGE);
		assertEquals("HIDE_PATCHED", Prefs.HIDE_PATCHED);
		assertEquals("SWITCH_A", Prefs.KEY_SWITCH_A);
		assertEquals("SWITCH", Prefs.KEY_SWITCH);
	}

	@Test
	public void CODE16_themeAndLanguageUsePrefsFiles() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences theme = (SharedPreferences) field(a, "THEME");
		SharedPreferences language = (SharedPreferences) field(a, "LANGUAGE");
		theme.edit().putString(Prefs.THEME, "3").commit();
		language.edit().putString(LanguageTags.KEY, LanguageTags.tagForIndex(2)).commit();
		assertEquals("3", a.getSharedPreferences(Prefs.THEME, Context.MODE_PRIVATE).getString(Prefs.THEME, ""));
		assertEquals(LanguageTags.tagForIndex(2),
				a.getSharedPreferences(Prefs.LANGUAGE, Context.MODE_PRIVATE).getString(LanguageTags.KEY, ""));
	}

	@Test
	@Config(sdk = 33)
	public void SEC12_copiedChangelogIsMarkedSensitive() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		a._copyToClipboard("secret changelog line");
		ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = cm.getPrimaryClip();
		assertNotNull(clip);
		assertEquals("secret changelog line", clip.getItemAt(0).coerceToText(a).toString());
		assertTrue(clip.getDescription().getExtras().getBoolean(ClipDescription.EXTRA_IS_SENSITIVE));
	}

	@Test
	public void SEC12_copyWorksOnDefaultSdk() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		a._copyToClipboard("plain");
		ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
		assertEquals("plain", cm.getPrimaryClip().getItemAt(0).coerceToText(a).toString());
	}

	/** TEST-02: every theme applies, and header text meets WCAG AA against the header background. */
	@Test
	public void TEST02_everyThemeHeaderMeetsContrastOrIsKnown() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences theme = (SharedPreferences) field(a, "THEME");
		Spinner spinner = (Spinner) field(a, "select_theme");
		TextView titleHeader = (TextView) field(a, "title_header");
		View boxHeader = (View) field(a, "box_header");
		assertEquals(21, spinner.getAdapter().getCount());

		for (int i = 0; i < 21; i++) {
			theme.edit().putString(Prefs.THEME, String.valueOf(i)).commit();
			a._Theme_UI();
			idle();
			assertEquals("theme " + i, i, spinner.getSelectedItemPosition());
			int fg = titleHeader.getCurrentTextColor();
			int bg = solidBackground(boxHeader);
			double ratio = contrastRatio(fg, bg);
			if (KNOWN_HEADER_CONTRAST_FAILS.contains(i)) {
				assertTrue("theme " + i + " should still be below AA until A11Y-03 fixes it; was " + ratio,
						ratio < 4.5);
			} else {
				assertTrue("theme " + i + " header contrast " + ratio + " (fg="
								+ Integer.toHexString(fg) + " bg=" + Integer.toHexString(bg) + ")",
						ratio >= 4.5);
			}
		}
	}

	/**
	 * TEST-03: every non-English language leaves key labels different from the English defaults.
	 * (The app name stays "xManager" in every language, so that string is not checked.)
	 */
	@Test
	public void TEST03_nonEnglishLanguagesDoNotKeepEnglishLabels() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences language = (SharedPreferences) field(a, "LANGUAGE");
		Spinner spinner = (Spinner) field(a, "select_language");
		TextView settings = (TextView) field(a, "settings_title");
		TextView about = (TextView) field(a, "about_title");
		TextView installed = (TextView) field(a, "sub_text_installed");
		TextView latest = (TextView) field(a, "sub_text_1");
		TextView langLabel = (TextView) field(a, "language");

		language.edit().putString(LanguageTags.KEY, LanguageTags.tagForIndex(0)).commit();
		a._Language_UI();
		idle();
		String enSettings = settings.getText().toString();
		String enAbout = about.getText().toString();
		String enInstalled = installed.getText().toString();
		String enLatest = latest.getText().toString();
		String enLang = langLabel.getText().toString();

		for (int i = 1; i < LanguageTags.TAGS.length; i++) {
			language.edit().putString(LanguageTags.KEY, LanguageTags.tagForIndex(i)).commit();
			a._Language_UI();
			idle();
			assertEquals("language " + LanguageTags.tagForIndex(i), i, spinner.getSelectedItemPosition());
			boolean changed = !enSettings.equals(settings.getText().toString())
					|| !enAbout.equals(about.getText().toString())
					|| !enInstalled.equals(installed.getText().toString())
					|| !enLatest.equals(latest.getText().toString())
					|| !enLang.equals(langLabel.getText().toString());
			assertTrue("language " + LanguageTags.tagForIndex(i)
							+ " still shows English for settings/about/installed/latest/language",
					changed);
		}
	}

	/** TEST-06: recreate keeps the selected theme and language. */
	@Test
	public void TEST06_recreateRestoresThemeAndLanguage() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences theme = (SharedPreferences) field(a, "THEME");
		SharedPreferences language = (SharedPreferences) field(a, "LANGUAGE");
		theme.edit().putString(Prefs.THEME, "5").commit();
		language.edit().putString(LanguageTags.KEY, LanguageTags.tagForIndex(3)).commit();
		a._Theme_UI();
		a._Language_UI();
		idle();
		assertEquals(5, ((Spinner) field(a, "select_theme")).getSelectedItemPosition());
		assertEquals(3, ((Spinner) field(a, "select_language")).getSelectedItemPosition());

		c = c.recreate();
		idle();
		MainActivity b = c.get();
		assertEquals("5", ((SharedPreferences) field(b, "THEME")).getString(Prefs.THEME, ""));
		assertEquals(LanguageTags.tagForIndex(3),
				((SharedPreferences) field(b, "LANGUAGE")).getString(LanguageTags.KEY, ""));
		assertEquals(5, ((Spinner) field(b, "select_theme")).getSelectedItemPosition());
		assertEquals(3, ((Spinner) field(b, "select_language")).getSelectedItemPosition());
	}

	@Test
	public void contrastHelper_knownRatios() {
		assertEquals(21.0, contrastRatio(Color.WHITE, Color.BLACK), 0.01);
		assertTrue(contrastRatio(0xFFF2F3F4, 0xFF1B5E20) >= 4.5);
		assertTrue(contrastRatio(0xFFF2F3F4, 0xFFFF8C00) < 4.5);
	}
}
