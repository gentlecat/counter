package me.tsukanov.counter.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import me.tsukanov.counter.R;
import me.tsukanov.counter.SharedPrefKeys;
import me.tsukanov.counter.view.SettingsFragment;
import me.tsukanov.counter.view.Themes;

public class SettingsActivity extends AppCompatActivity
    implements OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

  private SharedPreferences sharedPrefs;
  private SettingsFragment settingsFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.layout_settings);

    MaterialToolbar actionBar = findViewById(R.id.settingsToolbar);
    setSupportActionBar(actionBar);

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    Themes.initCurrentTheme(sharedPrefs);

    settingsFragment = new SettingsFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settingsFrame, settingsFragment)
        .commit();
  }

  @Override
  public boolean onOptionsItemSelected(final @NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
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
  public boolean onPreferenceChange(final @NonNull Preference preference, final Object newValue) {
    return false;
  }

  @Override
  public void onSharedPreferenceChanged(
      final SharedPreferences sharedPreferences, final @Nullable String key) {
    if (key != null && key.equals(SharedPrefKeys.THEME.getName())) {
      final Preference pref = settingsFragment.findPreference(SharedPrefKeys.THEME.getName());
      if (pref != null) {
        pref.setSummary(getCurrentThemeName());
      }
      Themes.initCurrentTheme(sharedPrefs);
    }
  }

  private String getCurrentThemeName() {
    return getResources().getString(Themes.getCurrent(sharedPrefs).getLabelId());
  }
}
