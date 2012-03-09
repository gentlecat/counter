package me.tsukanov.counter.ui;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener {

	CounterApplication app;
	Intent starterIntent;
	ActionBar actionBar;

	ListPreference themePreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		app = (CounterApplication) getApplication();
		starterIntent = getIntent();

		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		String version = (String) getResources().getText(R.string.unknown);
		try {
			version = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Preference versionPref = findPreference("version");
		versionPref.setSummary(version);

		getPreferenceManager().findPreference("clearData")
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						app.clearData();
						Toast.makeText(getBaseContext(),
								getResources().getText(R.string.toast_wipe_success),
								Toast.LENGTH_SHORT).show();
						return true;
					}
				});
		
		Log.v("Activities", "Settings activity created");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

}