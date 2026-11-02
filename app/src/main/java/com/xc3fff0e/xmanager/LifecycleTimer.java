package com.xc3fff0e.xmanager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@link Timer} that an activity cancels in onDestroy (REL-12).
 *
 * <p>The activities schedule many one-shot TimerTasks from callbacks (network responses,
 * clicks, animations). After {@link #cancel()} a plain Timer throws IllegalStateException
 * from {@code schedule(...)}, so a late callback would crash the app. This timer ignores
 * scheduling once it has been cancelled instead.
 */
final class LifecycleTimer extends Timer {

	private volatile boolean cancelled;

	boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void cancel() {
		cancelled = true;
		super.cancel();
	}

	@Override
	public void schedule(TimerTask task, long delay) {
		if (cancelled) return;
		try {
			super.schedule(task, delay);
		} catch (IllegalStateException e) {
			// Only swallow the race with cancel(); anything else is a real bug.
			if (!cancelled) throw e;
		}
	}

	@Override
	public void schedule(TimerTask task, long delay, long period) {
		if (cancelled) return;
		try {
			super.schedule(task, delay, period);
		} catch (IllegalStateException e) {
			// Only swallow the race with cancel(); anything else is a real bug.
			if (!cancelled) throw e;
		}
	}
}
