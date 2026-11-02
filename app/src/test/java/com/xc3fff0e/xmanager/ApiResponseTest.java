package com.xc3fff0e.xmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ApiResponseTest {

	private static Map<String, Object> valid() {
		Map<String, Object> m = new HashMap<>();
		for (String k : ApiResponse.REQUIRED_LISTS) m.put(k, new ArrayList<>());
		for (String k : ApiResponse.REQUIRED_VALUES) m.put(k, "1.0");
		return m;
	}

	@Test
	public void completeDocument_isAccepted() {
		assertTrue(ApiResponse.hasRequiredKeys(valid()));
	}

	@Test
	public void numericVersion_isAccepted() {
		Map<String, Object> m = valid();
		m.put("Server", 5.9d);
		assertTrue(ApiResponse.hasRequiredKeys(m));
	}

	@Test
	public void nullEmptyOrMissingKeys_areRejected() {
		assertFalse(ApiResponse.hasRequiredKeys(null));
		assertFalse(ApiResponse.hasRequiredKeys(new HashMap<>()));
		for (String k : ApiResponse.REQUIRED_VALUES) {
			Map<String, Object> m = valid();
			m.remove(k);
			assertFalse(k, ApiResponse.hasRequiredKeys(m));
		}
	}

	@Test
	public void wrongTypes_areRejected() {
		Map<String, Object> m = valid();
		m.put("Stock_Patched", "not a list");
		assertFalse(ApiResponse.hasRequiredKeys(m));
		m = valid();
		m.put("Server", new ArrayList<>());
		assertFalse(ApiResponse.hasRequiredKeys(m));
		m = valid();
		m.put("SCP_Latest", null);
		assertFalse(ApiResponse.hasRequiredKeys(m));
	}

	@Test
	public void optString_fallsBackForMissingNullOrWrongType() {
		Map<String, Object> m = new HashMap<>();
		m.put("A", "x");
		m.put("B", null);
		m.put("C", new ArrayList<>());
		m.put("D", 2.5d);
		assertEquals("x", ApiResponse.optString(m, "A", "-"));
		assertEquals("-", ApiResponse.optString(m, "B", "-"));
		assertEquals("-", ApiResponse.optString(m, "C", "-"));
		assertEquals("2.5", ApiResponse.optString(m, "D", "-"));
		assertEquals("-", ApiResponse.optString(m, "missing", "-"));
		assertEquals("-", ApiResponse.optString(null, "A", "-"));
	}
}
