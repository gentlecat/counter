package me.tsukanov.counter.infrastructure

import me.tsukanov.counter.CounterApplication

enum class Actions {
    COUNTER_SET_CHANGE,
    SELECT_COUNTER;

    val actionName: String
        get() = String.format(
            "%s.%s",
            CounterApplication.PACKAGE_NAME,
            this
        )
}
