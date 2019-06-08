package me.tsukanov.counter.view;

import static me.tsukanov.counter.activities.SettingsActivity.KEY_REMOVE_COUNTERS;
import static me.tsukanov.counter.activities.SettingsActivity.KEY_VERSION;

import android.os.Bundle;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import me.tsukanov.counter.R;
import me.tsukanov.counter.SharedPrefKeys;

public final class SettingsFragment extends PreferenceFragmentCompat {

  private OnPreferenceClickListener mOnRemoveCountersClickListener;
  private String mAppVersion;
  private String mTheme;

  public void setOnRemoveCountersClickListener(
      OnPreferenceClickListener onRemoveCountersClickListener) {
    mOnRemoveCountersClickListener = onRemoveCountersClickListener;
  }

  public void setAppVersion(String appVersion) {
    mAppVersion = appVersion;
  }

  public void setTheme(String theme) {
    mTheme = theme;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Setting summaries for necessary preferences
    findPreference(SharedPrefKeys.THEME.getName()).setSummary(mTheme);
    findPreference(KEY_VERSION).setSummary(mAppVersion);

    findPreference(KEY_REMOVE_COUNTERS)
        .setOnPreferenceClickListener(mOnRemoveCountersClickListener);
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.settings, rootKey);
  }
}
