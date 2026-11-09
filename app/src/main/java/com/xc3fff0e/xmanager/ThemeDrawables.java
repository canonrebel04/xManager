package com.xc3fff0e.xmanager;

import android.graphics.drawable.GradientDrawable;

/**
 * Small helper for the many rounded solid backgrounds the theme code builds. Avoids the
 * anonymous-subclass {@code getIns} pattern and keeps corner/color setup in one place.
 */
final class ThemeDrawables {

	private ThemeDrawables() {}

	/** A new solid rounded rectangle; callers may set it on a view without sharing. */
	static GradientDrawable rounded(int cornerRadiusPx, int color) {
		GradientDrawable d = new GradientDrawable();
		d.setShape(GradientDrawable.RECTANGLE);
		d.setCornerRadius(cornerRadiusPx);
		d.setColor(color);
		return d;
	}
}
