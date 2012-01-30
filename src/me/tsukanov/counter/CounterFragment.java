package me.tsukanov.counter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CounterFragment extends Fragment {

	int counterValue;
	TextView counterLabel;
	Button incrementButton, decrementButton;

	static CounterFragment newInstance(int id) {
		CounterFragment fragment = new CounterFragment();
		/*Bundle args = new Bundle();
		args.putInt("index", id);
		fragment.setArguments(args);
		*/return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//int mNum = getArguments() != null ? getArguments().getInt("num") : 1;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View view = inflater.inflate(R.layout.counter, container, false);
		counterLabel =  (TextView) view.findViewById(R.id.counterLabel);
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
		if (counterValue < 1000) // Space limit
			counterLabel.setText(Integer.toString(++counterValue));
	}
	
	public void decrement() {
		if (counterValue > 0)
			counterLabel.setText(Integer.toString(--counterValue));
	}

	public void refresh() {
		counterValue = 0;
		counterLabel.setText(Integer.toString(counterValue));
	}
}
