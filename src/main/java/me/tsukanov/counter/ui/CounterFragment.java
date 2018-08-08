package me.tsukanov.counter.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.DeleteDialog;
import me.tsukanov.counter.ui.dialogs.EditDialog;

public class CounterFragment extends Fragment {
    public static final int MAX_VALUE = 9999;
    public static final int MIN_VALUE = 0;
    public static final int DEFAULT_VALUE = 0;
    private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
    private String name = null;
    private int value = DEFAULT_VALUE;
    private CounterApplication app;
    private SharedPreferences settings;
    private Vibrator vibrator;
    private SoundPool soundPool;
    private SparseIntArray soundsMap;
    private TextView counterLabel;
    private Button incrementButton;
    private Button decrementButton;

    public CounterFragment() {
    }

    @SuppressLint("ValidFragment")
    public CounterFragment(String name) {
        this.name = name;
    }

    @SuppressLint("ValidFragment")
    public CounterFragment(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getCounterName() {
        return name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (CounterApplication) getActivity().getApplication();
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        /** Setting up sounds */
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundsMap = new SparseIntArray(2);
        soundsMap.put(Sound.INCREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.increment_sound, 1));
        soundsMap.put(Sound.DECREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.decrement_sound, 1));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.counter, container, false);

        counterLabel = (TextView) view.findViewById(R.id.counterLabel);

        incrementButton = (Button) view.findViewById(R.id.incrementButton);
        incrementButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                increment();
            }
        });

        decrementButton = (Button) view.findViewById(R.id.decrementButton);
        decrementButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                decrement();
            }
        });

        if (name == null) name = getActivity().getString(R.string.default_counter_name);
        if (app.counters.containsKey(name)) {
            setValue(app.counters.get(name));
        } else {
            app.counters.put(name, value);
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(name);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = ((MainActivity) getActivity()).isNavigationOpen();

        /** Edit */
        MenuItem editItem = menu.findItem(R.id.menu_edit);
        editItem.setVisible(!isDrawerOpen);

        /** Delete */
        MenuItem deleteItem = menu.findItem(R.id.menu_delete);
        deleteItem.setVisible(!isDrawerOpen);

        /** Reset */
        MenuItem resetItem = menu.findItem(R.id.menu_reset);
        resetItem.setVisible(!isDrawerOpen);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (settings.getBoolean("hardControlOn", true)) {
                    increment();
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (settings.getBoolean("hardControlOn", true)) {
                    decrement();
                    return true;
                }
                return false;
            case KeyEvent.KEYCODE_CAMERA:
                if (settings.getBoolean("hardControlOn", true)) {
                    reset();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                showResetConfirmationDialog();
                return true;
            case R.id.menu_edit:
                showEditDialog();
                return true;
            case R.id.menu_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showResetConfirmationDialog() {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getText(R.string.dialog_reset_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.dialog_button_reset),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                reset();
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null)
                .create();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private void showEditDialog() {
        EditDialog dialog = EditDialog.newInstance(name, value);
        dialog.show(getFragmentManager(), EditDialog.TAG);
    }

    private void showDeleteDialog() {
        DeleteDialog dialog = DeleteDialog.newInstance(name);
        dialog.show(getFragmentManager(), DeleteDialog.TAG);
    }

    public void increment() {
        if (value < MAX_VALUE) {
            setValue(++value);
            vibrate(DEFAULT_VIBRATION_DURATION);
            playSound(Sound.INCREMENT_SOUND);
        }
    }

    public void decrement() {
        if (value > MIN_VALUE) {
            setValue(--value);
            vibrate(DEFAULT_VIBRATION_DURATION + 20);
            playSound(Sound.DECREMENT_SOUND);
        }
    }

    public void reset() {
        setValue(DEFAULT_VALUE);
    }

    public void setValue(int value) {
        if (value > MAX_VALUE) value = MAX_VALUE;
        else if (value < MIN_VALUE) value = MIN_VALUE;
        this.value = value;
        counterLabel.setText(Integer.toString(value));
        checkStateOfButtons();
        saveValue();
    }

    private void saveValue() {
        app.counters.remove(name);
        app.counters.put(name, value);
    }

    public String getName() {
        return name;
    }

    private void checkStateOfButtons() {
        if (value >= MAX_VALUE) incrementButton.setEnabled(false);
        else incrementButton.setEnabled(true);
        if (value <= MIN_VALUE) decrementButton.setEnabled(false);
        else decrementButton.setEnabled(true);
    }

    private void vibrate(long duration) {
        if (settings.getBoolean("vibrationOn", true)) {
            vibrator.vibrate(duration);
        }
    }

    private void playSound(Sound sound) {
        if (settings.getBoolean("soundsOn", false)) {
            AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            soundPool.play(soundsMap.get(sound.ordinal()), volume, volume, 1, 0, 1f);
        }
    }

    private enum Sound {INCREMENT_SOUND, DECREMENT_SOUND}

}
