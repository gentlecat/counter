package me.tsukanov.counter.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.AddDialog;

public class CountersListFragment extends ListFragment {

    private CounterApplication app;
    private CountersListAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.menu, null);
        LinearLayout addButton = view.findViewById(R.id.add_counter);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAddDialog();
            }
        });
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        app = (CounterApplication) getActivity().getApplication();
        updateList();
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        CounterFragment newContent = new CounterFragment(adapter.getItem(position).name);
        if (newContent != null) switchCounterFragment(newContent);
    }

    private void switchCounterFragment(CounterFragment fragment) {
        if (getActivity() == null) return;
        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            ra.switchCounterFragment(fragment);
        }
    }

    public void updateList() {
        adapter = new CountersListAdapter(getActivity());
        for (Map.Entry<String, Integer> e : app.counters.entrySet()) {
            adapter.add(new Counter(e.getKey(), String.valueOf(e.getValue())));
        }
        setListAdapter(adapter);
    }

    private void showAddDialog() {
        AddDialog dialog = new AddDialog();
        dialog.show(getFragmentManager(), AddDialog.TAG);
    }

    private class Counter {
        final String name;
        final String count;

        Counter(String name, String count) {
            this.name = name;
            this.count = count;
        }
    }

    class CountersListAdapter extends ArrayAdapter<Counter> {

        CountersListAdapter(Context context) {
            super(context, 0);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_row_value, null);
            }
            TextView title = convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).name);
            TextView count = convertView.findViewById(R.id.row_count);
            count.setText(getItem(position).count);
            return convertView;
        }

    }

}
