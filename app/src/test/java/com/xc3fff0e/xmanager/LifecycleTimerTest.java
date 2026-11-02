package com.xc3fff0e.xmanager;

import static org.junit.Assert.*;

import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/** REL-12: the activities cancel their timer in onDestroy; late schedule() calls must not crash. */
public class LifecycleTimerTest {

	private static TimerTask countDown(final CountDownLatch latch) {
		return new TimerTask() {
			@Override
			public void run() {
				latch.countDown();
			}
		};
	}

	@Test
	public void runsTasksBeforeCancel() throws Exception {
		LifecycleTimer t = new LifecycleTimer();
		CountDownLatch latch = new CountDownLatch(1);
		t.schedule(countDown(latch), 10);
		assertTrue(latch.await(2, TimeUnit.SECONDS));
		t.cancel();
	}

	@Test
	public void cancel_dropsPendingTasks() throws Exception {
		LifecycleTimer t = new LifecycleTimer();
		CountDownLatch latch = new CountDownLatch(1);
		t.schedule(countDown(latch), 200);
		t.cancel();
		assertTrue(t.isCancelled());
		assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
	}

	@Test
	public void scheduleAfterCancel_isIgnoredInsteadOfThrowing() throws Exception {
		LifecycleTimer t = new LifecycleTimer();
		t.cancel();
		CountDownLatch latch = new CountDownLatch(2);
		t.schedule(countDown(latch), 0);
		t.schedule(countDown(latch), 0, 10);
		assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
	}

	@Test(expected = IllegalStateException.class)
	public void reschedulingTheSameTask_stillThrowsWhileRunning() {
		LifecycleTimer t = new LifecycleTimer();
		try {
			TimerTask task = countDown(new CountDownLatch(1));
			t.schedule(task, 10_000);
			t.schedule(task, 10_000);
		} finally {
			t.cancel();
		}
	}
}
