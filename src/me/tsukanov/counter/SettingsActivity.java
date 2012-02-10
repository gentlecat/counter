package me.tsukanov.counter;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener {

	CounterApplication app;
	Intent starterintent;
	ActionBar actionBar;

	ListPreference themePreference;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(CounterApplication.theme);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		app = (CounterApplication) getApplication();
		starterintent = getIntent();
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		PreferenceScreen prefScreen = getPreferenceScreen();
		themePreference = (ListPreference) prefScreen.findPreference("theme");
		themePreference.setOnPreferenceChangeListener(this);

		String app_version = "Unknown";
		try {
			app_version = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Preference version = findPreference("version");
		version.setSummary(app_version);

		getPreferenceManager().findPreference("clearData")
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Wipe
						Toast.makeText(getBaseContext(),
								"NOTHING LEFT! MWAHAHHAHA!", Toast.LENGTH_SHORT)
								.show();
						return true;
					}
				});
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == themePreference) {
			app.themeChanged = true;
			String value = (String) newValue;
			if (value.equals("dark")) {
				CounterApplication.theme = R.style.Theme_Sherlock;
			} else if (value.equals("light")) {
				CounterApplication.theme = R.style.Theme_Sherlock_Light_DarkActionBar;
			}
			// TODO Delete toast
			Toast.makeText(getBaseContext(), (String) newValue,
					Toast.LENGTH_SHORT).show();
			finish();
            startActivity(starterintent);
			return true;
		}

		return false;
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

}