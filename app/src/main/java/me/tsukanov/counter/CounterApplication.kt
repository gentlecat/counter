package me.tsukanov.counter

import android.app.Activity
import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.android.DispatchingAndroidInjector
import me.tsukanov.counter.domain.CounterApplicationComponent
import me.tsukanov.counter.domain.CounterModule
import me.tsukanov.counter.domain.DaggerCounterApplicationComponent
import javax.inject.Inject

class CounterApplication : Application() {

    @JvmField
    @Inject
    var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>? = null

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        component = DaggerCounterApplicationComponent.builder()
            .counterModule(CounterModule(this))
            .build()

        PACKAGE_NAME = applicationContext.packageName
    }

    companion object {
        var PACKAGE_NAME: String? = null

        var component: CounterApplicationComponent? = null
            private set
    }

}
