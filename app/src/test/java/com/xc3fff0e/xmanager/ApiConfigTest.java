package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import org.junit.Test;

public class ApiConfigTest {

	@Test
	public void defaultBuild_hasNoEndpoint() {
		// The endpoint is supplied with -PxmanagerApiUrl; nothing is baked into the repository.
		assertEquals("", ApiConfig.url());
		assertFalse(ApiConfig.isUsable(ApiConfig.url()));
	}

	@Test
	public void emptyOrBlank_isNotUsable() {
		assertFalse(ApiConfig.isUsable(null));
		assertFalse(ApiConfig.isUsable(""));
		assertFalse(ApiConfig.isUsable("   "));
	}

	@Test
	public void malformed_isNotUsable() {
		assertFalse(ApiConfig.isUsable("not a url"));
		assertFalse(ApiConfig.isUsable("ftp://example.org/api.json"));
		assertFalse(ApiConfig.isUsable("example.org/api.json"));
		assertFalse(ApiConfig.isUsable("http://"));
		assertFalse(ApiConfig.isUsable("https:///api.json"));
	}

	@Test
	public void httpAndHttps_areUsable() {
		assertTrue(ApiConfig.isUsable("https://example.org/api.json"));
		assertTrue(ApiConfig.isUsable(" http://10.0.2.2:8080/api.json "));
		assertTrue(ApiConfig.isUsable("HTTPS://Example.org/api.json?v=2"));
		assertTrue(ApiConfig.isUsable("http://[::1]:8765/api.json"));
	}
}
