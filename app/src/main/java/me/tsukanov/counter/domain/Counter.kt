package me.tsukanov.counter.domain

import me.tsukanov.counter.domain.exception.InvalidNameException
import me.tsukanov.counter.domain.exception.InvalidValueException
import org.joda.time.DateTime

interface Counter<T> {

    @set:Throws(InvalidNameException::class)
    var name: String

    @set:Throws(InvalidValueException::class)
    var value: T

    val lastUpdatedDate: DateTime?

    fun increment()

    fun decrement()

    /** Resets counter to its default value. Default value depends on the type [T].  */
    fun reset()
}
