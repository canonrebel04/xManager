package com.xc3fff0e.xmanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * CODE-16: SharedPreferences file names and keys in one place, so a typo can't silently
 * create a second prefs file or ignore a saved value.
 */
public final class Prefs {

	private Prefs() {}

	/** File + key for the selected theme index ("0"…"20"). */
	public static final String THEME = "THEME";

	/** File for the selected language; the key inside is {@link LanguageTags#KEY}. */
	public static final String LANGUAGE = "LANGUAGE";

	public static final String DESC_X = "DESC_X";
	public static final String LIST_REFRESH = "LIST_REFRESH";
	public static final String SWITCH_VERSION = "SWITCH_VERSION";
	public static final String REFETCH = "REFETCH";
	public static final String INITIALIZATION = "INITIALIZATION";
	public static final String HIDE_PATCHED = "HIDE_PATCHED";
	public static final String DISABLE_NOTIFICATION = "DISABLE_NOTIFICATION";

	/** Keys inside {@link #HIDE_PATCHED}. */
	public static final String KEY_SWITCH_A = "SWITCH_A";
	public static final String KEY_SWITCH_B = "SWITCH_B";

	/** Key inside {@link #SWITCH_VERSION} and {@link #LIST_REFRESH}. */
	public static final String KEY_SWITCH = "SWITCH";

	/** Key inside {@link #DISABLE_NOTIFICATION}. */
	public static final String KEY_DISABLE = "DISABLE";

	/** Key inside {@link #INITIALIZATION}. */
	public static final String KEY_INITIALIZATION = "INITIALIZATION";

	/** Key inside {@link #REFETCH}. */
	public static final String KEY_REFETCH = "REFETCH";

	/** Key inside {@link #DESC_X}. */
	public static final String KEY_UPDATE = "UPDATE";

	private static final String[] ALL_FILES = {
		THEME, LANGUAGE, DESC_X, LIST_REFRESH, SWITCH_VERSION, REFETCH,
		INITIALIZATION, HIDE_PATCHED, DISABLE_NOTIFICATION,
	};

	/**
	 * Opens every prefs file and forces the on-disk load (plus the language-tag migration)
	 * on a background thread so Splash/Main don't hit {@code awaitLoadedLocked} on the
	 * main thread during the first frame.
	 */
	public static void preloadAsync(Context context) {
		final Context app = context.getApplicationContext();
		Thread loader = new Thread(() -> {
			for (String file : ALL_FILES) {
				try {
					SharedPreferences prefs = app.getSharedPreferences(file, Context.MODE_PRIVATE);
					// getAll() waits for the XML load without touching a specific key.
					prefs.getAll();
				} catch (RuntimeException ignored) {
					// Corrupt prefs: the activity path will surface it.
				}
			}
			try {
				LanguageTags.migrate(app.getSharedPreferences(LANGUAGE, Context.MODE_PRIVATE));
			} catch (RuntimeException ignored) {
			}
		}, "xManager-prefs-preload");
		loader.setDaemon(true);
		loader.start();
	}
}
