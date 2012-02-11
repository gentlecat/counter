package me.tsukanov.counter;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterFragment extends Fragment {

	private static final int MAX_VALUE = 1000; // Space limit
	private static final int MIN_VALUE = 0;
	private static final int DEFALUT_VALUE = MIN_VALUE;
	private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
	private static final Integer INCREMENT_SOUND = 200;
	private static final Integer DECREMENT_SOUND = 201;
	private static final Integer REFRESH_SOUND = 202;

	int counterValue;
	TextView counterLabel;
	Button incrementButton, decrementButton;
	Vibrator vibrator;

	SharedPreferences settings;
	SoundPool soundPool;
	HashMap<Integer, Integer> soundsMap;

	static CounterFragment newInstance(int id) {
		CounterFragment fragment = new CounterFragment();
		Bundle args = new Bundle();
		args.putInt("index", id);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// TODO Implement
		// int id = getArguments() != null ? getArguments().getInt("num") : 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = inflater.inflate(R.layout.counter, container, false);
		counterLabel = (TextView) view.findViewById(R.id.counterLabel);
		incrementButton = (Button) view.findViewById(R.id.incrementButton);
		decrementButton = (Button) view.findViewById(R.id.decrementButton);

		// TODO Load saved value
		updateButtons();

		(counterLabel).setText(Integer.toString(counterValue));
		(incrementButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				increment();
			}
		});
		(decrementButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				decrement();
			}
		});
		
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundsMap = new HashMap<Integer, Integer>();
        soundsMap.put(INCREMENT_SOUND,
                        soundPool.load(getActivity(), R.raw.increment_sound, 1));
        soundsMap.put(DECREMENT_SOUND,
                        soundPool.load(getActivity(), R.raw.decrement_sound, 1));
        soundsMap.put(REFRESH_SOUND,
        				soundPool.load(getActivity(), R.raw.refresh_sound, 1));
 
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
			if (settings.getBoolean("vibrationOn", true))
				vibrator.vibrate(DEFAULT_VIBRATION_DURATION);
			playSound(INCREMENT_SOUND);
			counterLabel.setText(Integer.toString(++counterValue));
			// TODO Increment in app.counters
		}
		updateButtons();
		Log.v("Counter", "Incremented");
	}

	public void decrement() {
		if (counterValue > MIN_VALUE) {
			if (settings.getBoolean("vibrationOn", true))
				vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 20);
			playSound(DECREMENT_SOUND);
			counterLabel.setText(Integer.toString(--counterValue));
			// TODO Decrement in app.counters
		}
		updateButtons();
		Log.v("Counter", "Decremented");
	}

	public void refresh() {
		if (settings.getBoolean("vibrationOn", true))
			vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 40);
		playSound(REFRESH_SOUND);
		counterValue = DEFALUT_VALUE;
		counterLabel.setText(Integer.toString(counterValue));
		// TODO Refresh in app.counters
		updateButtons(); // TODO Check if needed later
		Log.v("Counter", "Refreshed");
	}

	private void updateButtons() {
		if (counterValue >= MAX_VALUE)
			incrementButton.setEnabled(false);
		else
			incrementButton.setEnabled(true);

		if (counterValue <= MIN_VALUE)
			decrementButton.setEnabled(false);
		else
			decrementButton.setEnabled(true);
	}
	
    private void playSound(int soundID) {
        if (settings.getBoolean("soundsOn", false)) {
                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                float volume = (float) audioManager
                                .getStreamVolume(AudioManager.STREAM_MUSIC);
                soundPool.play(soundsMap.get(soundID), volume, volume, 1, 0, 1f);
        }
    }

}
