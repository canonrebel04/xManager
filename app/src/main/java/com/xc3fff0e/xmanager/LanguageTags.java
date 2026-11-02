package com.xc3fff0e.xmanager;

import android.content.SharedPreferences;

/**
 * Stable language tags for the language picker (REL-10). The preference used to hold the
 * spinner index, so reordering or adding a language changed the saved choice. It now holds a
 * tag; legacy index values are migrated on launch.
 */
final class LanguageTags {

	static final String KEY = "LANGUAGE";

	/** One tag per entry of the language spinner, in spinner order. */
	static final String[] TAGS = {
		"en", "bn", "zh-Hans", "fr", "el", "id", "it", "pl", "pt-BR", "pt-PT",
		"ru", "es", "uk", "tr", "ar", "sl", "ro", "ko", "nl", "vi",
		"de", "fa", "he", "sk", "sv", "fi", "cs", "zh-Hant", "bg", "sr-Cyrl",
		"sr-Latn", "ca", "lv", "de-x-formal", "si", "hu", "ja", "ne", "ta", "th",
		"mr", "ml", "hr", "hi", "mn", "hy",
	};

	private LanguageTags() {
	}

	/** The tag for a spinner position. */
	static String tagForIndex(int index) {
		return TAGS[index];
	}

	/**
	 * The spinner position for a stored value, accepting a tag or a legacy index; -1 when the
	 * value is empty or unknown.
	 */
	static int indexOf(String stored) {
		if (stored == null || stored.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < TAGS.length; i++) {
			if (TAGS[i].equals(stored)) {
				return i;
			}
		}
		if (stored.matches("\\d{1,2}")) {
			int legacy = Integer.parseInt(stored);
			if (legacy < TAGS.length) {
				return legacy;
			}
		}
		return -1;
	}

	/** True when the stored value (tag or legacy index) selects spinner position {@code index}. */
	static boolean matches(String stored, int index) {
		return indexOf(stored) == index;
	}

	/** Rewrites a legacy index value to its tag. Returns true if the preference changed. */
	static boolean migrate(SharedPreferences prefs) {
		String stored = prefs.getString(KEY, "");
		if (stored == null || !stored.matches("\\d{1,2}")) {
			return false;
		}
		int index = indexOf(stored);
		if (index < 0) {
			return false;
		}
		prefs.edit().putString(KEY, TAGS[index]).apply();
		return true;
	}
}
