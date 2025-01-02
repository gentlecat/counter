package me.tsukanov.counter.repository.impl

import android.content.Context
import android.content.SharedPreferences
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.domain.exception.CounterException
import me.tsukanov.counter.infrastructure.Actions
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.repository.SharedPrefsCounterStorage
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.Collections
import java.util.HashMap

@RunWith(MockitoJUnitRunner::class)
class SharedPrefsCounterStorageTest {

    companion object {
        private const val DEFAULT_COUNTER_NAME = "Test counter"
    }

    @Mock
    private val context: Context? = null

    @Mock
    private val countersSharedPrefs: SharedPreferences? = null

    @Mock
    private val timestampSharedPrefs: SharedPreferences? = null

    @Mock
    private val countersPrefsEditor: SharedPreferences.Editor? = null

    @Mock
    private val timestampPrefsEditor: SharedPreferences.Editor? = null

    @Mock
    private val broadcastHelper: BroadcastHelper? = null

    private var systemUnderTest: SharedPrefsCounterStorage? = null

    @Before
    fun setUp() {
        Mockito.`when`(
            context!!.getSharedPreferences(
                ArgumentMatchers.eq("counters"),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(countersSharedPrefs)
        Mockito.`when`(countersSharedPrefs!!.edit()).thenReturn(countersPrefsEditor)
        Mockito.`when`(
            countersSharedPrefs.edit()
                .putInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())
        ).thenReturn(countersPrefsEditor)

        Mockito.`when`(
            context.getSharedPreferences(
                ArgumentMatchers.eq("update-timestamps"),
                ArgumentMatchers.anyInt()
            )
        )
            .thenReturn(timestampSharedPrefs)

        systemUnderTest = SharedPrefsCounterStorage(
            context,
            broadcastHelper!!, DEFAULT_COUNTER_NAME
        )
    }

    @Test
    fun read_withoutDefaultCounter() {
        Mockito.`when`(countersSharedPrefs!!.all).thenReturn(Collections.emptyMap())

        val output = systemUnderTest!!.readAll(false)
        Assert.assertEquals(0, output.size.toLong())

        Mockito.verify(countersSharedPrefs).all
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }

    @Test
    fun read_withDefaultCounter() {
        Mockito.`when`(countersSharedPrefs!!.all).thenReturn(Collections.emptyMap())

        val output = systemUnderTest!!.readAll(true)
        Assert.assertEquals(1, output.size.toLong())

        Mockito.verify(countersSharedPrefs).all
        Mockito.verify(countersPrefsEditor)!!.putInt(DEFAULT_COUNTER_NAME, 0)
        Mockito.verify(countersPrefsEditor)!!.commit()
        Mockito.verify(broadcastHelper)!!.sendBroadcast(Actions.COUNTER_SET_CHANGE)
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }

    @Test
    @Throws(CounterException::class)
    fun write() {
        systemUnderTest!!.overwriteAll(
            ImmutableList.of(
                IntegerCounter("First counter", 0),
                IntegerCounter("Second counter", 1),
                IntegerCounter("Third counter", -1)
            )
        )

        Mockito.verify(countersPrefsEditor)!!.clear()
        Mockito.verify(countersPrefsEditor)!!.putInt("First counter", 0)
        Mockito.verify(countersPrefsEditor)!!.putInt("Second counter", 1)
        Mockito.verify(countersPrefsEditor)!!.putInt("Third counter", -1)
        Mockito.verify(countersPrefsEditor)!!.commit()
        Mockito.verify(broadcastHelper)!!.sendBroadcast(Actions.COUNTER_SET_CHANGE)
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }

    @Test
    fun overwrite_withNoCounters() {
        systemUnderTest!!.overwriteAll(emptyList())

        Mockito.verify(countersPrefsEditor)!!.clear()
        Mockito.verify(countersPrefsEditor)!!.commit()
        Mockito.verify(broadcastHelper)!!.sendBroadcast(Actions.COUNTER_SET_CHANGE)
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }

    @Test
    fun wipe() {
        systemUnderTest!!.wipe()

        Mockito.verify(countersPrefsEditor)!!.clear()
        Mockito.verify(countersPrefsEditor)!!.commit()
        Mockito.verify(broadcastHelper)!!.sendBroadcast(Actions.COUNTER_SET_CHANGE)
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }

    @Test
    @Throws(Exception::class)
    fun toCsv() {
        val testData: Map<*, *> = ImmutableMap.of("first", 0, "second, ok", -1)
        Mockito.`when`(countersSharedPrefs!!.all).thenReturn(testData as MutableMap<String, *>?)

        val output = systemUnderTest!!.toCsv()

        Assert.assertEquals(
            """
            "Name","Value","Last Update"
            "first","0",""
            "second, ok","-1",""
            """.trimIndent() + "\n",
            output
        )

        Mockito.verify(countersSharedPrefs).all
        Mockito.verify(context)!!.getSharedPreferences("counters", Context.MODE_PRIVATE)
        Mockito.verify(context)!!.getSharedPreferences("update-timestamps", Context.MODE_PRIVATE)
    }
}
