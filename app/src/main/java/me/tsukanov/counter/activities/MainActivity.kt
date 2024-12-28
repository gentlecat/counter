package me.tsukanov.counter.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.SharedPrefKeys
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.infrastructure.Actions
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.repository.exceptions.MissingCounterException
import me.tsukanov.counter.view.CounterFragment
import me.tsukanov.counter.view.CountersListFragment
import me.tsukanov.counter.view.Themes.Companion.initCurrentTheme

class MainActivity : AppCompatActivity() {

    private var toolbar: MaterialToolbar? = null
    private var navigationLayout: DrawerLayout? = null
    private var sharedPrefs: SharedPreferences? = null
    private var menuFrame: FrameLayout? = null
    private var selectedCounterName: String? = null
    private var selectedCounterFragment: CounterFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        registerIntentReceivers(applicationContext)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_main)

        toolbar = findViewById(R.id.mainToolbar)
        toolbar?.setNavigationOnClickListener { v: View? -> openDrawer() }
        setSupportActionBar(toolbar)

        navigationLayout = findViewById(R.id.mainNavigationLayout)
        menuFrame = findViewById(R.id.mainMenuFrame)
    }

    private fun registerIntentReceivers(context: Context) {
        val counterSelectionFilter =
            IntentFilter(Actions.SELECT_COUNTER.actionName)
        counterSelectionFilter.addCategory(Intent.CATEGORY_DEFAULT)
        ContextCompat.registerReceiver(
            context,
            CounterChangeReceiver(),
            counterSelectionFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()

        initCurrentTheme(sharedPrefs!!)

        initCountersList()
        switchCounter(findActiveCounter()!!.name)

        if (sharedPrefs!!.getBoolean(SharedPrefKeys.KEEP_SCREEN_ON.key, false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun initCountersList() {
        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainMenuFrame, CountersListFragment())
        transaction.commit()
    }

    private fun switchCounter(counterName: String) {
        this.selectedCounterName = counterName

        val bundle = Bundle()
        bundle.putString(CounterFragment.COUNTER_NAME_ATTRIBUTE, counterName)

        selectedCounterFragment = CounterFragment()
        selectedCounterFragment!!.arguments = bundle
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainContentFrame, selectedCounterFragment!!)
            .commitAllowingStateLoss()

        toolbar!!.title = counterName

        if (isNavigationOpen) {
            closeNavigation()
        }
    }

    /**
     * Finds which counter is selected based on priority.
     *
     *
     *  1. From value stored during the previous session
     *  1. First of the stored counters *(default one will be generated if none are present)*
     *
     */
    private fun findActiveCounter(): IntegerCounter? {
        val storage = CounterApplication.component!!.localStorage()

        val savedActiveCounter =
            sharedPrefs!!.getString(SharedPrefKeys.ACTIVE_COUNTER.key, null)

        // Checking whether it still exists...
        if (savedActiveCounter != null) {
            try {
                return storage!!.read(savedActiveCounter)
            } catch (e: MissingCounterException) {
                // No need to do anything.
            }
        }

        return storage!!.readAll(true)[0]
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Since there's no way to keep track of these events from the fragment side, we have to pass
        // them to it for processing.
        return super.onKeyDown(keyCode, event) || selectedCounterFragment!!.onKeyDown(keyCode)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    @SuppressLint("ApplySharedPref")
    override fun onPause() {
        super.onPause()

        val prefsEditor = sharedPrefs!!.edit()
        prefsEditor.putString(SharedPrefKeys.ACTIVE_COUNTER.key, selectedCounterName)
        prefsEditor.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isNavigationOpen) {
                    closeNavigation()
                } else {
                    openDrawer()
                }
                true
            }

            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private val isNavigationOpen: Boolean
        get() = navigationLayout!!.isDrawerOpen(menuFrame!!)

    private fun closeNavigation() {
        navigationLayout!!.closeDrawer(menuFrame!!)
    }

    private fun openDrawer() {
        navigationLayout!!.openDrawer(menuFrame!!)
    }

    private inner class CounterChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val counterName = intent.getStringExtra(BroadcastHelper.EXTRA_COUNTER_NAME)
            Log.d(
                TAG,
                String.format(
                    "Received counter change broadcast. Switching to counter \"%s\"", counterName
                )
            )
            switchCounter(counterName!!)
        }
    }

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }
}
