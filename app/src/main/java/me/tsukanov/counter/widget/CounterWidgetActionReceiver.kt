package me.tsukanov.counter.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.repository.exceptions.MissingCounterException

/**
 * Handles the widget's + and − buttons. It isn't exported, so only the widget's own pending intents can
 * change counters.
 */
class CounterWidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val counterName = WidgetCounters(context).get(appWidgetId) ?: return
        val storage = CounterApplication.component!!.localStorage()!!
        val counter =
            try {
                storage.read(counterName)
            } catch (e: MissingCounterException) {
                null
            }
        if (counter == null) {
            CounterWidget.update(context, AppWidgetManager.getInstance(context), appWidgetId)
            return
        }

        when (intent.action) {
            ACTION_INCREMENT -> counter.increment()
            ACTION_DECREMENT -> counter.decrement()
            else -> return
        }
        // Saving notifies the app and redraws the widgets
        storage.write(counter)
    }

    companion object {
        const val ACTION_INCREMENT = "me.tsukanov.counter.widget.INCREMENT"
        const val ACTION_DECREMENT = "me.tsukanov.counter.widget.DECREMENT"
    }
}
