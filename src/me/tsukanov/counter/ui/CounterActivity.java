package me.tsukanov.counter.ui;

import java.util.ArrayList;
import java.util.List;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
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
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener {

	private static final int DIALOG_ADD = 100;
	private static final int DIALOG_EDIT = 101;
	private static final int DIALOG_DELETE = 102;

	CounterApplication app;
	ActionBar actionBar;
	CounterFragment currentFragment;
	SharedPreferences settings;
	List<String> keys;
	ArrayAdapter<String> navigationAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		app = (CounterApplication) getApplication();
		
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		createNavigation();
	}

	@Override
	protected void onResume() {
		super.onResume();		
		if (app.isUpdateNeeded) {
			app.isUpdateNeeded = false;
			refreshActivity();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		app.saveData();
		SharedPreferences.Editor settingsEditor = settings.edit();
		settingsEditor.putString("activeKey", app.activeKey);
		settingsEditor.commit();
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
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		app.activeKey = keys.get(itemPosition);
		currentFragment = new CounterFragment();
		getSupportFragmentManager().beginTransaction()
			.replace(android.R.id.content, currentFragment).commit();
		return true;
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
		Dialog dialog;
		switch (id) {
		case DIALOG_ADD:
			dialog = getAddDialog().create();
			break;
		case DIALOG_EDIT:
			dialog = getEditDialog().create();
			break;
		case DIALOG_DELETE:
			dialog = getDeleteDialog().create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private Builder getAddDialog() {
		AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(this);
		addDialogBuilder.setTitle(getResources().getText(
				R.string.dialog_add_title));

		LinearLayout addDialogLayout = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.editor_layout, null);

		// Name input label
		TextView nameInputLabel = new TextView(this);
		nameInputLabel.setText(getResources()
				.getText(R.string.dialog_edit_name));
		addDialogLayout.addView(nameInputLabel);

		// Name input
		final EditText nameInput = new EditText(this);
		nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
		nameInput.setText("");
		addDialogLayout.addView(nameInput);

		// Value input label
		TextView valueInputLabel = new TextView(this);
		valueInputLabel.setText(getResources().getText(
				R.string.dialog_edit_value));
		valueInputLabel.setPadding(0, 10, 0, 0);
		addDialogLayout.addView(valueInputLabel);

		// Value input
		final EditText valueInput = new EditText(this);
		valueInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		valueInput.setText("");
		InputFilter[] valueFilter = new InputFilter[1];
		valueFilter[0] = new InputFilter.LengthFilter(getCharLimit());
		valueInput.setFilters(valueFilter);
		addDialogLayout.addView(valueInput);

		addDialogBuilder
			.setView(addDialogLayout)
			.setPositiveButton(getResources().getText(R.string.dialog_button_add),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = nameInput.getText().toString();
						if (name.equals("")) {
							Toast.makeText(getBaseContext(),
									getResources().getText(R.string.toast_no_name_message),
									Toast.LENGTH_SHORT).show();
						} else {
							int value = 0;
							String valueInputContents = valueInput.getText().toString();
							if (!valueInputContents.equals("")) {
								value = Integer.parseInt(valueInputContents);
							}
							app.counters.put(name, value);
							recreateNavigation();
							actionBar.setSelectedNavigationItem(findPosition(name));
						}
					}
				})
			.setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
		return addDialogBuilder;
	}

	private Builder getEditDialog() {
		AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(this);
		editDialogBuilder.setTitle(getResources().getText(R.string.dialog_edit_title));

		LinearLayout editDialogLayout = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.editor_layout, null);

		// Name input label
		TextView nameInputLabel = new TextView(this);
		nameInputLabel.setText(getResources()
				.getText(R.string.dialog_edit_name));
		editDialogLayout.addView(nameInputLabel);

		// Name input
		final EditText nameInput = new EditText(this);
		nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
		nameInput.setText(app.activeKey);
		editDialogLayout.addView(nameInput);

		// Value input label
		TextView valueInputLabel = new TextView(this);
		valueInputLabel.setText(getResources().getText(
				R.string.dialog_edit_value));
		valueInputLabel.setPadding(0, 10, 0, 0);
		editDialogLayout.addView(valueInputLabel);

		// Value input
		final EditText valueInput = new EditText(this);
		valueInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		valueInput.setText(String.valueOf(app.counters.get(app.activeKey)));
		InputFilter[] valueFilter = new InputFilter[1];
		valueFilter[0] = new InputFilter.LengthFilter(getCharLimit());
		valueInput.setFilters(valueFilter);
		editDialogLayout.addView(valueInput);

		editDialogBuilder
			.setView(editDialogLayout)
			.setPositiveButton(getResources().getText(R.string.dialog_button_apply),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String name = nameInput.getText().toString();
						if (name.equals("")) {
							Toast.makeText(getBaseContext(),
									getResources().getText(R.string.toast_no_name_message),
									Toast.LENGTH_SHORT).show();
						} else {
							int value = 0;
							String valueInputContents = valueInput.getText().toString();
							if (!valueInputContents.equals("")) {
								value = Integer.parseInt(valueInputContents);
							}
							app.counters.remove(app.activeKey);
							app.counters.put(name, value);
							recreateNavigation();
							actionBar.setSelectedNavigationItem(findPosition(name));
						}
					}
				})
			.setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
		return editDialogBuilder;
	}

	private Builder getDeleteDialog() {
		AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
		deleteDialogBuilder
			.setMessage(getResources().getText(R.string.dialog_delete_title))
			.setCancelable(false)
			.setPositiveButton(getResources().getText(R.string.dialog_button_delete),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						app.counters.remove(app.activeKey);
						Toast.makeText(getBaseContext(),
							getResources().getText(R.string.toast_remove_sucess_1)
								+ " \"" + app.activeKey + "\" "
								+ getResources().getText(R.string.toast_remove_sucess_2),
							Toast.LENGTH_SHORT).show();
						recreateNavigation();
					}
				})
			.setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
		return deleteDialogBuilder;
	}
	
	private int getCharLimit() {
		return String.valueOf(CounterFragment.getMaxValue()).length();
	}

	private void recreateNavigation() {
		app.saveData();
		createNavigation();
	}

	private void createNavigation() {
		app.loadData();
		keys = new ArrayList<String>();
		for (String key : app.counters.keySet()) {
			keys.add(key);
		}
		Context context = getSupportActionBar().getThemedContext();
		navigationAdapter = new ArrayAdapter<String>(context, R.layout.sherlock_spinner_item, keys);
		navigationAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		actionBar.setListNavigationCallbacks(navigationAdapter, this);
		// Restore previously selected element
		actionBar.setSelectedNavigationItem(findPosition(app.activeKey));
	}
	
	private int findPosition(String key) {
		for (int i = 0; i < navigationAdapter.getCount(); i++)
			if (key.equals(navigationAdapter.getItem(i)))
				return i;
		return 0;
	}

	private void refreshActivity() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

}
