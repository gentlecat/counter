package me.tsukanov.counter.domain

import android.util.Log
import me.tsukanov.counter.domain.exception.InvalidNameException
import me.tsukanov.counter.domain.exception.InvalidValueException
import org.joda.time.DateTime

/**
 * Variation of the [Counter] that uses [Integer] type as a value. Names (identifiers)
 * of counters are always [String].
 */
class IntegerCounter : Counter<Int> {

    override var name: String = ""
        @Throws(InvalidNameException::class)
        set(newName) {
            if (!isValidName(newName)) {
                throw InvalidNameException("Provided name is invalid")
            }
            field = newName
        }

    override var value: Int = DEFAULT_VALUE
        @Throws(InvalidValueException::class)
        set(newValue) {
            if (!isValidValue(newValue)) {
                throw InvalidValueException(
                    String.format(
                        "Desired value (%s) is outside of allowed range: %s to %s",
                        newValue, MIN_VALUE, MAX_VALUE
                    )
                )
            }
            field = newValue
            lastUpdatedDate = DateTime()
        }

    override var lastUpdatedDate: DateTime?
        private set

    constructor(name: String) {
        if (!isValidName(name)) {
            throw InvalidNameException("Provided name is invalid")
        }
        this.name = name
        this.lastUpdatedDate = DateTime()
    }

    constructor(name: String, value: Int) {
        if (!isValidName(name)) {
            throw InvalidNameException("Provided name is invalid")
        }
        this.name = name

        if (!isValidValue(value)) {
            throw InvalidValueException(
                String.format(
                    "Desired value (%s) is outside of allowed range: %s to %s",
                    value, MIN_VALUE, MAX_VALUE
                )
            )
        }
        this.value = value

        this.lastUpdatedDate = null
    }

    constructor(name: String, value: Int, lastUpdate: DateTime?) {
        if (!isValidName(name)) {
            throw InvalidNameException("Provided name is invalid")
        }
        this.name = name

        if (!isValidValue(value)) {
            throw InvalidValueException(
                String.format(
                    "Desired value (%s) is outside of allowed range: %s to %s",
                    value, MIN_VALUE, MAX_VALUE
                )
            )
        }
        this.value = value

        this.lastUpdatedDate = lastUpdate
    }

    override fun increment() {
        try {
            this.value += 1
        } catch (e: InvalidValueException) {
            Log.e(TAG, "Unable to increment the counter", e)
        }
    }

    override fun decrement() {
        try {
            this.value -= 1
        } catch (e: InvalidValueException) {
            Log.e(TAG, "Unable to decrement the counter", e)
        }
    }

    override fun reset() {
        try {
            value = DEFAULT_VALUE
        } catch (e: Exception) {
            // This should never happen, but we should still handle the exception...
            throw RuntimeException(e)
        }
    }

    companion object {
        private val TAG: String = IntegerCounter::class.java.simpleName

        const val MAX_VALUE: Int = 1000000000 - 1
        const val MIN_VALUE: Int = -100000000 + 1
        private const val DEFAULT_VALUE = 0

        @JvmStatic
        val valueCharLimit: Int
            /**
             * Provides max number of characters that would fit within the [IntegerCounter] value
             * limits.
             */
            get() = MAX_VALUE.toString().length - 1

        private fun isValidName(name: String): Boolean {
            return name.isNotEmpty()
        }

        private fun isValidValue(value: Int): Boolean {
            return value in MIN_VALUE..MAX_VALUE
        }
    }
}
