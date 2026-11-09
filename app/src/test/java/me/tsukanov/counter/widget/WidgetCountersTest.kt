package me.tsukanov.counter.widget

import org.junit.Assert
import org.junit.Test

class WidgetCountersTest {
    @Test
    fun renamedPointsMatchingWidgetsAtTheNewName() {
        val mapping = mapOf("widget_1" to "Laps", "widget_2" to "Push-ups", "widget_3" to "Laps")

        Assert.assertEquals(
            mapOf("widget_1" to "Pool laps", "widget_3" to "Pool laps"),
            WidgetCounters.renamed(mapping, "Laps", "Pool laps"),
        )
    }

    @Test
    fun renamedLeavesOtherWidgetsAlone() {
        val mapping = mapOf("widget_1" to "Push-ups")

        Assert.assertEquals(emptyMap<String, String>(), WidgetCounters.renamed(mapping, "Laps", "Pool laps"))
    }
}
