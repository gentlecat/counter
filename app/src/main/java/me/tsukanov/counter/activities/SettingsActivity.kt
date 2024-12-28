package me.tsukanov.counter.activities

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import me.tsukanov.counter.R
import me.tsukanov.counter.SharedPrefKeys
import me.tsukanov.counter.view.SettingsFragment
import me.tsukanov.counter.view.Themes

class SettingsActivity :
    AppCompatActivity(),
    Preference.OnPreferenceChangeListener,
    OnSharedPreferenceChangeListener {

    private var sharedPrefs: SharedPreferences? = null
    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_settings)

        val actionBar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        setSupportActionBar(actionBar)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        Themes.initCurrentTheme(sharedPrefs!!)

        settingsFragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsFrame, settingsFragment!!)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        sharedPrefs!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPrefs!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return false
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences, key: String?
    ) {
        if (key != null && key == SharedPrefKeys.THEME.key) {
            val pref = settingsFragment!!.findPreference<Preference>(SharedPrefKeys.THEME.key)
            if (pref != null) {
                pref.summary = currentThemeName
            }
            Themes.initCurrentTheme(sharedPrefs!!)
        }
    }

    private val currentThemeName: String
        get() = resources.getString(Themes.getCurrent(sharedPrefs!!).labelId)
}
