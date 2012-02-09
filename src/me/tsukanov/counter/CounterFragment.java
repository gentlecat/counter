package me.tsukanov.counter;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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

	private static final int MAX_VALUE = 1000; // Space limit
	private static final int MIN_VALUE = 0;
	private static final int DEFALUT_VALUE = 0;
	private static final long DEFAULT_VIBRATION_DURATION = 30; // Milliseconds

	int counterValue;
	TextView counterLabel;
	Button incrementButton, decrementButton;
	Vibrator vibrator;

	static CounterFragment newInstance(int id) {
		CounterFragment fragment = new CounterFragment();
		/*
		 * Bundle args = new Bundle(); args.putInt("index", id);
		 * fragment.setArguments(args);
		 */
		return fragment;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		// int mNum = getArguments() != null ? getArguments().getInt("num") : 1;
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

		return view;
	}

	public void increment() {
		if (counterValue < MAX_VALUE) {
			vibrator.vibrate(DEFAULT_VIBRATION_DURATION);
			counterLabel.setText(Integer.toString(++counterValue));
		}
		updateButtons();
	}

	public void decrement() {
		if (counterValue > MIN_VALUE) {
			vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 20);
			counterLabel.setText(Integer.toString(--counterValue));
		}
		updateButtons();
	}

	public void refresh() {
		vibrator.vibrate(DEFAULT_VIBRATION_DURATION + 40);
		counterValue = DEFALUT_VALUE;
		counterLabel.setText(Integer.toString(counterValue));
		updateButtons(); // TODO Check if needed later
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
