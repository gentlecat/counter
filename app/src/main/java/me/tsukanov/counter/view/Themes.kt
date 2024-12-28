package me.tsukanov.counter.view

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import me.tsukanov.counter.R
import me.tsukanov.counter.SharedPrefKeys

enum class Themes(private val identifier: String, val labelId: Int) {
    LIGHT("light", R.string.settings_theme_light),
    DARK("dark", R.string.settings_theme_dark),
    AUTO("auto", R.string.settings_theme_auto_battery),
    SYSTEM("system", R.string.settings_theme_system);

    companion object {
        fun getCurrent(sharedPrefs: SharedPreferences): Themes {
            val identifier = sharedPrefs.getString(SharedPrefKeys.THEME.key, SYSTEM.name)!!
            for (t in entries) {
                if (t.identifier == identifier) {
                    return t
                }
            }
            return SYSTEM
        }

        /**
         * Sets up current theme.
         *
         * @param sharedPrefs [SharedPreferences] that contain the theme preference.
         */
        fun initCurrentTheme(sharedPrefs: SharedPreferences) {
            val currentTheme = getCurrent(sharedPrefs)

            when (currentTheme) {
                LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}
