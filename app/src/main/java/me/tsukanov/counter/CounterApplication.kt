package me.tsukanov.counter

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import dagger.android.DispatchingAndroidInjector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        initFirstUse()

        PACKAGE_NAME = applicationContext.packageName

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initFirstUse() {
        val wasAppUsed = component!!.localStorage()!!.readAll(false).isNotEmpty()
        if (!wasAppUsed) {

            // Hide controls if this this is the first time the app is used.
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(SharedPrefKeys.HIDE_CONTROLS.key, true)
                .commit()

            // Show a tutorial toast after a few seconds.
            ProcessLifecycleOwner.get().lifecycleScope.launch {
                delay(3_000)
                Toast.makeText(this@CounterApplication, R.string.toast_tutorial, Toast.LENGTH_LONG).show()
            }

        }
    }

}
