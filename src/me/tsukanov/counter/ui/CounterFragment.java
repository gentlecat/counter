package me.tsukanov.counter.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;

public class CounterFragment extends SherlockFragment {

    private static final int MAX_VALUE = 9999;
    private static final int MIN_VALUE = 0;
    private static final int DEFAULT_VALUE = MIN_VALUE;
    private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
    private int counterValue = DEFAULT_VALUE;
    private CounterApplication app;
    private SharedPreferences settings;
    private Vibrator vibrator;
    private SoundPool soundPool;
    private SparseIntArray soundsMap;
    private TextView counterLabel;
    private Button incrementButton;
    private Button decrementButton;

    public static int getMaxValue() {
        return MAX_VALUE;
    }

    public static int getMinValue() {
        return MIN_VALUE;
    }

    public static int getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        app = (CounterApplication) getActivity().getApplication();
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundsMap = new SparseIntArray(3);
        soundsMap.put(Sound.INCREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.increment_sound, 1));
        soundsMap.put(Sound.DECREMENT_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.decrement_sound, 1));
        soundsMap.put(Sound.REFRESH_SOUND.ordinal(), soundPool.load(getActivity(), R.raw.refresh_sound, 1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setValue(app.counters.get(app.activeKey));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void increment() {
        if (counterValue < MAX_VALUE) {
            setValue(++counterValue);
            vibrate(DEFAULT_VIBRATION_DURATION);
            playSound(Sound.INCREMENT_SOUND);
        }
    }

    public void decrement() {
        if (counterValue > MIN_VALUE) {
            setValue(--counterValue);
            vibrate(DEFAULT_VIBRATION_DURATION + 20);
            playSound(Sound.DECREMENT_SOUND);
        }
    }

    public void refresh() {
        setValue(DEFAULT_VALUE);
        vibrate(DEFAULT_VIBRATION_DURATION + 40);
        playSound(Sound.REFRESH_SOUND);
    }

    public void setValue(int value) {
        counterValue = value;
        counterLabel.setText(Integer.toString(value));
        checkStateOfButtons();
        saveValue();
    }

    private void saveValue() {
        app.counters.remove(app.activeKey);
        app.counters.put(app.activeKey, counterValue);
    }

    private void checkStateOfButtons() {
        if (counterValue >= MAX_VALUE) incrementButton.setEnabled(false);
        else incrementButton.setEnabled(true);
        if (counterValue <= MIN_VALUE) decrementButton.setEnabled(false);
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

    enum Sound {INCREMENT_SOUND, DECREMENT_SOUND, REFRESH_SOUND}

}
