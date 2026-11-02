package com.xc3fff0e.xmanager;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

/**
 * On a fresh install (connectedAndroidTest reinstalls every run) MainActivity relaunches the
 * app a few seconds after first start (_Initialize "Initial optimization" and _Refetch "Checking
 * cached datas"), which destroys the activity an ActivityScenario is holding. Tests that are not
 * about first run mark both steps as already done.
 */
final class FirstRun {
	private FirstRun() { }

	static void markDone() {
		Context ctx = ApplicationProvider.getApplicationContext();
		ctx.getSharedPreferences("INITIALIZATION", Context.MODE_PRIVATE)
				.edit().putString("INITIALIZATION", "X").commit();
		ctx.getSharedPreferences("REFETCH", Context.MODE_PRIVATE)
				.edit().putString("REFETCH", "X").commit();
	}
}
