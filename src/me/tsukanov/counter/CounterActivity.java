package me.tsukanov.counter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	private static final String DATA_FILE = "data_test_11";
	private static final int DIALOG_ADD = 100;
	private static final int DIALOG_EDIT = 101;
	private static final int DIALOG_DELETE = 102;

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
		sharedPreferences = getBaseContext().getSharedPreferences(DATA_FILE,
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
		sharedPreferences = getBaseContext().getSharedPreferences(DATA_FILE,
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
		case R.id.menu_add:
			showDialog(DIALOG_ADD);
			return true;
		case R.id.menu_edit:
			showDialog(DIALOG_EDIT);
			return true;
		case R.id.menu_delete:
			showDialog(DIALOG_DELETE);
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		LayoutInflater inflater = getLayoutInflater();	
		switch (id) {
		case DIALOG_ADD:				
            LinearLayout addDialogLayout = new LinearLayout(this);
			addDialogLayout.addView(inflater.inflate(R.layout.edit_dialog, addDialogLayout, false));
			AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(this);
			addDialogBuilder
					.setTitle(getResources().getText(R.string.dialog_title_add))
					.setView(addDialogLayout)
					.setPositiveButton(getResources().getText(R.string.dialog_button_add),
							new OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// TODO Add counter
								}
							})
					.setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
			dialog = addDialogBuilder.create();
			break;
		case DIALOG_EDIT:	
            LinearLayout editDialogLayout = new LinearLayout(this);
			editDialogLayout.addView(inflater.inflate(R.layout.edit_dialog, editDialogLayout, false));
			AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(this);
			editDialogBuilder
					.setTitle(getResources().getText(R.string.dialog_title_add))
					.setView(editDialogLayout)
					.setPositiveButton(getResources().getText(R.string.dialog_button_apply),
							new OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// TODO Add counter
								}
							})
					.setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
			dialog = editDialogBuilder.create();
			break;
		case DIALOG_DELETE:
			AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(
					this);
			deleteDialogBuilder
					.setMessage(getResources().getText(R.string.dialog_title_delete))
					.setCancelable(false)
					.setPositiveButton(getResources().getText(R.string.dialog_button_delete),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// TODO Delete counter
								}
							})
					.setNegativeButton(getResources().getText(R.string.dialog_button_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog = deleteDialogBuilder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

}
