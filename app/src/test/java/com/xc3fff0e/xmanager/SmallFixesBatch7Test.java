package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.animation.LayoutTransition;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Startup ANR follow-up (deferred LayoutTransition, outline trim), A11Y-05, TEST-02, TEST-03. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SmallFixesBatch7Test {

	private static void idle() {
		shadowOf(Looper.getMainLooper()).idle();
	}

	private static Object field(Object o, String name) throws Exception {
		Field f = o.getClass().getDeclaredField(name);
		f.setAccessible(true);
		return f.get(o);
	}

	@Test
	public void startup_layoutTransitionsAreInstalledOnlyAfterTheFirstFrame() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).create().start().resume();
		MainActivity a = c.get();
		for (int id : MainActivity.ANIMATED_CONTAINERS) {
			ViewGroup g = a.findViewById(id);
			assertNotNull(g);
			assertNull("no LayoutTransition during the first layout", g.getLayoutTransition());
		}
		c.visible();
		shadowOf(Looper.getMainLooper()).idleFor(100, TimeUnit.MILLISECONDS);
		for (int id : MainActivity.ANIMATED_CONTAINERS) {
			LayoutTransition t = ((ViewGroup) a.findViewById(id)).getLayoutTransition();
			assertNotNull("LayoutTransition installed after the first frame", t);
			assertTrue(t.isTransitionTypeEnabled(LayoutTransition.CHANGING));
			assertTrue(t.isTransitionTypeEnabled(LayoutTransition.APPEARING));
		}
		// Applying twice keeps the same transition objects.
		ViewGroup first = a.findViewById(MainActivity.ANIMATED_CONTAINERS[0]);
		LayoutTransition before = first.getLayoutTransition();
		a._Apply_Layout_Transitions();
		assertSame(before, first.getLayoutTransition());
	}

	@Test
	public void startup_outlinesDroppedOnlyWhereNoShadowIsDrawn() {
		Context ctx = ApplicationProvider.getApplicationContext();
		FrameLayout root = new FrameLayout(ctx);
		View flat = new View(ctx);
		View raised = new View(ctx);
		raised.setElevation(4f);
		View clipped = new View(ctx);
		clipped.setClipToOutline(true);
		View lifted = new View(ctx);
		lifted.setTranslationZ(2f);
		root.addView(flat);
		root.addView(raised);
		root.addView(clipped);
		root.addView(lifted);

		assertEquals(2, MainActivity._Trim_Outlines(root)); // root + flat
		assertNull(root.getOutlineProvider());
		assertNull(flat.getOutlineProvider());
		assertSame(ViewOutlineProvider.BACKGROUND, raised.getOutlineProvider());
		assertSame(ViewOutlineProvider.BACKGROUND, clipped.getOutlineProvider());
		assertSame(ViewOutlineProvider.BACKGROUND, lifted.getOutlineProvider());
		assertEquals(0, MainActivity._Trim_Outlines(null));
	}

	@Test
	public void startup_homeScreenKeepsTheRefreshSpinnerShadow() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		ViewGroup refresh = a.findViewById(R.id.main_refresh_layout);
		assertNull(refresh.getOutlineProvider());
		boolean sawElevated = false;
		for (int i = 0; i < refresh.getChildCount(); i++) {
			View child = refresh.getChildAt(i);
			if (child.getElevation() > 0f) {
				sawElevated = true;
				assertNotNull("views with a shadow keep their outline", child.getOutlineProvider());
			}
		}
		assertTrue("SwipeRefreshLayout's spinner circle has elevation", sawElevated);
	}

	@Test
	public void A11Y05_animationsEnabledFollowsAnimatorScale() {
		Context ctx = ApplicationProvider.getApplicationContext();
		Settings.Global.putFloat(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
		assertTrue(RippleReveal.animationsEnabled(ctx));
		Settings.Global.putFloat(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
		assertFalse(RippleReveal.animationsEnabled(ctx));
		Settings.Global.putFloat(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f);
		assertTrue(RippleReveal.animationsEnabled(ctx));
	}

	@Test
	public void A11Y05_noRevealOverlayWhenAnimationsAreOff() {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		ViewGroup decor = (ViewGroup) a.getWindow().getDecorView();
		Settings.Global.putFloat(a.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
		int before = decor.getChildCount();
		RippleReveal.start(a.findViewById(R.id.main_refresh_layout), 1000);
		assertEquals("no snapshot overlay is added", before, decor.getChildCount());
	}

	@Test
	public void A11Y05_splashSkipsFadeInWhenAnimationsAreOff() {
		Context ctx = ApplicationProvider.getApplicationContext();
		Settings.Global.putFloat(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 0f);
		// create() only: once frames run, a finished fade-in clears itself from the view.
		ActivityController<SplashActivity> off = Robolectric.buildActivity(SplashActivity.class).create();
		assertNull(off.get().findViewById(R.id.icon_splash).getAnimation());
		assertNull(off.get().findViewById(R.id.title_splash).getAnimation());
		off.destroy();

		Settings.Global.putFloat(ctx.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
		ActivityController<SplashActivity> on = Robolectric.buildActivity(SplashActivity.class).create();
		assertNotNull(on.get().findViewById(R.id.icon_splash).getAnimation());
		on.destroy();
	}

	/** TEST-02: every theme applies without crashing and selects its own spinner entry. */
	@Test
	public void TEST02_everyThemeApplies() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences theme = (SharedPreferences) field(a, "THEME");
		Spinner spinner = (Spinner) field(a, "select_theme");
		int count = spinner.getAdapter().getCount();
		assertEquals(21, count);
		for (int i = 0; i < count; i++) {
			theme.edit().putString("THEME", String.valueOf(i)).commit();
			a._Theme_UI();
			idle();
			assertEquals("theme " + i, i, spinner.getSelectedItemPosition());
			assertNotNull("theme " + i, a.findViewById(R.id.main_refresh_layout).getBackground());
		}
	}

	/** TEST-03: every language applies without crashing and selects its own spinner entry. */
	@Test
	public void TEST03_everyLanguageApplies() throws Exception {
		ActivityController<MainActivity> c = Robolectric.buildActivity(MainActivity.class).setup();
		idle();
		MainActivity a = c.get();
		SharedPreferences language = (SharedPreferences) field(a, "LANGUAGE");
		Spinner spinner = (Spinner) field(a, "select_language");
		assertEquals(LanguageTags.TAGS.length, spinner.getAdapter().getCount());
		TextView title = (TextView) field(a, "main_title");
		for (int i = 0; i < LanguageTags.TAGS.length; i++) {
			language.edit().putString(LanguageTags.KEY, LanguageTags.tagForIndex(i)).commit();
			a._Language_UI();
			idle();
			assertEquals("language " + LanguageTags.tagForIndex(i), i, spinner.getSelectedItemPosition());
			assertFalse("language " + LanguageTags.tagForIndex(i), title.getText().toString().trim().isEmpty());
		}
	}
}
