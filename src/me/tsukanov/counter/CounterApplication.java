package me.tsukanov.counter;

import java.util.HashMap;

import android.app.Application;

public class CounterApplication extends Application {

	public int activePosition;
	public static int theme;
	public boolean isUpdateNeeded;
	public HashMap<String, Integer> counters;

	@Override
	public void onCreate() {
		super.onCreate();
		theme = R.style.Theme_Sherlock_Light_DarkActionBar;
		activePosition = 0;
		isUpdateNeeded = false;	
	}
}