package me.tsukanov.counter;

import android.content.Context;
import android.content.SharedPreferences;
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

	int counterValue;
	TextView counterLabel;
	Button incrementButton, decrementButton;
	Vibrator vibrator;

	SharedPreferences settings;

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

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
			counterLabel.setText(Integer.toString(--counterValue));
			// TODO Decrement in app.counters
		}
		updateButtons();
		Log.v("Counter", "Decremented");
	}

	public void refresh() {
		if (settings.getBoolean("vibrationOn", true))
			vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 40);
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

}
