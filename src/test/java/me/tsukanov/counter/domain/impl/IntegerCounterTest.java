package me.tsukanov.counter.domain.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.tsukanov.counter.domain.Counter;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import org.junit.jupiter.api.Test;

class IntegerCounterTest {
  private static final String DEFAULT_NAME = "Test name";

  @Test
  void nameSetting_worksAsExpected() throws CounterException {
    final Counter counter = new IntegerCounter(DEFAULT_NAME);
    assertEquals(DEFAULT_NAME, counter.getName());

    final String newName = "New name";
    counter.setName(newName);
    assertEquals(newName, counter.getName());
  }

  @Test
  void valueSetting() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue());

    counter.setValue(42);
    assertEquals(42, counter.getValue());

    counter.setValue(IntegerCounter.MAX_VALUE);
    assertEquals(999999999, counter.getValue());

    counter.setValue(IntegerCounter.MIN_VALUE);
    assertEquals(-99999999, counter.getValue());
  }

  @Test
  void increment() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue());

    counter.increment();
    assertEquals(1, counter.getValue());

    counter.increment();
    counter.increment();
    assertEquals(3, counter.getValue());
  }

  @Test
  void decrement() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue());

    counter.decrement();
    assertEquals(-1, counter.getValue());

    counter.decrement();
    counter.decrement();
    assertEquals(-3, counter.getValue());
  }
}
