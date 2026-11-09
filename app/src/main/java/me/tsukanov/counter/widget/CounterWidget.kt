package me.tsukanov.counter.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.activities.MainActivity
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.repository.exceptions.MissingCounterException

/** Home screen widget that shows one counter with buttons to count up and down. */
class CounterWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            update(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetCounters = WidgetCounters(context)
        for (appWidgetId in appWidgetIds) {
            widgetCounters.remove(appWidgetId)
        }
    }

    companion object {
        /** Redraws every widget, for example after a counter has changed. */
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CounterWidget::class.java))
            for (appWidgetId in appWidgetIds) {
                update(context, appWidgetManager, appWidgetId)
            }
        }

        fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.counter_widget)
            val counterName = WidgetCounters(context).get(appWidgetId)
            val counter =
                counterName?.let {
                    try {
                        CounterApplication.component!!.localStorage()!!.read(it)
                    } catch (e: MissingCounterException) {
                        null
                    }
                }

            views.setTextViewText(R.id.widgetName, counterName ?: "")
            if (counter != null) {
                views.setTextViewText(R.id.widgetValue, counter.value.toString())
                views.setViewVisibility(R.id.widgetValue, View.VISIBLE)
                views.setViewVisibility(R.id.widgetMessage, View.GONE)
                views.setViewVisibility(R.id.widgetButtons, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.widgetCounter, openCounterIntent(context, appWidgetId, counter.name))
                views.setOnClickPendingIntent(
                    R.id.widgetIncrement,
                    countIntent(context, appWidgetId, CounterWidgetActionReceiver.ACTION_INCREMENT),
                )
                views.setOnClickPendingIntent(
                    R.id.widgetDecrement,
                    countIntent(context, appWidgetId, CounterWidgetActionReceiver.ACTION_DECREMENT),
                )
            } else {
                // The counter was deleted or renamed elsewhere; tapping lets the user pick another one
                views.setViewVisibility(R.id.widgetValue, View.GONE)
                views.setViewVisibility(R.id.widgetMessage, View.VISIBLE)
                views.setViewVisibility(R.id.widgetButtons, View.GONE)
                views.setOnClickPendingIntent(R.id.widgetCounter, configureIntent(context, appWidgetId))
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun openCounterIntent(context: Context, appWidgetId: Int, counterName: String): PendingIntent {
            val intent =
                Intent(context, MainActivity::class.java)
                    .putExtra(BroadcastHelper.EXTRA_COUNTER_NAME, counterName)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    )
            return PendingIntent.getActivity(context, appWidgetId, intent, PENDING_INTENT_FLAGS)
        }

        private fun configureIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent =
                Intent(context, CounterWidgetConfigureActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(context, appWidgetId, intent, PENDING_INTENT_FLAGS)
        }

        private fun countIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
            val intent =
                Intent(context, CounterWidgetActionReceiver::class.java)
                    .setAction(action)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getBroadcast(context, appWidgetId, intent, PENDING_INTENT_FLAGS)
        }

        private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
