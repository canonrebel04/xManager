package com.xc3fff0e.xmanager;

import static com.xc3fff0e.xmanager.TargetSupport.readRepoFile;
import static com.xc3fff0e.xmanager.TargetSupport.repoRoot;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.Test;

/**
 * Backlog targets for CI and instrumented-test setup. These can't run an emulator from the JVM,
 * so they check the files that decide what CI does. Each is expected to FAIL until its item is
 * done; docs/TARGETS.md lists the parts that still need a manual CI check.
 */
public class BacklogTargetsRepoTest {

	private static final String WORKFLOW = ".github/workflows/android-tests.yml";
	private static final String ANDROID_TEST = "app/src/androidTest/java/com/xc3fff0e/xmanager/";

	/** Code only: drops line and block comments so a mention in a comment doesn't count. */
	private static String code(String java) {
		return java.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
	}

	// ---------------------------------------------------------------- A11Y-11

	/**
	 * A11Y-11: CI runs AccessibilityChecksTest as its own named, blocking step (for example
	 * -Pandroid.testInstrumentationRunnerArguments.class=...AccessibilityChecksTest), so an ATF
	 * regression fails the build visibly and can't be lost in a filter. The step must not use
	 * continue-on-error.
	 */
	@Test
	public void A11Y11_ciRunsAccessibilityChecksAsBlockingStep() {
		String wf = readRepoFile(WORKFLOW);
		assertTrue("workflow never names AccessibilityChecksTest", wf.contains("AccessibilityChecksTest"));
		// The step that names it must not be allowed to fail.
		int at = wf.indexOf("AccessibilityChecksTest");
		int stepStart = wf.lastIndexOf("- name:", at);
		int stepEnd = wf.indexOf("- name:", at);
		String step = wf.substring(Math.max(0, stepStart), stepEnd < 0 ? wf.length() : stepEnd);
		assertFalse("AccessibilityChecksTest step has continue-on-error", step.matches("(?s).*continue-on-error:\\s*true.*"));
	}

	/**
	 * A11Y-11: AccessibilityChecksTest keeps a suppression list for known issues
	 * (AccessibilityValidator.setSuppressingResultMatcher), so CI can be gated now and the list
	 * shrinks as issues are fixed.
	 */
	@Test
	public void A11Y11_accessibilityChecks_haveSuppressionList() {
		String src = code(readRepoFile(ANDROID_TEST + "AccessibilityChecksTest.java"));
		assertTrue("AccessibilityChecksTest has no suppression list (setSuppressingResultMatcher)",
				src.contains("setSuppressingResultMatcher"));
	}

	// ---------------------------------------------------------------- CI-18

	/**
	 * CI-18: SmokeTest waits on app state (IdlingResource, UiDevice.wait/waitForIdle or
	 * Espresso sync) instead of fixed Thread.sleep() pauses, which hide slowness on a loaded
	 * emulator until it turns into an ANR or a flaky failure.
	 */
	@Test
	public void CI18_smokeTest_hasNoFixedSleeps() {
		String src = code(readRepoFile(ANDROID_TEST + "SmokeTest.java"));
		Matcher m = Pattern.compile("\\b(Thread\\.)?sleep\\s*\\(").matcher(src);
		int count = 0;
		while (m.find()) count++;
		assertEquals("SmokeTest still has " + count + " fixed sleep(...) calls", 0, count);
	}

	/**
	 * CI-18: SmokeTest's launch timeout is at least 30 s. The 15 s value timed out on a
	 * software-rendered emulator (docs/emulator/PR2_RESULTS.md).
	 */
	@Test
	public void CI18_smokeTest_launchTimeoutAtLeast30s() {
		String src = code(readRepoFile(ANDROID_TEST + "SmokeTest.java"));
		Matcher m = Pattern.compile("TIMEOUT\\s*=\\s*([0-9_]+)L?\\s*;").matcher(src);
		assertTrue("SmokeTest has no TIMEOUT constant", m.find());
		long timeout = Long.parseLong(m.group(1).replace("_", ""));
		assertTrue("SmokeTest TIMEOUT is " + timeout + " ms; needs >= 30000", timeout >= 30_000);
	}

	// ---------------------------------------------------------------- TEST-04

	/**
	 * TEST-04: at least one instrumented test serves a fixture from MockWebServer (through the
	 * ApiConfig.setUrlForTesting hook), and build.gradle has mockwebserver as an
	 * androidTestImplementation dependency.
	 */
	@Test
	public void TEST04_androidTest_usesMockWebServer() throws Exception {
		String gradle = readRepoFile("app/build.gradle");
		assertTrue("app/build.gradle has no androidTestImplementation mockwebserver",
				gradle.matches("(?s).*androidTestImplementation\\s+['\"]com\\.squareup\\.okhttp3:mockwebserver.*"));
		Path dir = new File(repoRoot(), ANDROID_TEST).toPath();
		boolean found;
		try (Stream<Path> files = Files.walk(dir)) {
			found = files.filter(p -> p.toString().endsWith(".java")).anyMatch(p -> {
				try {
					String s = code(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
					return s.contains("MockWebServer") && s.contains("setUrlForTesting");
				} catch (java.io.IOException e) {
					return false;
				}
			});
		}
		assertTrue("no androidTest uses MockWebServer with ApiConfig.setUrlForTesting", found);
	}
}
