package me.tsukanov.counter.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.activities.MainActivity;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.domain.exception.CounterException;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.view.CounterFragment;

public class AddDialog extends DialogFragment {

  public static final String TAG = AddDialog.class.getSimpleName();

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    final MainActivity activity = (MainActivity) getActivity();

    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit, null);

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
                  String name = checkCounterName(nameInput.getText().toString().trim());

                  int value;
                  final String valueInputContents = valueInput.getText().toString().trim();
                  if (!valueInputContents.isEmpty()) {
                    try {
                      value = Integer.parseInt(valueInputContents);
                    } catch (NumberFormatException e) {
                      Log.w(TAG, "Unable to parse new value", e);
                      Toast.makeText(
                              activity,
                              getResources().getText(R.string.toast_unable_to_modify),
                              Toast.LENGTH_SHORT)
                          .show();
                      value = CounterFragment.DEFAULT_VALUE;
                    }
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
    Objects.requireNonNull(dialog.getWindow())
        .setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    return dialog;
  }

  private void addCounter(@NonNull final IntegerCounter counter) {
    CounterApplication.getComponent().localStorage().write(counter);
    new BroadcastHelper(requireContext()).sendSelectCounterBroadcast(counter.getName());
  }

  private String checkCounterName(String name){
      if (name.isEmpty()) {
          int counterCount = 1;
          String genericName = "New Counter ";
          boolean runAgain = true;
          List<String> counterNames = new ArrayList<>();
          List<IntegerCounter> counters = CounterApplication.getComponent().localStorage().readAll(false);
          for (int i = 0; i < counters.size(); i++) {
              counterNames.add(counters.get(i).getName());
          }
          while (runAgain) {
              runAgain = false;
              for (String counterName:counterNames
              ) {
                  if (counterName.equals(genericName + counterCount)) {
                      counterCount++;
                      runAgain = true;
                  }
              }
          }
          name = genericName + counterCount;
      }
      return name;
  }

}
