package me.tsukanov.counter.domain

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.repository.CounterStorage
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        CounterModule::class,
    ]
)
interface CounterApplicationComponent : AndroidInjector<CounterApplication?> {

    fun localStorage(): CounterStorage<IntegerCounter?>?

}
