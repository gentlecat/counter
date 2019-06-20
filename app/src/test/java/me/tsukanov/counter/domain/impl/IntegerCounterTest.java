package me.tsukanov.counter.domain.impl;

import static org.junit.Assert.assertEquals;

import me.tsukanov.counter.domain.Counter;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import org.junit.Test;

public class IntegerCounterTest {

  private static final String DEFAULT_NAME = "Test name";

  @Test
  public void nameSetting_worksAsExpected() throws CounterException {
    final Counter counter = new IntegerCounter(DEFAULT_NAME);
    assertEquals(DEFAULT_NAME, counter.getName());

    final String newName = "New name";
    counter.setName(newName);
    assertEquals(newName, counter.getName());
  }

  @Test
  public void valueSetting() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue().intValue());

    counter.setValue(42);
    assertEquals(42, counter.getValue().intValue());

    counter.setValue(IntegerCounter.MAX_VALUE);
    assertEquals(999999999, counter.getValue().intValue());

    counter.setValue(IntegerCounter.MIN_VALUE);
    assertEquals(-99999999, counter.getValue().intValue());
  }

  @Test
  public void increment() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue().intValue());

    counter.increment();
    assertEquals(1, counter.getValue().intValue());

    counter.increment();
    counter.increment();
    assertEquals(3, counter.getValue().intValue());
  }

  @Test
  public void decrement() throws Exception {
    final IntegerCounter counter = new IntegerCounter(DEFAULT_NAME, 0);
    assertEquals(0, counter.getValue().intValue());

    counter.decrement();
    assertEquals(-1, counter.getValue().intValue());

    counter.decrement();
    counter.decrement();
    assertEquals(-3, counter.getValue().intValue());
  }
}
