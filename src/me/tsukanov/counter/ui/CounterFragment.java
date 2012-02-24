package me.tsukanov.counter.ui;

import java.util.HashMap;

import me.tsukanov.counter.R;
import me.tsukanov.counter.view.AutoResizableTextView;
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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CounterFragment extends Fragment {
	
	private static final int MAX_VALUE = 999999999;
	private static final int MIN_VALUE = 0;
	private static final int DEFALUT_VALUE = MIN_VALUE;
	private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds
	private static final int INCREMENT_SOUND = 200;
	private static final int DECREMENT_SOUND = 201;
	private static final int REFRESH_SOUND = 202;

	int counterValue = DEFALUT_VALUE;
	CounterApplication app;
	SharedPreferences settings;
	Vibrator vibrator;
	SoundPool soundPool;	
	HashMap<Integer, Integer> soundsMap;
	
	AutoResizableTextView counterLabel;
	Button incrementButton;
	Button decrementButton;
	
	public static int getMaxValue() { return MAX_VALUE; }
	public static int getMinValue() { return MIN_VALUE; }
	public static int getDefaultValue() { return DEFALUT_VALUE; }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		app = (CounterApplication) getActivity().getApplication();
		vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(INCREMENT_SOUND, soundPool.load(getActivity(), R.raw.increment_sound, 1));
		soundsMap.put(DECREMENT_SOUND, soundPool.load(getActivity(), R.raw.decrement_sound, 1));
		soundsMap.put(REFRESH_SOUND,   soundPool.load(getActivity(), R.raw.refresh_sound, 1));
	
		Log.v("Fragments", "Fragment for counter \"" + app.activeKey + "\" created");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = inflater.inflate(R.layout.counter, container, false);
		
		counterLabel = (AutoResizableTextView) view.findViewById(R.id.counterLabel);
		
		incrementButton = (Button) view.findViewById(R.id.incrementButton);
		incrementButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { increment(); }
		});
		
		decrementButton = (Button) view.findViewById(R.id.decrementButton);
		decrementButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { decrement(); }
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
			playSound(INCREMENT_SOUND);
		}
	}

	public void decrement() {
		if (counterValue > MIN_VALUE) {
			setValue(--counterValue);
			vibrate(DEFAULT_VIBRATION_DURATION + 20);
			playSound(DECREMENT_SOUND);
		}
	}

	public void refresh() {
		setValue(DEFALUT_VALUE);
		vibrate(DEFAULT_VIBRATION_DURATION + 40);
		playSound(REFRESH_SOUND);
	}
	
	public void setValue(int value) {
		counterValue = value;
		counterLabel.setText(Integer.toString(value));
		checkButtons();
		saveValue(); // That's probably not effective
		Log.v("Counters", "Set value " + counterValue + " to \"" + app.activeKey + "\"");
	}
		
	private void saveValue() {
		/*
		 *  TODO Investigate
		 *  Saving value onPause() is not working properly
		 */
		app.counters.remove(app.activeKey);
		app.counters.put(app.activeKey, counterValue);		
		Log.v("Counters", "Saved value of \"" + app.activeKey + "\"");
	}

	private void checkButtons() {
		// Increment button
		if (counterValue >= MAX_VALUE) incrementButton.setEnabled(false);
		else incrementButton.setEnabled(true);
		// Decrement button
		if (counterValue <= MIN_VALUE) decrementButton.setEnabled(false);
		else decrementButton.setEnabled(true);
	}
	
	private void vibrate(long duration) {
		if (settings.getBoolean("vibrationOn", true))
			vibrator.vibrate(duration);		
	}

	private void playSound(int soundID) {
		if (settings.getBoolean("soundsOn", false)) {
			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			soundPool.play(soundsMap.get(soundID), volume, volume, 1, 0, 1f);
		}
	}

}
