package me.tsukanov.counter.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.tsukanov.counter.domain.Counter;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.infrastructure.Actions;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.exceptions.MissingCounterException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Counter storage that uses {@link SharedPreferences} as a medium.
 *
 * <p>This implementation us based on the {@link IntegerCounter} variation of the {@link Counter}.
 */
public class SharedPrefsCounterStorage implements CounterStorage<IntegerCounter> {

  private static final String TAG = SharedPrefsCounterStorage.class.getSimpleName();

  private static final String VALUES_FILE_NAME = "counters";
  private static final String UPDATE_TIMESTAMPS_FILE_NAME = "update-timestamps";
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      ISODateTimeFormat.basicDateTimeNoMillis().withZone(DateTimeZone.getDefault());

  private final SharedPreferences values;
  private final SharedPreferences updateTimestamps;

  private final BroadcastHelper broadcastHelper;
  private final String defaultCounterName;

  /**
   * Default constructor.
   *
   * @param defaultCounterName Name that will be assigned to a default counter.
   */
  public SharedPrefsCounterStorage(
      @NonNull final Context context,
      @NonNull final BroadcastHelper broadcastHelper,
      @NonNull final String defaultCounterName) {
    this.values = context.getSharedPreferences(VALUES_FILE_NAME, Context.MODE_PRIVATE);
    this.updateTimestamps =
        context.getSharedPreferences(UPDATE_TIMESTAMPS_FILE_NAME, Context.MODE_PRIVATE);
    this.broadcastHelper = broadcastHelper;
    this.defaultCounterName = defaultCounterName;
  }

  @Override
  @NonNull
  public List<IntegerCounter> readAll(boolean addDefault) {
    final List<IntegerCounter> counters = new LinkedList<>();

    final Map<String, ?> valuesMap = values.getAll();

    try {
      if (valuesMap.isEmpty() && addDefault) {
        final IntegerCounter defaultCounter = new IntegerCounter(this.defaultCounterName);
        counters.add(defaultCounter);
        write(defaultCounter);
        return counters;
      }

      for (Map.Entry<String, ?> valEntry : valuesMap.entrySet()) {
        final String updateTimestampStr = updateTimestamps.getString(valEntry.getKey(), null);
        final DateTime updateTimestamp =
            updateTimestampStr != null
                ? DateTime.parse(updateTimestampStr, TIMESTAMP_FORMATTER)
                : null;

        counters.add(
            new IntegerCounter(valEntry.getKey(), (Integer) valEntry.getValue(), updateTimestamp));
      }
      Collections.sort(counters, (x, y) -> x.getName().compareTo(y.getName()));
      return counters;

    } catch (CounterException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  @Override
  public IntegerCounter read(@NonNull final Object identifierObj) throws MissingCounterException {
    final String name = identifierObj.toString();
    final List<IntegerCounter> counters = readAll(false);

    for (IntegerCounter c : counters) {
      if (c.getName().equals(name)) {
        return c;
      }
    }

    throw new MissingCounterException(String.format("Unable find counter: %s", name));
  }

  @NonNull
  @Override
  public IntegerCounter getFirst() {
    return readAll(true).get(0);
  }

  /**
   * Saves provided counter in storage. If it's identifier is already defined, existing counter will
   * be overwritten.
   */
  @SuppressLint("ApplySharedPref")
  @Override
  public void write(@NonNull final IntegerCounter counter) {
    values.edit().putInt(counter.getName(), counter.getValue()).commit();

    if (counter.getLastUpdatedDate() != null) {
      updateTimestamps
          .edit()
          .putString(counter.getName(), counter.getLastUpdatedDate().toString(TIMESTAMP_FORMATTER))
          .commit();
    }

    broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE);
  }

  @SuppressLint("ApplySharedPref")
  @Override
  public void overwriteAll(@NonNull List<IntegerCounter> counters) {
    Log.i(TAG, String.format("Writing %s counters to storage", counters.size()));

    final SharedPreferences.Editor prefsEditor = values.edit();
    prefsEditor.clear();
    for (IntegerCounter c : counters) {
      prefsEditor.putInt(c.getName(), c.getValue());
    }
    boolean success = prefsEditor.commit();

    if (success) {
      Log.i(TAG, "Writing has been completed");
    } else {
      Log.e(TAG, "Failed to overwrite counters to storage");
    }

    broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE);
  }

  @SuppressLint("ApplySharedPref")
  @Override
  public void delete(@NonNull Object counterName) {
    final SharedPreferences.Editor prefsEditor = values.edit();
    prefsEditor.remove(counterName.toString());
    prefsEditor.commit();

    broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE);
  }

  @SuppressLint("ApplySharedPref")
  @Override
  public void wipe() {
    SharedPreferences.Editor prefsEditor = values.edit();
    prefsEditor.clear();
    prefsEditor.commit();

    broadcastHelper.sendBroadcast(Actions.COUNTER_SET_CHANGE);
  }

  @NonNull
  @Override
  public String toCsv() throws IOException {
    final StringBuilder output = new StringBuilder();
    try (CSVPrinter csvPrinter = new CSVPrinter(output, CSVFormat.DEFAULT)) {

      csvPrinter.printRecord("Name", "Value", "Last Update");

      for (final IntegerCounter c : readAll(false)) {
        csvPrinter.printRecord(
            c.getName(),
            c.getValue(),
            c.getLastUpdatedDate() != null
                ? c.getLastUpdatedDate().toString(TIMESTAMP_FORMATTER)
                : "");
      }

      return output.toString();
    }
  }
}
