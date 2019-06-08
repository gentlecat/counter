package me.tsukanov.counter.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.ViewGroup.LayoutParams;
import java.util.Objects;
import me.tsukanov.counter.CounterApplication;
import me.tsukanov.counter.R;
import me.tsukanov.counter.domain.IntegerCounter;
import me.tsukanov.counter.infrastructure.BroadcastHelper;
import me.tsukanov.counter.repository.CounterStorage;

public class DeleteDialog extends DialogFragment {

  public static final String TAG = "DeleteDialog";
  private static final String BUNDLE_ARGUMENT_NAME = "selected_position";

  public static DeleteDialog newInstance(final @NonNull String counterName) {
    final DeleteDialog dialog = new DeleteDialog();

    final Bundle arguments = new Bundle();
    arguments.putString(BUNDLE_ARGUMENT_NAME, counterName);
    dialog.setArguments(arguments);

    return dialog;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final String name = getArguments().getString(BUNDLE_ARGUMENT_NAME);

    final Dialog deleteDialog =
        new AlertDialog.Builder(getActivity())
            .setMessage(getResources().getText(R.string.dialog_delete_title))
            .setCancelable(false)
            .setPositiveButton(
                getResources().getText(R.string.dialog_button_delete),
                (dialog, id) -> {
                  final CounterStorage<IntegerCounter> storage =
                      CounterApplication.getComponent().localStorage();
                  storage.delete(name);

                  // Switch to a different counter
                  new BroadcastHelper(Objects.requireNonNull(getContext()))
                      .sendSelectCounterBroadcast(storage.getFirst().getName());
                })
            .setNegativeButton(getResources().getText(R.string.dialog_button_cancel), null)
            .create();
    deleteDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    return deleteDialog;
  }
}
