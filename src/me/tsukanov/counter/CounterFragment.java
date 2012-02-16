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
	
	// TODO Dynamically change counter text size and remove limit
	private static final int MAX_VALUE = 999, // Space limit
							MIN_VALUE = 0,
							DEFALUT_VALUE = MIN_VALUE;
	private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
	private static final int INCREMENT_SOUND = 200,
							 DECREMENT_SOUND = 201,
							 REFRESH_SOUND = 202;

	int counterValue;

	CounterApplication app;
	TextView counterLabel;
	Button incrementButton, decrementButton;
	Vibrator vibrator;

	SharedPreferences settings;
	SoundPool soundPool;
	HashMap<Integer, Integer> soundsMap;
	
	public int getMaxValue() { return MAX_VALUE; }
	public int getMinValue() { return MIN_VALUE; }
	public int getDefaultValue() { return DEFALUT_VALUE; }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		app = (CounterApplication) getActivity().getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = inflater.inflate(R.layout.counter, container, false);
		counterLabel = (TextView) view.findViewById(R.id.counterLabel);
		incrementButton = (Button) view.findViewById(R.id.incrementButton);
		decrementButton = (Button) view.findViewById(R.id.decrementButton);
		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		setValue(app.counters.get(app.activeKey));

		(incrementButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) { increment(); }
		});
		(decrementButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) { decrement(); }
		});

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
			vibrate(DEFAULT_VIBRATION_DURATION);
			playSound(INCREMENT_SOUND);
			setValue(++counterValue);
		}
	}

	public void decrement() {
		if (counterValue > MIN_VALUE) {
			vibrate(DEFAULT_VIBRATION_DURATION + 20);
			playSound(DECREMENT_SOUND);
			setValue(--counterValue);
		}
	}

	public void refresh() {
		vibrate(DEFAULT_VIBRATION_DURATION + 40);
		playSound(REFRESH_SOUND);
		setValue(DEFALUT_VALUE);
	}
	
	public void setValue(int value) {
		counterValue = value;
		counterLabel.setText(Integer.toString(value));		
		checkButtons();
		saveValue();
	}
		
	private void saveValue() {
		app.counters.remove(app.activeKey);
		app.counters.put(app.activeKey, counterValue);		
	}

	private void checkButtons() {
		if (counterValue >= MAX_VALUE)
			incrementButton.setEnabled(false);
		else
			incrementButton.setEnabled(true);

		if (counterValue <= MIN_VALUE)
			decrementButton.setEnabled(false);
		else
			decrementButton.setEnabled(true);
	}
	
	private void vibrate(long duration) {
		if (settings.getBoolean("vibrationOn", true))
			vibrator.vibrate(duration);		
	}

	private void playSound(int soundID) {
		if (settings.getBoolean("soundsOn", false)) {
			AudioManager audioManager = (AudioManager) getActivity()
					.getSystemService(Context.AUDIO_SERVICE);
			float volume = (float) audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			soundPool.play(soundsMap.get(soundID), volume, volume, 1, 0, 1f);
		}
	}

}
