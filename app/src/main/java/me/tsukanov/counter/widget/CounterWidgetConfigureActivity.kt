package me.tsukanov.counter.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.view.Themes

/** Asks which counter a widget should show, when it's added to the home screen or reconfigured. */
class CounterWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        Themes.initCurrentTheme(PreferenceManager.getDefaultSharedPreferences(this))
        super.onCreate(savedInstanceState)

        // Backing out leaves a new widget unplaced
        setResult(RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val counters = CounterApplication.component!!.localStorage()!!.readAll(true).filterNotNull()
        val names = counters.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.widget_configure_title)
            .setItems(names) { _, which -> pick(names[which]) }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun pick(counterName: String) {
        WidgetCounters(this).set(appWidgetId, counterName)
        CounterWidget.update(this, AppWidgetManager.getInstance(this), appWidgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
}
