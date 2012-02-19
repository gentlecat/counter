package me.tsukanov.counter;

import java.util.LinkedHashMap;

import android.app.Application;

public class CounterApplication extends Application {

	public LinkedHashMap<String, Integer> counters = null;
	public static int theme = 0;
	public int activePosition = 0;
	public String activeKey = null;	
	public boolean isUpdateNeeded = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		theme = R.style.Theme_Sherlock_Light_DarkActionBar;
	}
}