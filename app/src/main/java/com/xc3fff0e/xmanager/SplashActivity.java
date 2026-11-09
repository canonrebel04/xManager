package com.xc3fff0e.xmanager;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class SplashActivity extends AppCompatActivity {

	static final int[] TAGLINES = {
		R.string.app_name,
		R.string.splash_tagline_1,
		R.string.splash_tagline_2,
		R.string.splash_tagline_3,
		R.string.splash_tagline_4
	};
	
	private final LifecycleTimer _timer = new LifecycleTimer();
	
	private LinearLayout main_body;
	private ImageView icon_splash;
	private TextView title_splash;
	
	private TimerTask Timer;
	private Intent Switch_Activity = new Intent();
	private SharedPreferences THEME;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.splash);
		getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				_onBackPressed();
			}
		});
		initialize(_savedInstanceState);
		
		
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		main_body = findViewById(R.id.main_body);
		icon_splash = findViewById(R.id.icon_splash);
		title_splash = findViewById(R.id.title_splash);
		THEME = getSharedPreferences(Prefs.THEME, Activity.MODE_PRIVATE);
	}
	
	private void initializeLogic() {
		Window w =SplashActivity.this.getWindow();
		w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); w.setStatusBarColor(0xFF171717);
		Glide.with(getApplicationContext()).load(Uri.parse("file:///android_asset/splash.gif")).into(icon_splash);
		// Load MainActivity's font and OkHttp off the main thread while the splash is showing.
		Fonts.preloadAsync(getApplicationContext().getAssets(), Fonts.BASE);
		RequestNetworkController.warmUpAsync();
		title_splash.setTypeface(Fonts.get(getAssets(), Fonts.GLITCH), Typeface.BOLD);
		// One draw, so each of the five titles is equally likely (the old if/else chain
		// re-rolled at every step and mostly showed the first entries).
		int pick = SketchwareUtil.getRandom(0, TAGLINES.length - 1);
		title_splash.setText(TAGLINES[pick]);
		title_splash.setTextSize(pick == TAGLINES.length - 1 ? 20 : 25);
		// A11Y-05: skip the fade-in when the user turned animations off.
		if (RippleReveal.animationsEnabled(this)) {
			Animation animation_icon;
			animation_icon = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
			animation_icon.setDuration(800); icon_splash.startAnimation(animation_icon);
			animation_icon = null;
			
			Animation animation_title;
			animation_title = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
			animation_title.setDuration(800); title_splash.startAnimation(animation_title);
			animation_title = null;
		}
		
		Timer = new TimerTask() {
				@Override
				public void run() {
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
										Switch_Activity.setClass(getApplicationContext(), MainActivity.class);
										startActivity(Switch_Activity);
										finish();
								}
						});
				}
		};
		_timer.schedule(Timer, (int)(1000));
		
		_Dark_Navigation();
		_Theme_UI();
	}
	
	@Override
	protected void onDestroy() {
		// REL-12: if the splash goes away early (back, rotation, process trim), don't let its
		// pending TimerTask start MainActivity from a destroyed activity.
		_timer.cancel();
		super.onDestroy();
	}
	
	private void _onBackPressed() {
		com.google.android.material.snackbar.Snackbar.make(main_body, "Can't Go Back", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
	}
	public void _Dark_Navigation() {
			getWindow().setNavigationBarColor(Color.parseColor("#171717"));
		
	}
	
	
	public void _Theme_UI() {
		if (THEME.getString(Prefs.THEME, "").equals("14")) {
			main_body.setBackgroundColor(0xFF1B5E20);
		}
		else {
			if (THEME.getString(Prefs.THEME, "").equals("15")) {
				main_body.setBackgroundColor(0xFF00008B);
			}
			else {
				if (THEME.getString(Prefs.THEME, "").equals("16")) {
					main_body.setBackgroundColor(0xFF8B0000);
				}
				else {
					if (THEME.getString(Prefs.THEME, "").equals("17")) {
						main_body.setBackgroundColor(0xFFFF8C00);
					}
					else {
						if (THEME.getString(Prefs.THEME, "").equals("18")) {
							main_body.setBackgroundColor(0xFFAA336A);
						}
						else {
							if (THEME.getString(Prefs.THEME, "").equals("19")) {
								main_body.setBackgroundColor(0xFF301934);
							}
							else {
								if (THEME.getString(Prefs.THEME, "").equals("20")) {
									main_body.setBackgroundColor(0xFF008B8B);
								}
								else {
									main_body.setBackgroundColor(0xFF171717);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx)) {
				_result.add((double)_arr.keyAt(_iIdx));
			}
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}
