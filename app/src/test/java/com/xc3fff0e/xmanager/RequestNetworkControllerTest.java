package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.os.Looper;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class RequestNetworkControllerTest {

	private MockWebServer server;
	private Activity activity;

	/** Captures the single callback a request produces. */
	static class Capture implements RequestNetwork.RequestListener {
		String tag, response, error;
		HashMap<String, Object> headers;
		boolean done;
		@Override public void onResponse(String tag, String response, HashMap<String, Object> h) {
			this.tag = tag; this.response = response; this.headers = h; done = true;
		}
		@Override public void onErrorResponse(String tag, String message) {
			this.tag = tag; this.error = message; done = true;
		}
	}

	@Before
	public void setUp() throws Exception {
		server = new MockWebServer();
		server.start();
		activity = Robolectric.buildActivity(Activity.class).setup().get();
	}

	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}

	private void await(Capture c) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (!c.done && System.currentTimeMillis() < deadline) {
			shadowOf(Looper.getMainLooper()).idle();
			Thread.sleep(20);
		}
		assertTrue("callback never delivered", c.done);
	}

	@Test
	public void get_appendsParamsAndHeaders_andTrimsBody() throws Exception {
		server.enqueue(new MockResponse().setBody("  {\"Server\":\"5.9\"}\n").addHeader("X-Test", "yes"));
		RequestNetwork rn = new RequestNetwork(activity);
		HashMap<String, Object> params = new HashMap<>();
		params.put("q", "a b");
		rn.setParams(params, RequestNetworkController.REQUEST_PARAM);
		HashMap<String, Object> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		rn.setHeaders(headers);

		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, server.url("/api.json").toString(), "api", c);
		await(c);

		RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
		assertEquals("GET", req.getMethod());
		assertEquals("a b", req.getRequestUrl().queryParameter("q"));
		assertEquals("application/json", req.getHeader("Accept"));
		assertEquals("api", c.tag);
		assertEquals("{\"Server\":\"5.9\"}", c.response);
		assertEquals("yes", c.headers.get("X-Test"));
		assertNull(c.error);
	}

	@Test
	public void post_paramMode_sendsFormBody() throws Exception {
		server.enqueue(new MockResponse().setBody("ok"));
		RequestNetwork rn = new RequestNetwork(activity);
		HashMap<String, Object> params = new HashMap<>();
		params.put("k", "v");
		rn.setParams(params, RequestNetworkController.REQUEST_PARAM);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.POST, server.url("/p").toString(), "t", c);
		await(c);
		RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
		assertEquals("POST", req.getMethod());
		assertEquals("k=v", req.getBody().readUtf8());
	}

	@Test
	public void post_bodyMode_sendsJson() throws Exception {
		server.enqueue(new MockResponse().setBody("ok"));
		RequestNetwork rn = new RequestNetwork(activity);
		HashMap<String, Object> params = new HashMap<>();
		params.put("n", 1);
		rn.setParams(params, RequestNetworkController.REQUEST_BODY);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.POST, server.url("/j").toString(), "t", c);
		await(c);
		RecordedRequest req = server.takeRequest(5, TimeUnit.SECONDS);
		assertTrue(req.getHeader("Content-Type").startsWith("application/json"));
		assertEquals("{\"n\":1}", req.getBody().readUtf8());
	}

	@Test
	public void httpServerError_isDeliveredAsError() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(503).setBody("maintenance"));
		RequestNetwork rn = new RequestNetwork(activity);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, server.url("/x").toString(), "t", c);
		await(c);
		assertNull(c.response);
		assertNotNull(c.error);
		assertTrue(c.error, c.error.startsWith("HTTP 503"));
	}

	@Test
	public void httpClientError_isDeliveredAsError() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(404).setBody("nope"));
		RequestNetwork rn = new RequestNetwork(activity);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, server.url("/missing").toString(), "t", c);
		await(c);
		assertNull(c.response);
		assertTrue(c.error, c.error.startsWith("HTTP 404"));
	}

	@Test
	public void emptyBody_givesEmptyString() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(204));
		RequestNetwork rn = new RequestNetwork(activity);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, server.url("/e").toString(), "t", c);
		await(c);
		assertEquals("", c.response);
	}

	@Test
	public void emptyUrl_reportsErrorSynchronously() {
		// R.string.api_url is currently "" in the fork; this is the path _API() takes on startup.
		RequestNetwork rn = new RequestNetwork(activity);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, "", "null", c);
		assertTrue(c.done);
		assertNotNull(c.error);
		assertTrue(c.error.contains("unexpected url"));
	}

	@Test
	public void connectionFailure_reportsError() throws Exception {
		String url = server.url("/gone").toString();
		server.shutdown();
		RequestNetwork rn = new RequestNetwork(activity);
		Capture c = new Capture();
		rn.startRequestNetwork(RequestNetworkController.GET, url, "t", c);
		await(c);
		assertNotNull(c.error);
		assertNull(c.response);
	}

	@Test
	public void cleartextHttpUrl_isNotBlockedByOkHttpOnJvm() {
		// Sanity: the controller itself has no scheme allow-list; HTTPS-only is enforced by
		// network_security_config on device. See ENHANCEMENTS SEC-* (enforce https in code).
		assertNotNull(okhttp3.HttpUrl.parse("http://example.org/a.json"));
		assertNull(okhttp3.HttpUrl.parse("ftp://example.org/a.json"));
	}
}
