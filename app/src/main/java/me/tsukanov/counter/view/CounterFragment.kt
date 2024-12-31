package me.tsukanov.counter.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.SharedPrefKeys
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.repository.exceptions.MissingCounterException
import me.tsukanov.counter.view.dialogs.DeleteDialog
import me.tsukanov.counter.view.dialogs.EditDialog
import org.apache.commons.text.StringSubstitutor
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import java.util.Map

class CounterFragment : Fragment() {

    /** Name is used as a key to look up and modify counter value in storage.  */
    private var name: String? = null

    private var sharedPrefs: SharedPreferences? = null
    private var vibrator: Vibrator? = null

    private var counterLabel: TextView? = null
    private var updateTimestampLabel: TextView? = null
    private var incrementButton: Button? = null
    private var decrementButton: Button? = null

    private var counter: IntegerCounter? = null

    private var incrementSoundPlayer: MediaPlayer? = null
    private var decrementSoundPlayer: MediaPlayer? = null

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }

        this.name = savedInstanceState.getString(STATE_SELECTED_COUNTER_NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSavedState(savedInstanceState)

        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        initCounter()

        setHasOptionsMenu(true)
    }

    private fun initCounter() {
        val storage = CounterApplication.component!!.localStorage()
        try {
            val requestedCounter = requireArguments().getString(COUNTER_NAME_ATTRIBUTE)
            if (requestedCounter == null) {
                this.counter = storage!!.readAll(true)[0]
                return
            }
            this.counter = storage!!.read(requestedCounter)
        } catch (e: MissingCounterException) {
            Log.w(TAG, "Unable to find provided counter. Retrieving a different one.", e)
            this.counter = storage!!.readAll(true)[0]
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putString(STATE_SELECTED_COUNTER_NAME, this.name)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        /* Setting up sounds */
        incrementSoundPlayer = MediaPlayer.create(context, R.raw.increment_sound)
        decrementSoundPlayer = MediaPlayer.create(context, R.raw.decrement_sound)
    }

    override fun onPause() {
        super.onPause()

        incrementSoundPlayer!!.reset()
        incrementSoundPlayer!!.release()
        decrementSoundPlayer!!.reset()
        decrementSoundPlayer!!.release()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.counter, container, false)

        counterLabel = view.findViewById(R.id.counterLabel)

        incrementButton = view.findViewById(R.id.incrementButton)
        incrementButton!!.setOnClickListener { v: View? -> increment() }

        decrementButton = view.findViewById(R.id.decrementButton)
        decrementButton!!.setOnClickListener { v: View? -> decrement() }

        if (sharedPrefs!!.getBoolean(SharedPrefKeys.HIDE_CONTROLS.key, false)) {
            incrementButton!!.visibility = View.GONE;
            decrementButton!!.visibility = View.GONE;
        }

        updateTimestampLabel = view.findViewById(R.id.updateTimestampLabel)

        if (sharedPrefs!!.getBoolean(SharedPrefKeys.HIDE_LAST_UPDATE.key, false)) {
            updateTimestampLabel!!.visibility = View.GONE;
        }

        view.findViewById<View>(R.id.counterFrame)
            .setOnClickListener { v: View? ->
                if (sharedPrefs!!.getBoolean(SharedPrefKeys.LABEL_CONTROL_ON.key, true)) {
                    increment()
                }
            }

        updateInterface()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.counter_menu, menu)
    }

    fun onKeyDown(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (sharedPrefs!!.getBoolean(SharedPrefKeys.HARDWARE_BTN_CONTROL_ON.key, true)) {
                    increment()
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (sharedPrefs!!.getBoolean(SharedPrefKeys.HARDWARE_BTN_CONTROL_ON.key, true)) {
                    decrement()
                    return true
                }
                return false
            }

            else -> return false
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_reset -> {
                showResetConfirmationDialog()
                return true
            }

            R.id.menu_edit -> {
                showEditDialog()
                return true
            }

            R.id.menu_delete -> {
                showDeleteDialog()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showResetConfirmationDialog() {
        val dialog: Dialog =
            AlertDialog.Builder(activity)
                .setMessage(resources.getText(R.string.dialog_reset_title))
                .setCancelable(false)
                .setPositiveButton(
                    resources.getText(R.string.dialog_button_reset)
                ) { d: DialogInterface?, id: Int -> reset() }
                .setNegativeButton(resources.getText(R.string.dialog_button_cancel), null)
                .create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun showEditDialog() {
        val dialog = EditDialog.newInstance(counter!!.name, counter!!.value)
        dialog.show(parentFragmentManager, EditDialog.TAG)
    }

    private fun showDeleteDialog() {
        val dialog = DeleteDialog.newInstance(counter!!.name)
        dialog.show(parentFragmentManager, DeleteDialog.TAG)
    }

    private fun increment() {
        counter!!.increment()
        vibrate(DEFAULT_VIBRATION_DURATION)
        playSound(incrementSoundPlayer!!)

        updateInterface()
        saveValue()
    }

    private fun decrement() {
        counter!!.decrement()
        vibrate(DEFAULT_VIBRATION_DURATION + 20)
        playSound(decrementSoundPlayer!!)

        updateInterface()
        saveValue()
    }

    private fun reset() {
        try {
            counter!!.reset()
        } catch (e: Exception) {
            Log.getStackTraceString(e)
            throw RuntimeException(e)
        }

        updateInterface()
        saveValue()
    }

    /** Updates UI elements of the fragment based on current value of the counter.  */
    @SuppressLint("SetTextI18n")
    private fun updateInterface() {
        counterLabel!!.text = counter!!.value.toString()

        if (counter!!.lastUpdatedDate != null) {
            val formattedTimestamp =
                counter!!.lastUpdatedDate!!.toString(
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                        .withLocale(Locale.getDefault())
                )
            updateTimestampLabel!!.text = StringSubstitutor.replace(
                resources.getText(R.string.last_update_timestamp),
                Map.of("timestamp", formattedTimestamp),
                "{",
                "}"
            )
        }

        incrementButton!!.isEnabled = counter!!.value < IntegerCounter.MAX_VALUE
        decrementButton!!.isEnabled = counter!!.value > IntegerCounter.MIN_VALUE
    }

    private fun saveValue() {
        val storage = CounterApplication.component!!.localStorage()
        storage!!.write(counter)
    }

    /** Triggers vibration for a specified duration, if vibration is turned on.  */
    private fun vibrate(duration: Long) {
        if (sharedPrefs!!.getBoolean(SharedPrefKeys.VIBRATION_ON.key, true)) {
            try {
                vibrator!!.vibrate(duration)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to vibrate", e)
            }
        }
    }

    /** Plays sound if sounds are turned on.  */
    private fun playSound(soundPlayer: MediaPlayer) {
        if (sharedPrefs!!.getBoolean(SharedPrefKeys.SOUNDS_ON.key, false)) {
            try {
                if (soundPlayer.isPlaying) {
                    soundPlayer.seekTo(0)
                }
                soundPlayer.start()
            } catch (e: Exception) {
                Log.e(TAG, "Unable to play sound", e)
            }
        }
    }

    companion object {
        private val TAG: String = CounterFragment::class.java.simpleName

        const val COUNTER_NAME_ATTRIBUTE: String = "COUNTER_NAME"

        const val DEFAULT_VALUE: Int = 0
        private const val DEFAULT_VIBRATION_DURATION: Long = 30 // Milliseconds

        private const val STATE_SELECTED_COUNTER_NAME = "SELECTED_COUNTER_NAME"
    }
}
