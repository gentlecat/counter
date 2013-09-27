package me.tsukanov.counter.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String version;
        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            version = getResources().getString(R.string.unknown);
        }
        Preference versionPref = findPreference("version");
        versionPref.setSummary(version);

        getPreferenceManager().findPreference("removeCounters")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        CounterApplication app = (CounterApplication) getApplication();
                        app.removeCounters();

                        SharedPreferences.Editor settingsEditor = sharedPref.edit();
                        if (app.counters.isEmpty()) {
                            settingsEditor.putString("activeKey", getString(R.string.default_counter_name));
                        } else {
                            settingsEditor.putString("activeKey", app.counters.keySet().iterator().next());
                        }
                        settingsEditor.commit();

                        Toast.makeText(
                                getBaseContext(),
                                getResources().getText(R.string.toast_wipe_success),
                                Toast.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}