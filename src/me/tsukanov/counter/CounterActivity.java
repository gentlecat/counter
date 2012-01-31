package me.tsukanov.counter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (!app.isRotated) {
			app.activePosition = itemPosition;
			currentFragment = CounterFragment.newInstance(itemPosition);
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, currentFragment).commit();
		} else {
			app.isRotated = false;
			actionBar.setSelectedNavigationItem(app.activePosition);
		}
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		app.isRotated = true;
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
			// TODO Open settings
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

	private void goHome() {
		// actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		// TODO Open counter fragment
	}

	private void openSettings() {
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Simple Counter settings");
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		// TODO Open settings fragment
	}

}
