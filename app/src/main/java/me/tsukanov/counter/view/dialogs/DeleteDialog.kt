package me.tsukanov.counter.view.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.infrastructure.BroadcastHelper
import org.apache.commons.text.StringSubstitutor
import java.util.Map

class DeleteDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = requireArguments().getString(BUNDLE_ARGUMENT_NAME)

        val deleteDialog: Dialog =
            AlertDialog.Builder(activity)
                .setMessage(resources.getText(R.string.dialog_delete_title))
                .setCancelable(false)
                .setPositiveButton(
                    resources.getText(R.string.dialog_button_delete)
                ) { dialog: DialogInterface?, id: Int ->
                    val storage =
                        CounterApplication.component!!.localStorage()
                    storage!!.delete(name!!)

                    Toast.makeText(
                        context,
                        StringSubstitutor.replace(
                            resources.getText(R.string.toast_delete_success),
                            Map.of("name", name),
                            "{",
                            "}"
                        ),
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    // Switch to a different counter
                    BroadcastHelper(requireContext())
                        .sendSelectCounterBroadcast(storage!!.first()!!.name)
                }
                .setNegativeButton(resources.getText(R.string.dialog_button_cancel), null)
                .create()
        deleteDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return deleteDialog
    }

    companion object {
        const val TAG: String = "DeleteDialog"
        private const val BUNDLE_ARGUMENT_NAME = "selected_position"

        fun newInstance(counterName: String): DeleteDialog {
            val dialog = DeleteDialog()

            val arguments = Bundle()
            arguments.putString(BUNDLE_ARGUMENT_NAME, counterName)
            dialog.arguments = arguments

            return dialog
        }
    }
}
