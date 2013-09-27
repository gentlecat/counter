package me.tsukanov.counter.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.CounterFragment;
import me.tsukanov.counter.ui.MainActivity;

public class EditDialog extends DialogFragment {

    public static final String TAG = "EditDialog";
    private static final String BUNDLE_ARGUMENT_NAME = "name";
    private static final String BUNDLE_ARGUMENT_VALUE = "value";

    public static EditDialog newInstance(String counterName, int counterValue) {
        EditDialog dialog = new EditDialog();

        Bundle arguments = new Bundle();
        arguments.putString(BUNDLE_ARGUMENT_NAME, counterName);
        arguments.putInt(BUNDLE_ARGUMENT_VALUE, counterValue);
        dialog.setArguments(arguments);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String name = getArguments().getString(BUNDLE_ARGUMENT_NAME);
        final int value = getArguments().getInt(BUNDLE_ARGUMENT_VALUE);
        final MainActivity activity = (MainActivity) getActivity();

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, null);

        final EditText nameInput = (EditText) dialogView.findViewById(R.id.edit_name);
        nameInput.setText(name);

        final EditText valueInput = (EditText) dialogView.findViewById(R.id.edit_value);
        valueInput.setText(String.valueOf(value));
        InputFilter[] valueFilter = new InputFilter[1];
        valueFilter[0] = new InputFilter.LengthFilter(getValueCharLimit());
        valueInput.setFilters(valueFilter);

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                .setTitle(getString(R.string.dialog_edit_title))
                .setPositiveButton(getResources().getText(R.string.dialog_button_apply),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = nameInput.getText().toString();
                                if (newName.equals("")) {
                                    Toast.makeText(activity,
                                            getResources().getText(R.string.toast_no_name_message),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    CounterApplication app = (CounterApplication) activity.getApplication();
                                    app.counters.remove(name);
                                    int newValue;
                                    String valueInputContents = valueInput.getText().toString();
                                    if (!valueInputContents.equals("")) {
                                        newValue = Integer.parseInt(valueInputContents);
                                    } else {
                                        newValue = CounterFragment.DEFAULT_VALUE;
                                    }
                                    app.counters.put(newName, newValue);
                                    activity.switchCounter(new CounterFragment(newName, newValue));
                                    activity.countersListFragment.updateList();
                                }
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null).create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    private int getValueCharLimit() {
        return String.valueOf(CounterFragment.MAX_VALUE).length();
    }

}
