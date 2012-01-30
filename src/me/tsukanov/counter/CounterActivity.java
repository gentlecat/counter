package me.tsukanov.counter;

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

	CounterApplication application;
	CounterFragment currentFragment;
	ActionBar actionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (CounterApplication) getApplication();
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this,
				R.array.action_list,
				android.R.layout.simple_dropdown_item_1line);
		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(list, this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (!application.isRotated) {
			application.activePosition = itemPosition;
			currentFragment = CounterFragment.newInstance(itemPosition);
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, currentFragment).commit();
		} else {
			application.isRotated = false;
			actionBar.setSelectedNavigationItem(application.activePosition);
		}
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		application.isRotated = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			currentFragment.refresh();
			return true;
		case R.id.menu_help:
			showDialog(HELP_DIALOG);
			return true;
		case R.id.menu_settings:
			openSettings();
			return true;
		case android.R.id.home:
			goHome();
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

