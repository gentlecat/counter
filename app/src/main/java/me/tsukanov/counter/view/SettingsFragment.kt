package me.tsukanov.counter.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import java.io.IOException;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.SharedPrefKeys;
import org.apache.commons.lang3.StringUtils;

public final class SettingsFragment extends PreferenceFragmentCompat {

  private static final String TAG = SettingsFragment.class.getSimpleName();

  public static final String KEY_REMOVE_COUNTERS = "removeCounters";
  public static final String KEY_EXPORT_COUNTERS = "exportCounters";
  public static final String KEY_HOMEPAGE = "homepage";
  public static final String KEY_TIP = "tip";
  public static final String KEY_VERSION = "version";

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {

      SharedPreferences sharedPrefs =
          PreferenceManager.getDefaultSharedPreferences(this.getActivity());
      findPreference(SharedPrefKeys.THEME.getName())
          .setSummary(getResources().getString(Themes.getCurrent(sharedPrefs).getLabelId()));

      findPreference(KEY_VERSION).setSummary(getAppVersion());

      findPreference(KEY_REMOVE_COUNTERS)
          .setOnPreferenceClickListener(getOnRemoveCountersClickListener());
      findPreference(KEY_EXPORT_COUNTERS).setOnPreferenceClickListener(getOnExportClickListener());

      findPreference(KEY_HOMEPAGE)
          .setOnPreferenceClickListener(
              (Preference p) -> {
                startActivity(
                    new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://counter.roman.zone?utm_source=app")));
                return true;
              });

      findPreference(KEY_TIP)
          .setOnPreferenceClickListener(
              (Preference p) -> {
                startActivity(
                    new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://counter.roman.zone/tip?utm_source=app")));
                return true;
              });

    } catch (NullPointerException e) {
      Log.e(TAG, "Unable to retrieve one of the preferences", e);
    }
  }

  @NonNull
  private String getAppVersion() {
    try {
      final String versionName =
          this.getActivity()
              .getPackageManager()
              .getPackageInfo(this.getActivity().getPackageName(), 0)
              .versionName;
      if (StringUtils.isNotEmpty(versionName)) {
        return versionName;
      } else {
        return getResources().getString(R.string.unknown_version);
      }
    } catch (NullPointerException | PackageManager.NameNotFoundException e) {
      return getResources().getString(R.string.unknown_version);
    }
  }

  @NonNull
  private OnPreferenceClickListener getOnRemoveCountersClickListener() {
    return preference -> {
      showWipeDialog();
      return true;
    };
  }

  @NonNull
  private OnPreferenceClickListener getOnExportClickListener() {
    return preference -> {
      try {
        export();
      } catch (IOException e) {
        Log.e(TAG, "Error occurred while exporting counters", e);
        Toast.makeText(
                this.getActivity(),
                getResources().getText(R.string.toast_unable_to_export),
                Toast.LENGTH_SHORT)
            .show();
      }
      return true;
    };
  }

  private void showWipeDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
    builder.setMessage(R.string.settings_wipe_confirmation);
    builder.setPositiveButton(
        R.string.settings_wipe_confirmation_yes,
        (dialog, id) -> {
          CounterApplication.getComponent().localStorage().wipe();
          Toast.makeText(
                  this.getActivity(),
                  getResources().getText(R.string.toast_wipe_success),
                  Toast.LENGTH_SHORT)
              .show();
        });
    builder.setNegativeButton(R.string.dialog_button_cancel, (dialog, id) -> dialog.dismiss());

    builder.create().show();
  }

  private void export() throws IOException {
    final Intent exportIntent = new Intent();
    exportIntent.setAction(Intent.ACTION_SEND);
    exportIntent.putExtra(
        Intent.EXTRA_TEXT, CounterApplication.getComponent().localStorage().toCsv());
    exportIntent.setType("text/csv");

    final Intent shareIntent =
        Intent.createChooser(exportIntent, getResources().getText(R.string.settings_export_title));
    startActivity(shareIntent);
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.settings, rootKey);
  }
}
