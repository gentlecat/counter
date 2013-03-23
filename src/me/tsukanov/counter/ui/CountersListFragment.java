package me.tsukanov.counter.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;

public class CountersListFragment extends ListFragment {
    CounterApplication app;
    CountersListAdapter adapter;

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
        if (getActivity() instanceof CounterActivity) {
            CounterActivity ra = (CounterActivity) getActivity();
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
        Context context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getResources().getText(
                R.string.dialog_add_title));

        LinearLayout addDialogLayout = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.editor_layout, null);

        // Name input label
        TextView nameInputLabel = new TextView(context);
        nameInputLabel.setText(getResources()
                .getText(R.string.dialog_edit_name));
        addDialogLayout.addView(nameInputLabel);

        // Name input
        final EditText nameInput = new EditText(context);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setText("");
        addDialogLayout.addView(nameInput);

        // Value input label
        TextView valueInputLabel = new TextView(context);
        valueInputLabel.setText(getResources().getText(
                R.string.dialog_edit_value));
        valueInputLabel.setPadding(0, 10, 0, 0);
        addDialogLayout.addView(valueInputLabel);

        // Value input
        final EditText valueInput = new EditText(context);
        valueInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        valueInput.setText("");
        InputFilter[] valueFilter = new InputFilter[1];
        valueFilter[0] = new InputFilter.LengthFilter(getValueCharLimit());
        valueInput.setFilters(valueFilter);
        addDialogLayout.addView(valueInput);

        builder.setView(addDialogLayout)
                .setPositiveButton(getResources().getText(R.string.dialog_button_add),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String name = nameInput.getText().toString();
                                if (name.equals("")) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            getResources().getText(R.string.toast_no_name_message),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    int value = 0;
                                    String valueInputContents = valueInput.getText().toString();
                                    if (!valueInputContents.equals("")) {
                                        value = Integer.parseInt(valueInputContents);
                                    }
                                    app.counters.put(name, value);
                                    updateList();
                                    switchCounterFragment(new CounterFragment(name));
                                }
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null);
        builder.create().show();
    }

    private int getValueCharLimit() {
        return String.valueOf(CounterFragment.getMaxValue()).length();
    }

    private enum DialogType {ADD}

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
