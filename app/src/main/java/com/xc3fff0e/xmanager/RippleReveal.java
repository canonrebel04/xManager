package com.xc3fff0e.xmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Full-screen circular "page switch" reveal, replacing the unmaintained
 * com.wuyr:rippleanimation library.
 *
 * Call {@link #start(View, long)} right before changing the UI. The current screen is
 * frozen as a snapshot, the UI change is applied underneath, and on the next frame the
 * new screen is revealed in a circle growing from the center of {@code origin}
 * (starting at half the origin's larger side, ending at the farthest screen corner),
 * using {@link ViewAnimationUtils#createCircularReveal}. Touches are blocked while it runs.
 */
public final class RippleReveal {

	private RippleReveal() {}

	public static void start(final View origin, final long durationMs) {
		final Activity activity = findActivity(origin.getContext());
		if (activity == null || activity.isFinishing()) return;
		final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
		final int w = decor.getWidth();
		final int h = decor.getHeight();
		if (w <= 0 || h <= 0) return;

		// Reveal center = center of the origin view, in decor coordinates.
		int[] o = new int[2];
		int[] d = new int[2];
		origin.getLocationInWindow(o);
		decor.getLocationInWindow(d);
		final int cx = o[0] - d[0] + origin.getWidth() / 2;
		final int cy = o[1] - d[1] + origin.getHeight() / 2;
		final float startRadius = Math.max(origin.getWidth(), origin.getHeight()) / 2f;
		final float endRadius = (float) Math.max(
				Math.max(Math.hypot(cx, cy), Math.hypot(w - cx, cy)),
				Math.max(Math.hypot(cx, h - cy), Math.hypot(w - cx, h - cy)));

		final Bitmap oldShot = snapshot(decor, w, h);
		if (oldShot == null) return;

		// Overlay showing the old screen; blocks touches until the reveal is done.
		final FrameLayout overlay = new FrameLayout(activity);
		overlay.setClickable(true);
		final ImageView oldView = new ImageView(activity);
		oldView.setImageBitmap(oldShot);
		overlay.addView(oldView, new FrameLayout.LayoutParams(-1, -1));
		decor.addView(overlay, new ViewGroup.LayoutParams(-1, -1));

		// After the caller's UI change has been laid out, snapshot the new screen and reveal it.
		decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				decor.getViewTreeObserver().removeOnPreDrawListener(this);
				overlay.setVisibility(View.INVISIBLE);
				final Bitmap newShot = snapshot(decor, w, h);
				overlay.setVisibility(View.VISIBLE);
				if (newShot == null) {
					finish(decor, overlay, oldShot, null);
					return true;
				}
				final ImageView newView = new ImageView(activity);
				newView.setImageBitmap(newShot);
				newView.setVisibility(View.INVISIBLE);
				overlay.addView(newView, new FrameLayout.LayoutParams(-1, -1));
				newView.post(new Runnable() {
					@Override
					public void run() {
						if (!newView.isAttachedToWindow()) {
							finish(decor, overlay, oldShot, newShot);
							return;
						}
						Animator anim = ViewAnimationUtils.createCircularReveal(newView, cx, cy, startRadius, endRadius);
						anim.setDuration(durationMs);
						anim.setInterpolator(new AccelerateDecelerateInterpolator());
						anim.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								finish(decor, overlay, oldShot, newShot);
							}
						});
						newView.setVisibility(View.VISIBLE);
						anim.start();
					}
				});
				return true;
			}
		});
	}

	private static Bitmap snapshot(View view, int w, int h) {
		try {
			Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			view.draw(new Canvas(bmp));
			return bmp;
		} catch (Throwable t) { // OOM or a hardware bitmap that can't be drawn in software
			return null;
		}
	}

	private static void finish(ViewGroup decor, View overlay, Bitmap a, Bitmap b) {
		decor.removeView(overlay);
		if (a != null) a.recycle();
		if (b != null) b.recycle();
	}

	private static Activity findActivity(Context c) {
		while (c instanceof ContextWrapper) {
			if (c instanceof Activity) return (Activity) c;
			c = ((ContextWrapper) c).getBaseContext();
		}
		return null;
	}
}
