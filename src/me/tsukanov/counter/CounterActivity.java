package me.tsukanov.counter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	private static final int HELP_DIALOG = 101;

	CounterApplication app;
	CounterFragment currentFragment;
	ActionBar actionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(app.theme);
		super.onCreate(savedInstanceState);
		app = (CounterApplication) getApplication();

		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);

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
		actionBar.setSelectedNavigationItem(app.activePosition);
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
		case R.id.menu_help:
			showDialog(HELP_DIALOG);
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case HELP_DIALOG:
			dialog = new AlertDialog.Builder(this)
					.setView(getLayoutInflater().inflate(R.layout.help, null))
					.setTitle(R.string.menu_help)
					.setCancelable(true)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).create();
		default:
			dialog = null;
		}
		return dialog;
	}

}
