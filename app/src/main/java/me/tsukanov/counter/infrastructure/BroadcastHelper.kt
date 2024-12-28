package me.tsukanov.counter.infrastructure

import android.content.Context
import android.content.Intent
import android.util.Log
import me.tsukanov.counter.CounterApplication

class BroadcastHelper(private val context: Context) {

    private fun sendBroadcast(intent: Intent) {
        context.sendBroadcast(intent)
    }

    fun sendBroadcast(action: Actions) {
        val intent = Intent()
        intent.setAction(action.actionName)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        sendBroadcast(intent)
    }

    fun sendSelectCounterBroadcast(counterName: String) {
        val intent = Intent()
        intent.setAction(Actions.SELECT_COUNTER.actionName)
        intent.putExtra(EXTRA_COUNTER_NAME, counterName)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        sendBroadcast(intent)

        Log.d(
            TAG,
            String.format("Sending counter selection broadcast [counterName=%s]", counterName)
        )
    }

    companion object {
        @JvmField
        val EXTRA_COUNTER_NAME: String =
            String.format("%s.EXTRA_COUNTER_NAME", CounterApplication.PACKAGE_NAME)

        private val TAG: String = BroadcastHelper::class.java.simpleName
    }
}
