package me.tsukanov.counter.repository.exceptions;

import me.tsukanov.counter.domain.exception.CounterException;

public class MissingCounterException extends CounterException {

  public MissingCounterException(String message) {
    super(message);
  }
}
