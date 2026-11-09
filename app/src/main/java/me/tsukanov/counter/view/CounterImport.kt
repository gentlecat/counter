package me.tsukanov.counter.view

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.repository.CounterCsv
import me.tsukanov.counter.repository.ImportPlan
import org.joda.time.DateTime
import java.io.IOException

/**
 * Adds counters from a CSV file, asking once whether counters that already exist should be replaced.
 * Counters that aren't in the file are left alone.
 */
class CounterImport(
    private val activity: Activity,
    private val onImported: () -> Unit = {},
) {
    fun start(uri: Uri) {
        // Reading can be slow when the file comes from a cloud provider
        Thread {
            val csv =
                try {
                    readText(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to read $uri", e)
                    null
                }
            activity.runOnUiThread {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    onRead(csv)
                }
            }
        }.start()
    }

    private fun readText(uri: Uri): String {
        val stream = activity.contentResolver.openInputStream(uri) ?: throw IOException("No content at $uri")
        stream.bufferedReader().use { reader ->
            val text = StringBuilder()
            val buffer = CharArray(8192)
            while (true) {
                val count = reader.read(buffer)
                if (count < 0) {
                    return text.toString()
                }
                text.appendRange(buffer, 0, count)
                if (text.length > MAX_FILE_CHARS) {
                    throw IOException("File is too large to be a counter export")
                }
            }
        }
    }

    private fun onRead(csv: String?) {
        if (csv == null) {
            Toast.makeText(activity, R.string.toast_unable_to_import, Toast.LENGTH_SHORT).show()
            return
        }

        val parsed = CounterCsv.read(csv)
        if (parsed.counters.isEmpty()) {
            showSummary(listOf(activity.getString(R.string.import_nothing_found)), parsed.invalidRows)
            return
        }

        val existingNames =
            CounterApplication.component!!
                .localStorage()!!
                .readAll(false)
                .mapNotNull { it?.name }
                .toSet()
        val plan = CounterCsv.plan(parsed.counters, existingNames)
        if (plan.clashing.isEmpty()) {
            apply(plan, replace = false, parsed.invalidRows)
            return
        }

        AlertDialog.Builder(activity)
            .setMessage(
                activity.resources.getQuantityString(
                    R.plurals.import_clash_message,
                    plan.clashing.size,
                    plan.clashing.size,
                ),
            ).setPositiveButton(R.string.import_clash_replace) { d: DialogInterface?, id: Int ->
                apply(plan, replace = true, parsed.invalidRows)
            }.setNegativeButton(R.string.import_clash_keep) { d: DialogInterface?, id: Int ->
                apply(plan, replace = false, parsed.invalidRows)
            }.setNeutralButton(R.string.dialog_button_cancel, null)
            .create()
            .show()
    }

    private fun apply(plan: ImportPlan, replace: Boolean, invalidRows: Int) {
        val storage = CounterApplication.component!!.localStorage()!!
        val now = DateTime()
        val written = if (replace) plan.new + plan.clashing else plan.new
        for (c in written) {
            // A counter without a timestamp in the file changes here and now
            storage.write(IntegerCounter(c.name, c.value, c.lastUpdatedDate ?: now))
        }
        onImported()

        val lines = mutableListOf(activity.getString(R.string.import_summary_added, plan.new.size))
        if (plan.clashing.isNotEmpty()) {
            val clashLine = if (replace) R.string.import_summary_replaced else R.string.import_summary_kept
            lines.add(activity.getString(clashLine, plan.clashing.size))
        }
        showSummary(lines, invalidRows)
    }

    private fun showSummary(lines: List<String>, invalidRows: Int) {
        val message =
            if (invalidRows > 0) {
                lines + activity.getString(R.string.import_summary_invalid, invalidRows)
            } else {
                lines
            }
        AlertDialog.Builder(activity)
            .setTitle(R.string.import_summary_title)
            .setMessage(message.joinToString("\n"))
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .show()
    }

    companion object {
        private val TAG: String = CounterImport::class.java.simpleName

        /** MIME types that providers use for CSV files. */
        val CSV_MIME_TYPES: Array<String> =
            arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "application/csv",
                "application/vnd.ms-excel",
                "text/plain",
            )

        private const val MAX_FILE_CHARS = 1_000_000
    }
}
