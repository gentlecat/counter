package me.tsukanov.counter.domain;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import javax.inject.Singleton;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.repository.CounterStorage;

@Singleton
@Component(modules = { AndroidInjectionModule.class, CounterModule.class})
public interface CounterApplicationComponent extends AndroidInjector<CounterApplication> {

  CounterStorage localStorage();
}
