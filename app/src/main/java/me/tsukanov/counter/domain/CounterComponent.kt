package me.tsukanov.counter.domain

import dagger.Component
import me.tsukanov.counter.repository.CounterStorage
import javax.inject.Singleton

@Singleton
@Component(modules = [CounterModule::class])
interface CounterComponent {
    fun localStorage(): CounterStorage<IntegerCounter?>?
}
