package com.xc3fff0e.xmanager;

import java.util.List;
import java.util.Map;

/**
 * Checks the update-server document before the app uses it (REL-04) and reads optional
 * values without throwing (REL-05).
 */
final class ApiResponse {

	/** Keys that must hold a JSON array. */
	static final String[] REQUIRED_LISTS = {
		"Stock_Patched", "Amoled_Patched", "Stock_Cloned_Patched",
		"Amoled_Cloned_Patched", "Other_Patched", "Patched_Changelogs",
	};

	/** Keys that must hold a string or number. */
	static final String[] REQUIRED_VALUES = {
		"Stock_Patched_Latest", "Amoled_Patched_Latest", "SCP_Latest", "ACP_Latest", "Server",
	};

	private ApiResponse() {
	}

	/** True when every required key is present with the expected type. */
	static boolean hasRequiredKeys(Map<String, Object> data) {
		if (data == null) {
			return false;
		}
		for (String key : REQUIRED_LISTS) {
			if (!(data.get(key) instanceof List)) {
				return false;
			}
		}
		for (String key : REQUIRED_VALUES) {
			Object v = data.get(key);
			if (!(v instanceof String || v instanceof Number)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The value for {@code key} as text, or {@code fallback} when the map is null, the key is
	 * missing, the value is null, or it isn't a string or number.
	 */
	static String optString(Map<String, Object> data, String key, String fallback) {
		if (data == null) {
			return fallback;
		}
		Object v = data.get(key);
		if (v instanceof String || v instanceof Number || v instanceof Boolean) {
			return v.toString();
		}
		return fallback;
	}
}
