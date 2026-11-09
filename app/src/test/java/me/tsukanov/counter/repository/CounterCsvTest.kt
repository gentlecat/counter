package me.tsukanov.counter.repository

import me.tsukanov.counter.domain.IntegerCounter
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

class CounterCsvTest {
    private fun summary(counters: List<IntegerCounter>) =
        counters.map { Triple(it.name, it.value, it.lastUpdatedDate?.millis) }

    @Test
    fun readsWhatWriteProduces() {
        val counters =
            listOf(
                IntegerCounter("Laps", 42, DateTime(2026, 11, 8, 14, 28, 43)),
                IntegerCounter("Push-ups, morning", -3, null),
                IntegerCounter("He said \"hi\"", IntegerCounter.MAX_VALUE, DateTime(2020, 1, 2, 3, 4, 5)),
            )

        val parsed = CounterCsv.read(CounterCsv.write(counters))

        Assert.assertEquals(summary(counters), summary(parsed.counters))
        Assert.assertEquals(0, parsed.invalidRows)
    }

    @Test
    fun acceptsFilesWithoutHeaderQuotesOrTimestamps() {
        val parsed = CounterCsv.read("First,1\r\n\r\nSecond, 2 ,\n")

        Assert.assertEquals(listOf("First" to 1, "Second" to 2), parsed.counters.map { it.name to it.value })
        Assert.assertNull(parsed.counters[0].lastUpdatedDate)
        Assert.assertEquals(0, parsed.invalidRows)
    }

    @Test
    fun ignoresByteOrderMark() {
        val parsed = CounterCsv.read(Char(0xFEFF) + "A,5\n")

        Assert.assertEquals(listOf("A" to 5), parsed.counters.map { it.name to it.value })
        Assert.assertEquals(0, parsed.invalidRows)
    }

    @Test
    fun countsInvalidRowsAndKeepsTheRest() {
        val csv =
            """
            "Name","Value","Last Update"
            "Good","1",""
            "","2",""
            "Not a number","x",""
            "Too big","${IntegerCounter.MAX_VALUE.toLong() + 1}",""
            "Only a name"
            "Bad timestamp","3","yesterday"
            """.trimIndent()

        val parsed = CounterCsv.read(csv)

        Assert.assertEquals(listOf("Good" to 1, "Bad timestamp" to 3), parsed.counters.map { it.name to it.value })
        Assert.assertNull(parsed.counters[1].lastUpdatedDate)
        Assert.assertEquals(4, parsed.invalidRows)
    }

    @Test
    fun readsIsoTimestamps() {
        val parsed = CounterCsv.read("A,1,2026-11-08T14:28:43Z\n")

        Assert.assertEquals(DateTime.parse("2026-11-08T14:28:43Z").millis, parsed.counters[0].lastUpdatedDate!!.millis)
    }

    @Test
    fun lastRowWinsForRepeatedNames() {
        val parsed = CounterCsv.read("A,1\nA,2\n")

        Assert.assertEquals(listOf("A" to 2), parsed.counters.map { it.name to it.value })
    }

    @Test
    fun emptyFileHasNoCounters() {
        val parsed = CounterCsv.read("")

        Assert.assertTrue(parsed.counters.isEmpty())
        Assert.assertEquals(0, parsed.invalidRows)
    }

    @Test
    fun planSeparatesClashingNames() {
        val imported = listOf(IntegerCounter("A", 1), IntegerCounter("B", 2), IntegerCounter("C", 3))

        val plan = CounterCsv.plan(imported, setOf("B", "Z"))

        Assert.assertEquals(listOf("A", "C"), plan.new.map { it.name })
        Assert.assertEquals(listOf("B"), plan.clashing.map { it.name })
    }
}
