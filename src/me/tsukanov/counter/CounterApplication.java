package me.tsukanov.counter;

import android.app.Application;

public class CounterApplication extends Application {

	public int activePosition = 0;
	public boolean isRotated;

	@Override
	public void onCreate() {
		super.onCreate();
		isRotated = false;
	}
}