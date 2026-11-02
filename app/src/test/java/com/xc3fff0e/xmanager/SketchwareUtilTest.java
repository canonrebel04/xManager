package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SketchwareUtilTest {

	private static ArrayList<HashMap<String, Object>> rows(String key, Object... values) {
		ArrayList<HashMap<String, Object>> list = new ArrayList<>();
		for (Object v : values) {
			HashMap<String, Object> m = new HashMap<>();
			m.put(key, v);
			list.add(m);
		}
		return list;
	}

	private static ArrayList<String> column(ArrayList<HashMap<String, Object>> list, String key) {
		ArrayList<String> out = new ArrayList<>();
		for (HashMap<String, Object> m : list) out.add(String.valueOf(m.get(key)));
		return out;
	}

	@Test
	public void getRandom_staysWithinInclusiveBounds() {
		boolean sawMin = false, sawMax = false;
		for (int i = 0; i < 2000; i++) {
			int r = SketchwareUtil.getRandom(0, 4);
			assertTrue("out of range: " + r, r >= 0 && r <= 4);
			sawMin |= r == 0;
			sawMax |= r == 4;
		}
		assertTrue("min bound never produced", sawMin);
		assertTrue("max bound never produced", sawMax);
	}

	@Test
	public void getRandom_singleValueRange() {
		assertEquals(7, SketchwareUtil.getRandom(7, 7));
	}

	@Test
	public void copyFromInputStream_readsUtf8AcrossBufferBoundary() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3000; i++) sb.append((char) ('a' + (i % 26)));
		String text = sb.toString();
		assertEquals(text, SketchwareUtil.copyFromInputStream(
				new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
	}

	@Test
	public void copyFromInputStream_emptyStream() {
		assertEquals("", SketchwareUtil.copyFromInputStream(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void getAllKeysFromMap_clearsOutputAndCopiesKeys() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("Server", 5.9);
		map.put("Update", "https://example.org");
		ArrayList<String> out = new ArrayList<>();
		out.add("stale");
		SketchwareUtil.getAllKeysFromMap(map, out);
		assertEquals(2, out.size());
		assertTrue(out.contains("Server"));
		assertTrue(out.contains("Update"));
	}

	@Test
	public void getAllKeysFromMap_nullOrEmptyMapClearsOutput() {
		ArrayList<String> out = new ArrayList<>();
		out.add("stale");
		SketchwareUtil.getAllKeysFromMap(null, out);
		assertTrue(out.isEmpty());
		SketchwareUtil.getAllKeysFromMap(null, null); // must not throw
	}

	@Test
	public void sortListMap_stringAscendingAndDescending() {
		ArrayList<HashMap<String, Object>> list = rows("Title", "b", "c", "a");
		SketchwareUtil.sortListMap(list, "Title", false, true);
		assertEquals(java.util.Arrays.asList("a", "b", "c"), column(list, "Title"));
		SketchwareUtil.sortListMap(list, "Title", false, false);
		assertEquals(java.util.Arrays.asList("c", "b", "a"), column(list, "Title"));
	}

	/**
	 * BUG (TEST_REPORT #1): the numeric comparator never returns +1
	 * ({@code a < b ? -1 : a < b ? 1 : 0}), so numeric sorting is wrong.
	 */
	@Test
	public void sortListMap_numericAscending() {
		ArrayList<HashMap<String, Object>> list = rows("n", "3", "1", "2");
		SketchwareUtil.sortListMap(list, "n", true, true);
		assertEquals(java.util.Arrays.asList("1", "2", "3"), column(list, "n"));
	}

	@Test
	public void sortListMap_numericDescending() {
		ArrayList<HashMap<String, Object>> list = rows("n", "1", "3", "2");
		SketchwareUtil.sortListMap(list, "n", true, false);
		assertEquals(java.util.Arrays.asList("3", "2", "1"), column(list, "n"));
	}

	/**
	 * BUG (TEST_REPORT #1): the numeric comparator never returns +1
	 * ({@code a < b ? -1 : a < b ? 1 : 0}). Small lists happen to sort correctly (binary insertion
	 * only checks {@code < 0}), but TimSort's merge/gallop phase on lists of more than 32 entries
	 * relies on {@code > 0} and produces a wrong order (or "Comparison method violates its general
	 * contract").
	 */
	@Test
	public void sortListMap_numericAscending_largeList() {
		java.util.List<Integer> nums = new ArrayList<>();
		for (int i = 0; i < 200; i++) nums.add(i);
		java.util.Collections.shuffle(nums, new java.util.Random(42));
		ArrayList<HashMap<String, Object>> list = rows("n", nums.stream().map(String::valueOf).toArray());
		SketchwareUtil.sortListMap(list, "n", true, true);
		ArrayList<String> got = column(list, "n");
		for (int i = 0; i < 200; i++) assertEquals("index " + i, String.valueOf(i), got.get(i));
	}

	/** Gson turns JSON numbers into Doubles ("3.0"), which Integer.valueOf rejects. */
	@Test
	public void sortListMap_numeric_withGsonDoubles() {
		ArrayList<HashMap<String, Object>> list = rows("n", 3.0, 1.0, 2.0);
		try {
			SketchwareUtil.sortListMap(list, "n", true, true);
		} catch (NumberFormatException e) {
			fail("numeric sort fails on Gson-parsed numbers: " + e.getMessage());
		}
	}

	@Test
	public void getDip_scalesWithDensity() {
		float dip = SketchwareUtil.getDip(androidx.test.core.app.ApplicationProvider.getApplicationContext(), 10);
		assertTrue(dip > 0);
	}
}
