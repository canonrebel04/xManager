package com.xc3fff0e.xmanager;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Typefaces from the app's assets, loaded once per process. Typeface.createFromAsset reads and
 * parses the font file; MainActivity used to do that ~150 times on the main thread at startup.
 */
final class Fonts {

	static final String BASE = "fonts/base_font.ttf";
	static final String GLITCH = "fonts/base_glitch.ttf";

	private static final Map<String, Typeface> CACHE = new HashMap<>();

	private Fonts() {
	}

	/** The cached typeface for {@code path}, loading it on first use. */
	static Typeface get(AssetManager assets, String path) {
		synchronized (CACHE) {
			Typeface typeface = CACHE.get(path);
			if (typeface == null) {
				typeface = Typeface.createFromAsset(assets, path);
				CACHE.put(path, typeface);
			}
			return typeface;
		}
	}

	/** Loads the given fonts on a background thread so later {@link #get} calls hit the cache. */
	static void preloadAsync(final AssetManager assets, final String... paths) {
		Thread loader = new Thread(() -> {
			for (String path : paths) {
				try {
					get(assets, path);
				} catch (RuntimeException e) {
					// Missing or unreadable font: get() on the main thread will report it.
				}
			}
		}, "xManager-font-preload");
		loader.setDaemon(true);
		loader.start();
	}
}
