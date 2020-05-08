package me.tsukanov.counter.repository;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.List;
import me.tsukanov.counter.repository.exceptions.MissingCounterException;

public interface CounterStorage<COUNTER_TYPE> {

  /**
   * Retrieves all stored counters.
   *
   * @param addDefault Whether default counter needs to be added if no counters are stored.
   */
  @NonNull
  List<COUNTER_TYPE> readAll(boolean addDefault);

  /** Retrieves counter with a specified identifier. */
  @NonNull
  COUNTER_TYPE read(@NonNull Object counterIdentifier) throws MissingCounterException;

  /** Retrieves first encountered counter. If one doesn't exists, creates a default one. */
  @NonNull
  COUNTER_TYPE getFirst();

  /**
   * Saves provided counter in storage. If it's identifier is already defined, existing counter will
   * be overwritten.
   */
  void write(@NonNull COUNTER_TYPE counter);

  /**
   * Writes provided counters into storage.
   *
   * <p><b>This will overwrite all existing counters.</b>
   */
  void overwriteAll(@NonNull List<COUNTER_TYPE> counters);

  /** Deletes counter with a specified identifier. */
  void delete(@NonNull Object counterIdentifier);

  /** Removes all stored counters. */
  void wipe();

  /** Exports all counters into CSV formatted string. */
  @NonNull
  String toCSV() throws IOException;
}
