package me.tsukanov.counter.domain

import dagger.Module
import dagger.Provides
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.repository.CounterStorage
import me.tsukanov.counter.repository.SharedPrefsCounterStorage
import javax.inject.Singleton

@Module
class CounterModule(private val app: CounterApplication) {

    @Provides
    @Singleton
    fun provideApp(): CounterApplication {
        return app
    }

    @Provides
    @Singleton
    fun provideCounterStorage(app: CounterApplication): CounterStorage<IntegerCounter> {
        return SharedPrefsCounterStorage(
            app,
            BroadcastHelper(app),
            app.resources.getText(R.string.default_counter_name) as String
        )
    }

}
