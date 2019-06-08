package me.tsukanov.counter.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference.OnPreferenceClickListener;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.SharedPrefKeys;
import me.tsukanov.counter.view.SettingsFragment;
import me.tsukanov.counter.view.Themes;

public class SettingsActivity extends AppCompatActivity
    implements OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String KEY_REMOVE_COUNTERS = "removeCounters";
  public static final String KEY_VERSION = "version";

  private SharedPreferences sharedPrefs;
  private SettingsFragment settingsFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    Themes.initCurrentTheme(sharedPrefs);

    initSettingsFragment();
  }

  private void initSettingsFragment() {
    settingsFragment = new SettingsFragment();
    settingsFragment.setOnRemoveCountersClickListener(getOnRemoveCountersClickListener());
    settingsFragment.setAppVersion(getAppVersion());
    settingsFragment.setTheme(getCurrentThemeName());

    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
  }

  private String getCurrentThemeName() {
    return getResources()
        .getString(
            Themes.findOrGetDefault(sharedPrefs.getString(SharedPrefKeys.THEME.getName(), null))
                .getLabelId());
  }

  private OnPreferenceClickListener getOnRemoveCountersClickListener() {
    return preference -> {
      showWipeDialog();
      return true;
    };
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    getDelegate().onPostCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private String getAppVersion() {
    try {
      return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      return getResources().getString(R.string.unknown);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    sharedPrefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    return false;
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(SharedPrefKeys.THEME.getName())) {
      final Preference pref = settingsFragment.findPreference(SharedPrefKeys.THEME.getName());
      if (pref != null) {
        pref.setSummary(getCurrentThemeName());
      }
      Themes.initCurrentTheme(sharedPrefs);
    }
  }

  private void showWipeDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.settings_wipe_confirmation);
    builder.setPositiveButton(
        R.string.settings_wipe_confirmation_yes,
        (dialog, id) -> {
          CounterApplication.getComponent().localStorage().wipe();
          Toast.makeText(
                  getBaseContext(),
                  getResources().getText(R.string.toast_wipe_success),
                  Toast.LENGTH_SHORT)
              .show();
        });
    builder.setNegativeButton(R.string.dialog_button_cancel, (dialog, id) -> dialog.dismiss());

    builder.create().show();
  }
}
