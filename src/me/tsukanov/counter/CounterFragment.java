package me.tsukanov.counter;

import android.os.Bundle;
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

	int counterValue;
	TextView counterLabel;
	Button incrementButton, decrementButton;

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
		return view;
	}

	public void increment() {
		// TODO Disable button when limit reached
		if (counterValue < 1000) // Space limit
			counterLabel.setText(Integer.toString(++counterValue));
	}

	public void decrement() {
		// TODO Disable button when limit reached
		if (counterValue > 0)
			counterLabel.setText(Integer.toString(--counterValue));
	}

	public void refresh() {
		counterValue = 0;
		counterLabel.setText(Integer.toString(counterValue));
	}
}
