package me.tsukanov.counter.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.repository.SharedPrefsCounterStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SharedPrefsCounterStorageTest {

  private static final String DEFAULT_COUNTER_NAME = "Test counter";

  @Mock private Context context;
  @Mock private SharedPreferences sharedPreferences;
  @Mock private SharedPreferences.Editor prefsEditor;

  private SharedPrefsCounterStorage systemUnderTest;

  @BeforeEach
  void setUp() {
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

    systemUnderTest = new SharedPrefsCounterStorage(context, DEFAULT_COUNTER_NAME);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(context, sharedPreferences, prefsEditor);
  }

  @Test
  void read_withoutDefaultCounter() {
    when(sharedPreferences.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(false);
    assertEquals(0, output.size());

    verify(sharedPreferences).getAll();
  }

  @Test
  void read_withDefaultCounter() {
    when(sharedPreferences.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(true);
    assertEquals(1, output.size());

    verify(sharedPreferences).getAll();
  }

  @Test
  void write() throws CounterException {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);

    systemUnderTest.overwriteAll(
        ImmutableList.of(
            new IntegerCounter("First counter", 0),
            new IntegerCounter("Second counter", 1),
            new IntegerCounter("Third counter", -1)));

    verify(sharedPreferences).edit();
    verify(prefsEditor).clear();
    verify(prefsEditor).putInt("First counter", 0);
    verify(prefsEditor).putInt("Second counter", 1);
    verify(prefsEditor).putInt("Third counter", -1);
    verify(prefsEditor).commit();
  }

  @Test
  void overwrite_withNoCounters() {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);

    systemUnderTest.overwriteAll(Collections.emptyList());

    verify(sharedPreferences).edit();
    verify(prefsEditor).clear();
    verify(prefsEditor).commit();
  }

  @Test
  void wipe() {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);

    systemUnderTest.wipe();

    verify(sharedPreferences).edit();
    verify(prefsEditor).clear();
    verify(prefsEditor).commit();
  }
}
