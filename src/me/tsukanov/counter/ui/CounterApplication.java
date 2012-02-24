package me.tsukanov.counter.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import me.tsukanov.counter.R;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class CounterApplication extends Application {
	
	private static final String DATA_FILE_NAME = "data";
	
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
		
		// Setting default theme
		theme = R.style.Theme_Sherlock; // Theme_Sherlock = Dark
		
		// Loading counters
		counters = new LinkedHashMap<String, Integer>();
		SharedPreferences data = getBaseContext().getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE);
		Map<String, ?> dataMap = data.getAll();
		if (dataMap.isEmpty()) {
			counters.put((String) getResources().getText(R.string.default_counter_name),
					CounterFragment.getDefaultValue());
		} else {
			for (Map.Entry<String, ?> entry : dataMap.entrySet())
				counters.put(entry.getKey(), (Integer) entry.getValue());
		}
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