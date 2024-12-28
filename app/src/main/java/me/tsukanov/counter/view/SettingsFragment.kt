package me.tsukanov.counter.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.SharedPrefKeys
import me.tsukanov.counter.view.Themes.Companion.getCurrent
import org.apache.commons.lang3.StringUtils
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {

            val sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this.requireActivity())
            findPreference<Preference>(SharedPrefKeys.THEME.key)
                ?.setSummary(
                    resources.getString(getCurrent(sharedPrefs).labelId)
                )

            findPreference<Preference>(KEY_VERSION)!!.summary =
                appVersion

            findPreference<Preference>(KEY_REMOVE_COUNTERS)!!.onPreferenceClickListener =
                onRemoveCountersClickListener
            findPreference<Preference>(KEY_EXPORT_COUNTERS)!!.onPreferenceClickListener =
                onExportClickListener

            findPreference<Preference>(KEY_HOMEPAGE)!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { p: Preference? ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://counter.roman.zone?utm_source=app")
                        )
                    )
                    true
                }

            findPreference<Preference>(KEY_TIP)!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { p: Preference? ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://counter.roman.zone/tip?utm_source=app")
                        )
                    )
                    true
                }

        } catch (e: NullPointerException) {
            Log.e(TAG, "Unable to retrieve one of the preferences", e)
        }
    }

    private val appVersion: String
        get() {
            try {
                val versionName =
                    this.activity
                        ?.packageManager
                        ?.getPackageInfo(this.requireActivity().packageName, 0)
                        ?.versionName
                return if (StringUtils.isNotEmpty(versionName)) {
                    versionName!!
                } else {
                    resources.getString(R.string.unknown_version)
                }
            } catch (e: NullPointerException) {
                return resources.getString(R.string.unknown_version)
            } catch (e: PackageManager.NameNotFoundException) {
                return resources.getString(R.string.unknown_version)
            }
        }

    private val onRemoveCountersClickListener: Preference.OnPreferenceClickListener
        get() = Preference.OnPreferenceClickListener { preference: Preference? ->
            showWipeDialog()
            true
        }

    private val onExportClickListener: Preference.OnPreferenceClickListener
        get() = Preference.OnPreferenceClickListener { preference: Preference? ->
            try {
                export()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Error occurred while exporting counters",
                    e
                )
                Toast.makeText(
                    this.activity,
                    resources.getText(R.string.toast_unable_to_export),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            true
        }

    private fun showWipeDialog() {
        val builder = AlertDialog.Builder(this.activity)
        builder.setMessage(R.string.settings_wipe_confirmation)
        builder.setPositiveButton(
            R.string.settings_wipe_confirmation_yes
        ) { dialog: DialogInterface?, id: Int ->
            CounterApplication.component!!.localStorage()!!.wipe()
            Toast.makeText(
                this.activity,
                resources.getText(R.string.toast_wipe_success),
                Toast.LENGTH_SHORT
            )
                .show()
        }
        builder.setNegativeButton(
            R.string.dialog_button_cancel
        ) { dialog: DialogInterface, id: Int -> dialog.dismiss() }

        builder.create().show()
    }

    @Throws(IOException::class)
    private fun export() {
        val exportIntent = Intent()
        exportIntent.setAction(Intent.ACTION_SEND)
        exportIntent.putExtra(
            Intent.EXTRA_TEXT, CounterApplication.component!!.localStorage()!!.toCsv()
        )
        exportIntent.setType("text/csv")

        val shareIntent =
            Intent.createChooser(exportIntent, resources.getText(R.string.settings_export_title))
        startActivity(shareIntent)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    companion object {
        private val TAG: String = SettingsFragment::class.java.simpleName

        const val KEY_REMOVE_COUNTERS: String = "removeCounters"
        const val KEY_EXPORT_COUNTERS: String = "exportCounters"
        const val KEY_HOMEPAGE: String = "homepage"
        const val KEY_TIP: String = "tip"
        const val KEY_VERSION: String = "version"
    }
}
