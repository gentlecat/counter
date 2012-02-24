package me.tsukanov.counter.ui;

import java.util.LinkedHashMap;

import me.tsukanov.counter.R;
import android.app.Application;

public class CounterApplication extends Application {
	
	// Counters	(String = Name, Integer = Value)
	public LinkedHashMap<String, Integer> counters;
	
	// Active theme
	public static int theme;
	
	// Active counter's name (key string in LinkedHashMap)
	public String activeKey;

	// Active counter's ID based on LinkedHashMap order
	public int activePosition = 0;

	// True if theme was changed or/and data removed
	public boolean isUpdateNeeded = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Set default theme
		theme = R.style.Theme_Sherlock; // Theme_Sherlock = Dark
	}
	
	public void changeTheme(String name) {
		if (name.equals("light")) { 
			// Light theme
			theme = R.style.Theme_Sherlock_Light_DarkActionBar;
		} else { 
			// Default theme
			theme = R.style.Theme_Sherlock;
		}
	}

}