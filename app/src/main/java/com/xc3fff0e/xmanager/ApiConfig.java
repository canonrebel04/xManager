package com.xc3fff0e.xmanager;

import okhttp3.HttpUrl;

/**
 * Where the update-server endpoint comes from. The URL is a build-time setting
 * (Gradle property {@code xmanagerApiUrl} -> {@code BuildConfig.API_URL}) and is empty
 * unless the person building the app supplies one.
 */
public final class ApiConfig {

	private ApiConfig() {
	}

	/** The configured endpoint, trimmed; empty when none was set at build time. */
	public static String url() {
		return BuildConfig.API_URL == null ? "" : BuildConfig.API_URL.trim();
	}

	/** True for an absolute http(s) URL with a host; false for empty or malformed values. */
	public static boolean isUsable(String url) {
		if (url == null || url.trim().isEmpty()) {
			return false;
		}
		HttpUrl parsed = HttpUrl.parse(url.trim());
		return parsed != null && !parsed.host().isEmpty();
	}
}
