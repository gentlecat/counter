package me.tsukanov.counter.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.dialogs.AddDialog;

public class CountersListFragment extends ListFragment {

    private CounterApplication app;
    private CountersListAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.menu, null);
        LinearLayout addButton = (LinearLayout) view.findViewById(R.id.add_counter);
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
        if (newContent != null)
            switchCounterFragment(newContent);
    }

    private void switchCounterFragment(CounterFragment fragment) {
        if (getActivity() == null) return;
        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            ra.switchCounter(fragment);
        }
    }

    public void updateList() {
        adapter = new CountersListAdapter(getActivity());
        for (String key : app.counters.keySet()) {
            adapter.add(new Counter(key));
        }
        setListAdapter(adapter);
    }

    private void showAddDialog() {
        AddDialog dialog = new AddDialog();
        dialog.show(getFragmentManager(), AddDialog.TAG);
    }

    private class Counter {
        public String name;

        public Counter(String name) {
            this.name = name;
        }
    }

    public class CountersListAdapter extends ArrayAdapter<Counter> {

        public CountersListAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_row, null);
            }
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).name);
            return convertView;
        }

    }

}
