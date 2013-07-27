package me.tsukanov.counter.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import me.tsukanov.counter.R;

public class AboutDialog extends DialogFragment {
    public static final String TAG = "about";

    public AboutDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_about, null);

        TextView content = (TextView) view.findViewById(R.id.about_content);
        content.setText(Html.fromHtml(getResources().getString(R.string.info)));
        content.setMovementMethod(LinkMovementMethod.getInstance());

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(getResources().getString(R.string.app_name))
                .setNeutralButton(
                        getString(R.string.dialog_button_close),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        return dialog;
    }

}
