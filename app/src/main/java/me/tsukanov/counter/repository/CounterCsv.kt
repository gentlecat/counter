package me.tsukanov.counter.repository

import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.domain.exception.CounterException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import java.io.StringReader

/** Counters read from a CSV file, and how many rows couldn't be turned into counters. */
data class ParsedCsv(
    val counters: List<IntegerCounter>,
    val invalidRows: Int,
)

/** Imported counters split into ones that don't exist yet and ones whose names are already taken. */
data class ImportPlan(
    val new: List<IntegerCounter>,
    val clashing: List<IntegerCounter>,
)

/** The CSV format counters are exported in and imported from. */
object CounterCsv {
    private val TIMESTAMP_FORMATTER: DateTimeFormatter =
        ISODateTimeFormat.basicDateTimeNoMillis().withZone(DateTimeZone.getDefault())

    // Spreadsheet apps often start a saved CSV file with one
    private val BYTE_ORDER_MARK = Char(0xFEFF).toString()

    fun write(counters: List<IntegerCounter>): String {
        val output = StringBuilder()
        CSVPrinter(output, CSVFormat.POSTGRESQL_CSV).use { csvPrinter ->
            csvPrinter.printRecord("Name", "Value", "Last Update")
            for (c in counters) {
                csvPrinter.printRecord(c.name, c.value, c.lastUpdatedDate?.toString(TIMESTAMP_FORMATTER) ?: "")
            }
        }
        return output.toString()
    }

    /**
     * Reads counters written by [write], or by a spreadsheet editing such a file. A first row without a
     * number in the value column is taken as the header. If a name appears twice, the last row wins.
     */
    fun read(csv: String): ParsedCsv {
        val format =
            CSVFormat.POSTGRESQL_CSV
                .builder()
                .setIgnoreEmptyLines(true)
                .setNullString(null)
                .get()
        val counters = LinkedHashMap<String, IntegerCounter>()
        var invalidRows = 0
        format.parse(StringReader(csv.removePrefix(BYTE_ORDER_MARK))).use { parser ->
            for (record in parser) {
                val counter = toCounter(record)
                when {
                    counter != null -> counters[counter.name] = counter
                    record.recordNumber == 1L -> continue
                    else -> invalidRows++
                }
            }
        }
        return ParsedCsv(counters.values.toList(), invalidRows)
    }

    fun plan(imported: List<IntegerCounter>, existingNames: Set<String>): ImportPlan {
        val (clashing, new) = imported.partition { it.name in existingNames }
        return ImportPlan(new, clashing)
    }

    private fun toCounter(record: CSVRecord): IntegerCounter? {
        if (record.size() < 2) {
            return null
        }
        val value = field(record, 1).trim().toIntOrNull() ?: return null
        val lastUpdate = parseTimestamp(field(record, 2).trim())
        return try {
            IntegerCounter(field(record, 0), value, lastUpdate)
        } catch (e: CounterException) {
            null
        }
    }

    private fun field(record: CSVRecord, index: Int): String =
        if (index < record.size()) record.get(index) ?: "" else ""

    private fun parseTimestamp(text: String): DateTime? {
        if (text.isEmpty()) {
            return null
        }
        for (parse in listOf({ TIMESTAMP_FORMATTER.parseDateTime(text) }, { DateTime.parse(text) })) {
            try {
                return parse()
            } catch (e: IllegalArgumentException) {
                // Try the next format
            }
        }
        return null
    }
}
