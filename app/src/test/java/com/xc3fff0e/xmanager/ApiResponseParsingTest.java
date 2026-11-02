package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

/**
 * Characterisation tests for the JSON handling in MainActivity's request listener and _Backbone():
 * the response is parsed into HashMap&lt;String,Object&gt;, each list key is re-serialised and parsed
 * again into ArrayList&lt;HashMap&gt;, and "Server" is compared to the local version as a double.
 * These use exactly the same Gson calls as MainActivity so behaviour changes are caught.
 */
public class ApiResponseParsingTest {

	private static final String SAMPLE = "{"
			+ "\"Server\":\"5.9\",\"Update\":\"https://example.org/xManager.apk\","
			+ "\"App_Changelogs\":\"- fix\\n- more\",\"Supporters\":\"a, b\","
			+ "\"Stock_Patched_Latest\":\"1.2.3\",\"Amoled_Patched_Latest\":\"1.2.3\","
			+ "\"SCP_Latest\":\"1.2.3\",\"ACP_Latest\":\"1.2.3\","
			+ "\"Stock_Patched\":[{\"Title\":\"v1\",\"Link_1\":\"https://a/1\",\"Link_2\":\"https://b/1\"},"
			+ "{\"Title\":\"v2\",\"Link_1\":\"https://a/2\",\"Link_2\":\"https://b/2\"}],"
			+ "\"Patched_Changelogs\":[{\"Title\":\"v2\",\"Changelogs\":\"x\"}],"
			+ "\"Count\":3}";

	private static HashMap<String, Object> parseRoot(String json) {
		return new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>(){}.getType());
	}

	/** Same two-step conversion MainActivity._Backbone() performs. */
	private static ArrayList<HashMap<String, Object>> list(HashMap<String, Object> root, String key) {
		String s = new Gson().toJson(root.get(key), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		return new Gson().fromJson(s, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
	}

	@Test
	public void parsesTopLevelFields() {
		HashMap<String, Object> d = parseRoot(SAMPLE);
		assertEquals("5.9", d.get("Server").toString());
		assertEquals("https://example.org/xManager.apk", d.get("Update").toString());
		assertEquals("- fix\n- more", d.get("App_Changelogs").toString());
	}

	@Test
	public void listsSurviveReserialisation_andAdapterReversesOrder() {
		ArrayList<HashMap<String, Object>> stock = list(parseRoot(SAMPLE), "Stock_Patched");
		assertEquals(2, stock.size());
		// Adapters show index (size-1-position): position 0 is the LAST array element ("LATEST").
		assertEquals("v2", stock.get(stock.size() - 1 - 0).get("Title"));
		assertEquals("https://b/1", stock.get(stock.size() - 1 - 1).get("Link_2"));
	}

	@Test
	public void missingListKey_becomesNullList() {
		// Amoled_Patched is absent: toJson(null) -> "null" -> fromJson -> null; the adapter's
		// getCount() would then NPE. MainActivity catches this and shows the maintenance screen.
		assertNull(list(parseRoot(SAMPLE), "Amoled_Patched"));
	}

	@Test(expected = NullPointerException.class)
	public void missingScalarKey_throwsNpe_likeBackbone() {
		parseRoot(SAMPLE).get("Telegram").toString();
	}

	@Test
	public void numbersBecomeDoubles() {
		// Gson maps untyped numbers to Double; any int-style use (e.g. sortListMap numeric) sees "3.0".
		assertEquals("3.0", parseRoot(SAMPLE).get("Count").toString());
	}

	@Test
	public void numericServerVersion_alsoParses() {
		HashMap<String, Object> d = parseRoot("{\"Server\":5.9}");
		assertEquals(5.9, Double.parseDouble(d.get("Server").toString()), 0.0);
	}

	@Test(expected = com.google.gson.JsonSyntaxException.class)
	public void htmlErrorPage_isRejected() {
		parseRoot("<html><body>502 Bad Gateway</body></html>");
	}

	@Test
	public void emptyBody_parsesToNull() {
		assertNull(parseRoot(""));
	}

	@Test
	public void versionCompare_simpleCase() {
		assertTrue(Double.parseDouble("6.0") > Double.parseDouble("5.9"));
	}

	/**
	 * BUG (TEST_REPORT #2): versions are compared as doubles, so 5.10 is treated as OLDER than 5.9
	 * and the app would show the "maintenance / unauthorised" screen instead of an update prompt.
	 */
	@Test
	public void versionCompare_multiDigitMinor() {
		double current = Double.parseDouble("5.9");
		double latest = Double.parseDouble("5.10");
		assertTrue("5.10 should be newer than 5.9", latest > current);
	}

	/** BUG (TEST_REPORT #2): three-part versions can't be parsed at all. */
	@Test
	public void versionCompare_threePartVersion() {
		try {
			Double.parseDouble("5.9.1");
		} catch (NumberFormatException e) {
			fail("three-part versionName \"5.9.1\" cannot be parsed by Double.parseDouble");
		}
	}
}
