package me.tsukanov.counter.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.domain.exception.CounterException
import me.tsukanov.counter.infrastructure.Actions
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.repository.exceptions.MissingCounterException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import java.io.IOException
import java.util.LinkedList

/**
 * Counter storage that uses [SharedPreferences] as a medium.
 *
 *
 * This implementation us based on the [IntegerCounter] variation of the [Counter].
 */
class SharedPrefsCounterStorage(
    context: Context,
    private val broadcastHelper: BroadcastHelper,
    private val defaultCounterName: String
) : CounterStorage<IntegerCounter> {

    private val values: SharedPreferences =
        context.getSharedPreferences(VALUES_FILE_NAME, Context.MODE_PRIVATE)
    private val updateTimestamps: SharedPreferences =
        context.getSharedPreferences(UPDATE_TIMESTAMPS_FILE_NAME, Context.MODE_PRIVATE)

    override fun readAll(addDefault: Boolean): List<IntegerCounter> {
        val counters: MutableList<IntegerCounter> = LinkedList()

        val valuesMap = values.all

        try {
            if (valuesMap.isEmpty() && addDefault) {
                val defaultCounter = IntegerCounter(this.defaultCounterName)
                counters.add(defaultCounter)
                write(defaultCounter)
                return counters
            }

            for ((key, value) in valuesMap) {
                val updateTimestampStr = updateTimestamps.getString(key, null)
                val updateTimestamp =
                    if (updateTimestampStr != null)
                        DateTime.parse(updateTimestampStr, TIMESTAMP_FORMATTER)
                    else
                        null

                counters.add(
                    IntegerCounter(key, (value as Int?)!!, updateTimestamp)
                )
            }
            counters.sortWith { x: IntegerCounter, y: IntegerCounter -> x.name.compareTo(y.name) }
            return counters
        } catch (e: CounterException) {
            throw RuntimeException(e)
        }
    }

    @Throws(MissingCounterException::class)
    override fun read(counterIdentifier: Any): IntegerCounter {
        val name = counterIdentifier.toString()
        val counters = readAll(false)

        for (c in counters) {
            if (c.name == name) {
                return c
            }
        }

        throw MissingCounterException(String.format("Unable find counter: %s", name))
    }

    override fun first(): IntegerCounter {
        return readAll(true)[0]
    }

    /**
     * Saves provided counter in storage. If it's identifier is already defined, existing counter will
     * be overwritten.
     */
    @SuppressLint("ApplySharedPref")
    override fun write(counter: IntegerCounter) {
        values.edit().putInt(counter.name, counter.value).commit()

        if (counter.lastUpdatedDate != null) {
            updateTimestamps
                .edit()
                .putString(counter.name, counter.lastUpdatedDate!!.toString(TIMESTAMP_FORMATTER))
                .commit()
        }

        broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE)
    }

    @SuppressLint("ApplySharedPref")
    override fun overwriteAll(counters: List<IntegerCounter>) {
        Log.i(TAG, String.format("Writing %s counters to storage", counters.size))

        val prefsEditor = values.edit()
        prefsEditor.clear()
        for (c in counters) {
            prefsEditor.putInt(c.name, c.value)
        }
        val success = prefsEditor.commit()

        if (success) {
            Log.i(TAG, "Writing has been completed")
        } else {
            Log.e(TAG, "Failed to overwrite counters to storage")
        }

        broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE)
    }

    @SuppressLint("ApplySharedPref")
    override fun delete(counterIdentifier: Any) {
        val prefsEditor = values.edit()
        prefsEditor.remove(counterIdentifier.toString())
        prefsEditor.commit()

        broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE)
    }

    @SuppressLint("ApplySharedPref")
    override fun wipe() {
        val prefsEditor = values.edit()
        prefsEditor.clear()
        prefsEditor.commit()

        broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE)
    }

    @Throws(IOException::class)
    override fun toCsv(): String {
        val output = StringBuilder()
        CSVPrinter(output, CSVFormat.POSTGRESQL_CSV).use { csvPrinter ->
            csvPrinter.printRecord("Name", "Value", "Last Update")
            for (c in readAll(false)) {
                csvPrinter.printRecord(
                    c.name,
                    c.value,
                    if (c.lastUpdatedDate != null)
                        c.lastUpdatedDate!!.toString(TIMESTAMP_FORMATTER)
                    else
                        ""
                )
            }
            return output.toString()
        }
    }

    companion object {
        private val TAG: String = SharedPrefsCounterStorage::class.java.simpleName

        private const val VALUES_FILE_NAME = "counters"
        private const val UPDATE_TIMESTAMPS_FILE_NAME = "update-timestamps"
        private val TIMESTAMP_FORMATTER: DateTimeFormatter =
            ISODateTimeFormat.basicDateTimeNoMillis().withZone(DateTimeZone.getDefault())
    }
}
