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

	public static final int MAX_VALUE = 1000; // Space limit
	public static final int MIN_VALUE = 0;
	public static final int DEFALUT_VALUE = MIN_VALUE;
	private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
	private static final Integer INCREMENT_SOUND = 200;
	private static final Integer DECREMENT_SOUND = 201;
	private static final Integer REFRESH_SOUND = 202;

	int counterValue;

	CounterApplication app;
	TextView counterLabel;
	Button incrementButton, decrementButton;
	Vibrator vibrator;

	SharedPreferences settings;
	SoundPool soundPool;
	HashMap<Integer, Integer> soundsMap;

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
			public void onClick(View v) {
				increment();
			}
		});
		(decrementButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				decrement();
			}
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
			if (settings.getBoolean("vibrationOn", true))
				vibrator.vibrate(DEFAULT_VIBRATION_DURATION);
			playSound(INCREMENT_SOUND);
			counterLabel.setText(Integer.toString(++counterValue));
			saveCounter();
			updateButtons();
		}
	}

	private void saveCounter() {
		app.counters.remove(app.activeKey);
		app.counters.put(app.activeKey, counterValue);
	}

	public void decrement() {
		if (counterValue > MIN_VALUE) {
			if (settings.getBoolean("vibrationOn", true))
				vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 20);
			playSound(DECREMENT_SOUND);
			counterLabel.setText(Integer.toString(--counterValue));
			saveCounter();
			updateButtons();
		}
	}

	public void refresh() {
		if (settings.getBoolean("vibrationOn", true))
			vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 40);
		playSound(REFRESH_SOUND);
		counterValue = DEFALUT_VALUE;
		counterLabel.setText(Integer.toString(counterValue));
		saveCounter();
		updateButtons(); // TODO Check if needed later
	}

	public void setValue(int value) {
		counterValue = value;
		counterLabel.setText(Integer.toString(counterValue));
		updateButtons();
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
			AudioManager audioManager = (AudioManager) getActivity()
					.getSystemService(Context.AUDIO_SERVICE);
			float volume = (float) audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			soundPool.play(soundsMap.get(soundID), volume, volume, 1, 0, 1f);
		}
	}

}
