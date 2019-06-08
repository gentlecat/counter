package me.tsukanov.counter.domain;

import androidx.annotation.NonNull;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.domain.exception.InvalidNameException;
import me.tsukanov.counter.domain.exception.InvalidValueException;

/**
 * Variation of the {@link Counter} that uses {@link Integer} type as a value. Names (identifiers)
 * of counters are always {@link String}.
 */
public class IntegerCounter implements Counter<Integer> {

  public static final int MAX_VALUE = 1000000000 - 1;
  public static final int MIN_VALUE = -100000000 + 1;
  private static final int DEFAULT_VALUE = 0;

  private String name;
  private Integer value = DEFAULT_VALUE;

  public IntegerCounter(@NonNull final String name) throws CounterException {
    if (!isValidName(name)) {
      throw new InvalidNameException("Provided name is invalid");
    }
    this.name = name;
  }

  public IntegerCounter(@NonNull final String name, @NonNull final Integer value)
      throws CounterException {
    if (!isValidName(name)) {
      throw new InvalidNameException("Provided name is invalid");
    }
    this.name = name;

    if (!isValidValue(value)) {
      throw new InvalidValueException(
          String.format(
              "Desired value (%s) is outside of allowed range: %s to %s",
              value, MIN_VALUE, MAX_VALUE));
    }
    this.value = value;
  }

  @NonNull
  public String getName() {
    return this.name;
  }

  public void setName(@NonNull String newName) throws InvalidNameException {
    if (!isValidName(newName)) {
      throw new InvalidNameException("Provided name is invalid");
    }
    this.name = newName;
  }

  @NonNull
  public Integer getValue() {
    return this.value;
  }

  public void setValue(@NonNull final Integer newValue) throws InvalidValueException {
    if (!isValidValue(newValue)) {
      throw new InvalidValueException(
          String.format(
              "Desired value (%s) is outside of allowed range: %s to %s",
              newValue, MIN_VALUE, MAX_VALUE));
    }
    this.value = newValue;
  }

  public void increment() throws InvalidValueException {
    setValue(this.value + 1);
  }

  public void decrement() throws InvalidValueException {
    setValue(this.value - 1);
  }

  public void reset() {
    try {
      setValue(DEFAULT_VALUE);
    } catch (Exception e) {
      // This should never happen, but we should still handle the exception...
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private static boolean isValidName(@NonNull final String name) {
    return name.length() > 0;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private static boolean isValidValue(@NonNull final Integer value) {
    return MIN_VALUE <= value && value <= MAX_VALUE;
  }
}
