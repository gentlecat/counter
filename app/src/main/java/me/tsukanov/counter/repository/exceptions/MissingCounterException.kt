package me.tsukanov.counter.repository.exceptions

import me.tsukanov.counter.domain.exception.CounterException

class MissingCounterException(message: String?) : CounterException(message)
