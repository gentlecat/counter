package me.tsukanov.counter.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Remembers which counter each home screen widget shows, by the counter's name. */
class WidgetCounters(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    fun get(appWidgetId: Int): String? = prefs.getString(key(appWidgetId), null)

    fun set(appWidgetId: Int, counterName: String) {
        prefs.edit { putString(key(appWidgetId), counterName) }
    }

    fun remove(appWidgetId: Int) {
        prefs.edit { remove(key(appWidgetId)) }
    }

    /** Points widgets that showed [oldName] at [newName], so they follow a renamed counter. */
    fun rename(oldName: String, newName: String) {
        prefs.edit {
            for ((key, name) in renamed(all(), oldName, newName)) {
                putString(key, name)
            }
        }
    }

    private fun all(): Map<String, String> = prefs.all.mapNotNull { (key, value) ->
        (value as? String)?.let { key to it }
    }.toMap()

    companion object {
        private const val PREFS_FILE_NAME = "widgets"

        private fun key(appWidgetId: Int) = "widget_$appWidgetId"

        /** Returns the entries of [mapping] that need to change when [oldName] becomes [newName]. */
        fun renamed(mapping: Map<String, String>, oldName: String, newName: String): Map<String, String> =
            mapping.filterValues { it == oldName }.mapValues { newName }
    }
}
