package com.xc3fff0e.xmanager;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

public class xManager extends Application {
	
	private static Context mApplicationContext;
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
	
	public static Context getContext() {
		return mApplicationContext;
	}
	
	@Override
	public void onCreate() {
		mApplicationContext = getApplicationContext();
		enableStrictModeInDebug();
		this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		
		Thread.setDefaultUncaughtExceptionHandler(
		new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.putExtra("error", Log.getStackTraceString(throwable));
				
				PendingIntent pendingIntent =
				PendingIntent.getActivity(
				getApplicationContext(),
				11111,
				intent,
				PendingIntent.FLAG_ONE_SHOT
				| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
				);
				
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
				
				xManagerLogger.broadcastLog(Log.getStackTraceString(throwable));
				Process.killProcess(Process.myPid());
				System.exit(1);
				
				uncaughtExceptionHandler.uncaughtException(thread, throwable);
			}
		});
		xManagerLogger.startLogging();
		super.onCreate();
	}
	
	/**
	 * PERF-12: in debug builds, log (never crash on) disk and network access on the main
	 * thread and leaked resources, so ANR causes show up in logcat during development.
	 * Release builds are unaffected.
	 */
	static boolean enableStrictModeInDebug() {
		if (!BuildConfig.DEBUG) return false;
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build());
		return true;
	}
}
