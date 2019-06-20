package me.tsukanov.counter.repository.impl;

import static org.junit.Assert.assertEquals;
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
import me.tsukanov.counter.infrastructure.Actions;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.SharedPrefsCounterStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SharedPrefsCounterStorageTest {

  private static final String DEFAULT_COUNTER_NAME = "Test counter";

  @Mock private Context context;
  @Mock private SharedPreferences sharedPreferences;
  @Mock private SharedPreferences.Editor prefsEditor;
  @Mock private BroadcastHelper broadcastHelper;

  private SharedPrefsCounterStorage systemUnderTest;

  @Before
  public void setUp() {
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

    systemUnderTest = new SharedPrefsCounterStorage(context, broadcastHelper, DEFAULT_COUNTER_NAME);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(context, sharedPreferences, prefsEditor, broadcastHelper);
  }

  @Test
  public void read_withoutDefaultCounter() {
    when(sharedPreferences.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(false);
    assertEquals(0, output.size());

    verify(sharedPreferences).getAll();
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
  }

  @Test
  public void read_withDefaultCounter() {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);
    when(sharedPreferences.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(true);
    assertEquals(1, output.size());

    verify(sharedPreferences).getAll();
    verify(sharedPreferences).edit();
    verify(prefsEditor).putInt(DEFAULT_COUNTER_NAME, 0);
    verify(prefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
  }

  @Test
  public void write() throws CounterException {
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
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
  }

  @Test
  public void overwrite_withNoCounters() {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);

    systemUnderTest.overwriteAll(Collections.emptyList());

    verify(sharedPreferences).edit();
    verify(prefsEditor).clear();
    verify(prefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
  }

  @Test
  public void wipe() {
    when(sharedPreferences.edit()).thenReturn(prefsEditor);

    systemUnderTest.wipe();

    verify(sharedPreferences).edit();
    verify(prefsEditor).clear();
    verify(prefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
  }
}
