package me.tsukanov.counter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	private static final String FILENAME = "data_test_11";

	CounterApplication app;
	ActionBar actionBar;
	CounterFragment currentFragment;

	SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(CounterApplication.theme);
		super.onCreate(savedInstanceState);

		app = (CounterApplication) getApplication();
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

		app.counters = new HashMap<String, Integer>();

		sharedPreferences = getBaseContext().getSharedPreferences(FILENAME,
				Context.MODE_PRIVATE);
		Map<String, ?> prefsMap = sharedPreferences.getAll();
		if (prefsMap.isEmpty()) {
			Log.v("SharedPreferences", "Map is empty");
			app.counters.put("First counter", 0);
		} else {
			for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
				app.counters.put(entry.getKey(), (Integer) entry.getValue());
				Log.v("SharedPreferences", "Loading!");
			}
		}

		List<String> list = new ArrayList<String>();
		for (String key : app.counters.keySet()) {
			list.add(key);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, this);
		// Restore previously selected element
		actionBar.setSelectedNavigationItem(actionBar
				.getSelectedNavigationIndex());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (app.isUpdateNeeded) {
			app.isUpdateNeeded = false;
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		sharedPreferences = getBaseContext().getSharedPreferences(FILENAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		for (String name : app.counters.keySet()) {
			Log.v("SharedPreferences", "Saving!");
			editor.putInt(name, app.counters.get(name));
			editor.commit();
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		app.activePosition = itemPosition;
		currentFragment = CounterFragment.newInstance(itemPosition);
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, currentFragment).commit();
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (prefs.getBoolean("hardControlOn", true)) {
				currentFragment.increment();
				return true;
			}
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (prefs.getBoolean("hardControlOn", true)) {
				currentFragment.decrement();
				return true;
			}
			return false;
		case KeyEvent.KEYCODE_CAMERA:
			if (prefs.getBoolean("hardControlOn", true)) {
				currentFragment.refresh();
				return true;
			}
			return false;
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
