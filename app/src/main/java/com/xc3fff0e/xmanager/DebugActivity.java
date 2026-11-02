package com.xc3fff0e.xmanager;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Crash report screen launched by {@link xManager}'s uncaught exception handler.
 * Shows the stack trace passed in the "error" extra.
 */
public class DebugActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("xManager crashed");

		String error = getIntent() != null ? getIntent().getStringExtra("error") : null;
		if (error == null || error.isEmpty()) {
			error = getString(R.string.debug_no_error_details);
		}

		int padding = (int) (16 * getResources().getDisplayMetrics().density);
		TextView textView = new TextView(this);
		textView.setText(getString(R.string.debug_unexpected_error, error));
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextIsSelectable(true);
		textView.setPadding(padding, padding, padding, padding);

		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(textView);
		setContentView(scrollView);
	}
}
