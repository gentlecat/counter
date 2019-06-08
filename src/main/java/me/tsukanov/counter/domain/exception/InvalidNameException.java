package me.tsukanov.counter.domain.exception;

public class InvalidNameException extends CounterException {

  public InvalidNameException(String message) {
    super(message);
  }
}
