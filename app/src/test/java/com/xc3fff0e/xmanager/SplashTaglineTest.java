package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** UI-11: the splash title is picked with one draw, so every entry is reachable and equally likely. */
public class SplashTaglineTest {

	@Test
	public void taglines_areDistinct() {
		Set<Integer> ids = new HashSet<>();
		for (int id : SplashActivity.TAGLINES) ids.add(id);
		assertEquals(5, SplashActivity.TAGLINES.length);
		assertEquals(SplashActivity.TAGLINES.length, ids.size());
	}

	@Test
	public void singleDraw_coversEveryIndexRoughlyEvenly() {
		int n = SplashActivity.TAGLINES.length;
		int[] hits = new int[n];
		int draws = 50_000;
		for (int i = 0; i < draws; i++) hits[SketchwareUtil.getRandom(0, n - 1)]++;
		for (int h : hits) {
			// expected 10,000 each; the old chain gave the first entry ~20% and the last ~41%
			assertTrue("skewed: " + h, Math.abs(h - draws / n) < draws / n / 10);
		}
	}
}
