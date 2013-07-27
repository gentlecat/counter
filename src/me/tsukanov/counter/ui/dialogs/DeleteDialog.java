package me.tsukanov.counter.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.ui.CounterFragment;
import me.tsukanov.counter.ui.MainActivity;

public class DeleteDialog extends DialogFragment {
    public static final String TAG = "delete";
    private static final String BUNDLE_ARGUMENT_NAME = "selected_position";

    public static DeleteDialog newInstance(String counterName) {
        DeleteDialog dialog = new DeleteDialog();

        Bundle arguments = new Bundle();
        arguments.putString(BUNDLE_ARGUMENT_NAME, counterName);
        dialog.setArguments(arguments);

        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String name = getArguments().getString(BUNDLE_ARGUMENT_NAME);
        final MainActivity activity = (MainActivity) getActivity();

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getText(R.string.dialog_delete_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.dialog_button_delete),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CounterApplication app = (CounterApplication) activity.getApplication();
                                app.counters.remove(name);
                                Toast.makeText(getActivity(),
                                        getResources().getText(R.string.toast_remove_success_1) + " \"" + name + "\" "
                                                + getResources().getText(R.string.toast_remove_success_2),
                                        Toast.LENGTH_SHORT).show();
                                if (app.counters.isEmpty()) {
                                    app.counters.put(getString(R.string.default_counter_name), CounterFragment.DEFAULT_VALUE);
                                    activity.switchCounter(new CounterFragment(getString(R.string.default_counter_name)));
                                } else {
                                    activity.switchCounter(new CounterFragment(app.counters.keySet().iterator().next()));
                                }
                                activity.countersListFragment.updateList();
                            }
                        })
                .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null).create();
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        return dialog;
    }

}
