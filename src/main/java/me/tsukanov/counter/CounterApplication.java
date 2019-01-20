package me.tsukanov.counter;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import me.tsukanov.counter.ui.CounterFragment;

public class CounterApplication extends Application {

    private static final String DATA_FILE_NAME = "counters";
    public SortedMap<String, Integer> counters;
    private SharedPreferences data;

    @Override
    public void onCreate() {
        super.onCreate();
        counters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        data = getBaseContext().getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE);
        loadCounters();
    }

    private void loadCounters() {
        Map<String, ?> dataMap = data.getAll();
        if (dataMap.isEmpty()) {
            counters.put((String) getResources().getText(R.string.default_counter_name), CounterFragment.DEFAULT_VALUE);
        } else {
            for (Map.Entry<String, ?> entry : dataMap.entrySet()) {
                counters.put(entry.getKey(), (Integer) entry.getValue());
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    public void saveCounters() {
        SharedPreferences.Editor dataEditor = data.edit();
        dataEditor.clear();
        for (String name : counters.keySet()) {
            dataEditor.putInt(name, counters.get(name));
        }
        dataEditor.commit();
    }

    public void removeCounters() {
        counters.clear();
        counters.put((String) getResources().getText(R.string.default_counter_name), CounterFragment.DEFAULT_VALUE);
    }

}
