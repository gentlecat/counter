package me.tsukanov.counter.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class AddDialog extends DialogFragment {

    public static final String TAG = "AddDialog";

    public AddDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) getActivity();

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, null);

        final EditText nameInput = dialogView.findViewById(R.id.edit_name);

        final EditText valueInput = dialogView.findViewById(R.id.edit_value);
        InputFilter[] valueFilter = new InputFilter[1];
        valueFilter[0] = new InputFilter.LengthFilter(getValueCharLimit());
        valueInput.setFilters(valueFilter);

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                .setTitle(getString(R.string.dialog_add_title))
                .setPositiveButton(getResources().getText(R.string.dialog_button_add),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String name = nameInput.getText().toString();
                                if (name.equals("")) {
                                    Toast.makeText(activity,
                                            getResources().getText(R.string.toast_no_name_message),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    int value;
                                    String valueInputContents = valueInput.getText().toString();
                                    if (!valueInputContents.equals("")) {
                                        value = Integer.parseInt(valueInputContents);
                                    } else {
                                        value = CounterFragment.DEFAULT_VALUE;
                                    }
                                    CounterApplication app = (CounterApplication) activity.getApplication();
                                    app.counters.put(name, value);
                                    activity.switchCounterFragment(new CounterFragment(name, value));
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
