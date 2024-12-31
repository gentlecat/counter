package me.tsukanov.counter

import android.app.Activity
import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import dagger.android.DispatchingAndroidInjector
import me.tsukanov.counter.domain.CounterApplicationComponent
import me.tsukanov.counter.domain.CounterModule
import me.tsukanov.counter.domain.DaggerCounterApplicationComponent
import javax.inject.Inject

class CounterApplication : Application() {

    companion object {
        var PACKAGE_NAME: String? = null

        var component: CounterApplicationComponent? = null
            private set
    }

    @JvmField
    @Inject
    var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>? = null

    override fun onCreate() {
        super.onCreate()

        component = DaggerCounterApplicationComponent.builder()
            .counterModule(CounterModule(this))
            .build()

        initDefaultPreferences()

        PACKAGE_NAME = applicationContext.packageName

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initDefaultPreferences() {
        val wasAppUsed = component!!.localStorage()!!.readAll(false).isNotEmpty()
        if (!wasAppUsed) {
            // Hide controls if this this is the first time the app is used.
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(SharedPrefKeys.HIDE_CONTROLS.key, true)
                .commit()
        }
    }

}
