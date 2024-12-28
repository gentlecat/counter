package me.tsukanov.counter.repository.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.infrastructure.Actions;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.SharedPrefsCounterStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SharedPrefsCounterStorageTest {

  private static final String DEFAULT_COUNTER_NAME = "Test counter";

  @Mock private Context context;
  @Mock private SharedPreferences countersSharedPrefs;
  @Mock private SharedPreferences timestampSharedPrefs;
  @Mock private SharedPreferences.Editor countersPrefsEditor;
  @Mock private SharedPreferences.Editor timestampPrefsEditor;
  @Mock private BroadcastHelper broadcastHelper;

  private SharedPrefsCounterStorage systemUnderTest;

  @Before
  public void setUp() {
    when(context.getSharedPreferences(eq("counters"), anyInt())).thenReturn(countersSharedPrefs);
    when(countersSharedPrefs.edit()).thenReturn(countersPrefsEditor);
    when(countersSharedPrefs.edit().putInt(anyString(), anyInt())).thenReturn(countersPrefsEditor);

    when(context.getSharedPreferences(eq("update-timestamps"), anyInt()))
        .thenReturn(timestampSharedPrefs);
    when(timestampSharedPrefs.edit()).thenReturn(timestampPrefsEditor);
    when(timestampSharedPrefs.edit().putString(anyString(), anyString()))
        .thenReturn(timestampPrefsEditor);

    systemUnderTest = new SharedPrefsCounterStorage(context, broadcastHelper, DEFAULT_COUNTER_NAME);
  }

  @Test
  public void read_withoutDefaultCounter() {
    when(countersSharedPrefs.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(false);
    assertEquals(0, output.size());

    verify(countersSharedPrefs).getAll();
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }

  @Test
  public void read_withDefaultCounter() {
    when(countersSharedPrefs.getAll()).thenReturn(Collections.EMPTY_MAP);

    final List<IntegerCounter> output = systemUnderTest.readAll(true);
    assertEquals(1, output.size());

    verify(countersSharedPrefs).getAll();
    verify(countersPrefsEditor).putInt(DEFAULT_COUNTER_NAME, 0);
    verify(countersPrefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }

  @Test
  public void write() throws CounterException {
    systemUnderTest.overwriteAll(
        ImmutableList.of(
            new IntegerCounter("First counter", 0),
            new IntegerCounter("Second counter", 1),
            new IntegerCounter("Third counter", -1)));

    verify(countersPrefsEditor).clear();
    verify(countersPrefsEditor).putInt("First counter", 0);
    verify(countersPrefsEditor).putInt("Second counter", 1);
    verify(countersPrefsEditor).putInt("Third counter", -1);
    verify(countersPrefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }

  @Test
  public void overwrite_withNoCounters() {
    systemUnderTest.overwriteAll(Collections.emptyList());

    verify(countersPrefsEditor).clear();
    verify(countersPrefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }

  @Test
  public void wipe() {
    systemUnderTest.wipe();

    verify(countersPrefsEditor).clear();
    verify(countersPrefsEditor).commit();
    verify(broadcastHelper).sendBroadcast(Actions.COUNTER_SET_CHANGE);
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }

  @Test
  public void toCsv() throws Exception {
    final Map testData = ImmutableMap.of("first", 0, "second, ok", -1);
    when(countersSharedPrefs.getAll()).thenReturn(testData);

    final String output = systemUnderTest.toCsv();
    assertEquals(
        """
        Name,Value,Last Update\r
        first,0,\r
        "second, ok",-1,\r
        """,
        output);

    verify(countersSharedPrefs).getAll();
    verify(context).getSharedPreferences("counters", Context.MODE_PRIVATE);
    verify(context).getSharedPreferences("update-timestamps", Context.MODE_PRIVATE);
  }
}
