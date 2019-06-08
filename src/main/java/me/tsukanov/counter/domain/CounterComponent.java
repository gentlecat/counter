package me.tsukanov.counter.domain;

import dagger.Component;
import javax.inject.Singleton;
import me.tsukanov.counter.repository.CounterStorage;

@Singleton
@Component(modules = CounterModule.class)
public interface CounterComponent {

  CounterStorage localStorage();
}
