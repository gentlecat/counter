package me.tsukanov.counter;

import java.util.HashMap;

import android.app.Application;

public class CounterApplication extends Application {

	public int activePosition = 0;

	public static int theme;
	public boolean themeChanged; 
	public HashMap<String, Integer> counters;

	@Override
	public void onCreate() {
		super.onCreate();
		theme = R.style.Theme_Sherlock_Light_DarkActionBar;
		themeChanged = false;
		
		counters = new HashMap<String, Integer>();
		counters.put("First", 1);
		counters.put("Second", 2);
		counters.put("Third", 3);		
	}
}