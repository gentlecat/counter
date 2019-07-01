package me.tsukanov.counter.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import java.util.List;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.CounterStorage;
import me.tsukanov.counter.view.CounterFragment;

public class AddDialog extends DialogFragment {

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit, null);

    final EditText nameInput = dialogView.findViewById(R.id.edit_name);

    final EditText valueInput = dialogView.findViewById(R.id.edit_value);
    final InputFilter[] valueFilter = new InputFilter[1];
    valueFilter[0] = new InputFilter.LengthFilter(IntegerCounter.getValueCharLimit());
    valueInput.setFilters(valueFilter);

    final Dialog dialog =
        new AlertDialog.Builder(getActivity())
            .setView(dialogView)
            .setTitle(getString(R.string.dialog_add_title))
            .setPositiveButton(
                getResources().getText(R.string.dialog_button_add),
                (dialog1, which) -> {
                  final String name = nameInput.getText().toString().trim();
                  if (name.isEmpty()) {
                    Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.toast_no_name_message),
                            Toast.LENGTH_SHORT)
                        .show();
                    return;
                  }

                  int value;
                  final String valueInputContents = valueInput.getText().toString().trim();
                  if (!valueInputContents.isEmpty()) {
                    value = Integer.parseInt(valueInputContents);
                  } else {
                    value = CounterFragment.DEFAULT_VALUE;
                  }

                  try {
                    addCounter(new IntegerCounter(name, value));
                  } catch (CounterException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                  }
                })
            .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null)
            .create();

    dialog.setCanceledOnTouchOutside(true);
    dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    return dialog;
  }

  private void addCounter(@NonNull final IntegerCounter counter) {
    CounterApplication.getComponent().localStorage().write(counter);
    new BroadcastHelper(this.getContext()).sendSelectCounterBroadcast(counter.getName());
  }
}
