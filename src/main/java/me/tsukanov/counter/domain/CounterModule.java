package me.tsukanov.counter.domain;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.CounterStorage;
import me.tsukanov.counter.repository.SharedPrefsCounterStorage;

@Module
public class CounterModule {

  private final CounterApplication app;

  public CounterModule(CounterApplication app) {
    this.app = app;
  }

  @Provides
  @Singleton
  CounterApplication provideApp() {
    return app;
  }

  @Provides
  @Singleton
  CounterStorage provideCounterStorage(final @NonNull CounterApplication app) {
    return new SharedPrefsCounterStorage(
        app,
        new BroadcastHelper(app),
        (String) app.getResources().getText(R.string.default_counter_name));
  }
}
