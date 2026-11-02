package com.xc3fff0e.xmanager;

import static org.junit.Assert.assertTrue;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TEST-04: runs MainActivity against a local MockWebServer that serves a small fixture,
 * so the version list can be checked on a device without the real update server.
 */
@RunWith(AndroidJUnit4.class)
public class MockApiServerTest {

	private static final String PKG = "com.xc3fff0e.xmanager";
	private static final String LATEST = "8.9.10.611";
	private static final long TIMEOUT_MS = 30_000;

	private MockWebServer server;

	@Before
	public void setUp() throws Exception {
		FirstRun.markDone();
		server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(200).setBody(fixture()));
		server.start();
		ApiConfig.setUrlForTesting(server.url("/api.json").toString());
	}

	@After
	public void tearDown() throws Exception {
		ApiConfig.setUrlForTesting(null);
		server.shutdown();
	}

	@Test
	public void fixtureFromMockServer_reachesLatestLabel() throws Exception {
		try (ActivityScenario<MainActivity> s = ActivityScenario.launch(MainActivity.class)) {
			assertTrue("app never requested the mock server",
					server.takeRequest(TIMEOUT_MS, TimeUnit.MILLISECONDS) != null);
			UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
			boolean loaded = device.wait(Until.hasObject(By.res(PKG, "sub_1").text(LATEST)), TIMEOUT_MS);
			AtomicReference<String> shown = new AtomicReference<>("");
			s.onActivity(a -> shown.set(((TextView) a.findViewById(R.id.sub_1)).getText().toString()));
			assertTrue("LATEST label shows '" + shown.get() + "', not the fixture's " + LATEST, loaded);
		}
	}

	private static String list(String prefix) {
		StringBuilder b = new StringBuilder("[");
		for (int i = 1; i <= 2; i++) {
			if (i > 1) b.append(',');
			b.append("{\"Title\":\"").append(prefix).append(" 8.9.10.").append(i)
					.append("\",\"Description\":\"").append(prefix).append(" item ").append(i)
					.append("\",\"Link_1\":\"https://example.org/").append(i).append("/a\"")
					.append(",\"Link_2\":\"https://example.org/").append(i).append("/b\"}");
		}
		return b.append(']').toString();
	}

	private static String fixture() {
		return "{"
				+ "\"Stock_Patched\":" + list("Stock") + ","
				+ "\"Amoled_Patched\":" + list("Amoled") + ","
				+ "\"Stock_Cloned_Patched\":" + list("Stock cloned") + ","
				+ "\"Amoled_Cloned_Patched\":" + list("Amoled cloned") + ","
				+ "\"Other_Patched\":" + list("Other") + ","
				+ "\"Patched_Changelogs\":" + list("Changelog") + ","
				+ "\"Stock_Patched_Latest\":\"" + LATEST + "\","
				+ "\"Amoled_Patched_Latest\":\"8.9.10.612\","
				+ "\"SCP_Latest\":\"8.9.10.613\","
				+ "\"ACP_Latest\":\"8.9.10.614\","
				+ "\"Server\":\"5.9\","
				+ "\"Update\":\"https://example.org/update\","
				+ "\"App_Changelogs\":\"Fixes\","
				+ "\"Supporters\":\"Everyone\""
				+ "}";
	}
}
