package me.tsukanov.counter.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Preference versionPref = findPreference("version");
        versionPref.setSummary(getAppVersion());

        getPreferenceManager().findPreference("removeCounters")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showWipeDialog();
                        return true;
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        if (root != null) {
            root.addView(bar, 0);
            bar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private String getAppVersion() {
        try {
            return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return getResources().getString(R.string.unknown);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    private void showWipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.settings_wipe_confirmation);
        builder.setPositiveButton(R.string.settings_wipe_confirmation_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CounterApplication app = (CounterApplication) getApplication();
                app.removeCounters();

                SharedPreferences.Editor settingsEditor = sharedPref.edit();
                if (app.counters.isEmpty()) {
                    settingsEditor.putString(MainActivity.STATE_ACTIVE_COUNTER, getString(R.string.default_counter_name));
                } else {
                    settingsEditor.putString(MainActivity.STATE_ACTIVE_COUNTER, app.counters.keySet().iterator().next());
                }
                settingsEditor.commit();

                Toast.makeText(
                        getBaseContext(),
                        getResources().getText(R.string.toast_wipe_success),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
