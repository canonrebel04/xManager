package com.xc3fff0e.xmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class LanguageTagsTest {

	@Test
	public void tags_areUniqueAndWellFormed() {
		Set<String> seen = new HashSet<>();
		for (String tag : LanguageTags.TAGS) {
			assertTrue(tag, tag.matches("[a-z]{2,3}(-[A-Za-z0-9]{1,8})*"));
			assertTrue("duplicate " + tag, seen.add(tag));
		}
		assertEquals(46, LanguageTags.TAGS.length);
	}

	@Test
	public void indexOf_acceptsTagsAndLegacyIndexes() {
		assertEquals(10, LanguageTags.indexOf("ru"));
		assertEquals(10, LanguageTags.indexOf("10"));
		assertEquals(33, LanguageTags.indexOf("de-x-formal"));
		assertEquals(20, LanguageTags.indexOf("de"));
		assertEquals(-1, LanguageTags.indexOf(""));
		assertEquals(-1, LanguageTags.indexOf(null));
		assertEquals(-1, LanguageTags.indexOf("46"));
		assertEquals(-1, LanguageTags.indexOf("xx"));
	}

	@Test
	public void matches_usesIndex() {
		assertTrue(LanguageTags.matches("en", 0));
		assertTrue(LanguageTags.matches("0", 0));
		assertFalse(LanguageTags.matches("", 0));
	}
}
