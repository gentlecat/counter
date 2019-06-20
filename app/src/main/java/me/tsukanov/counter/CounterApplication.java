package me.tsukanov.counter;

import android.app.Activity;
import android.app.Application;
import dagger.android.DispatchingAndroidInjector;
import javax.inject.Inject;
import me.tsukanov.counter.domain.CounterApplicationComponent;
import me.tsukanov.counter.domain.CounterModule;
import me.tsukanov.counter.domain.DaggerCounterApplicationComponent;

public class CounterApplication extends Application {

  // TODO: See if there's a better way to retrieve this
  public static final String PACKAGE_NAME = "me.tsukanov.counter";

  @Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

  private static CounterApplicationComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component =
        DaggerCounterApplicationComponent.builder().counterModule(new CounterModule(this)).build();
  }

  public static CounterApplicationComponent getComponent() {
    return component;
  }
}
