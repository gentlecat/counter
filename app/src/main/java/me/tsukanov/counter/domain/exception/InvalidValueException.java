package me.tsukanov.counter.domain.exception;

public class InvalidValueException extends CounterException {

  public InvalidValueException(String message) {
    super(message);
  }
}
