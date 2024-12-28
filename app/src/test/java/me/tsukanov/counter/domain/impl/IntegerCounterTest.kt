package me.tsukanov.counter.domain.impl

import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.domain.exception.CounterException
import org.junit.Assert
import org.junit.Test

class IntegerCounterTest {

    companion object {
        private const val DEFAULT_NAME = "Test name"
    }

    @Test
    @Throws(CounterException::class)
    fun nameSetting_worksAsExpected() {
        val counter = IntegerCounter(DEFAULT_NAME)
        Assert.assertEquals(DEFAULT_NAME, counter.name)

        val newName = "New name"
        counter.name = newName
        Assert.assertEquals(newName, counter.name)
    }

    @Test
    @Throws(Exception::class)
    fun valueSetting() {
        val counter = IntegerCounter(DEFAULT_NAME, 0)
        Assert.assertEquals(0, counter.value.toLong())

        counter.value = 42
        Assert.assertEquals(42, counter.value.toLong())

        counter.value = IntegerCounter.MAX_VALUE
        Assert.assertEquals(999999999, counter.value.toLong())

        counter.value = IntegerCounter.MIN_VALUE
        Assert.assertEquals(-99999999, counter.value.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun increment() {
        val counter = IntegerCounter(DEFAULT_NAME, 0)
        Assert.assertEquals(0, counter.value.toLong())

        counter.increment()
        Assert.assertEquals(1, counter.value.toLong())

        counter.increment()
        counter.increment()
        Assert.assertEquals(3, counter.value.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun decrement() {
        val counter = IntegerCounter(DEFAULT_NAME, 0)
        Assert.assertEquals(0, counter.value.toLong())

        counter.decrement()
        Assert.assertEquals(-1, counter.value.toLong())

        counter.decrement()
        counter.decrement()
        Assert.assertEquals(-3, counter.value.toLong())
    }

}
