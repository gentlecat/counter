package me.tsukanov.counter.view.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.activities.MainActivity
import me.tsukanov.counter.domain.IntegerCounter
import me.tsukanov.counter.domain.IntegerCounter.Companion.valueCharLimit
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.view.CounterFragment
import java.util.Objects

class EditDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val oldName = requireArguments().getString(BUNDLE_ARGUMENT_NAME)
        val oldValue = requireArguments().getInt(BUNDLE_ARGUMENT_VALUE)

        val activity = activity as MainActivity?

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_name)
        nameInput.setText(oldName)

        val valueInput = dialogView.findViewById<EditText>(R.id.edit_value)
        valueInput.setText(oldValue.toString())
        valueInput.inputType = EditorInfo.TYPE_CLASS_NUMBER
        val valueFilter = arrayOfNulls<InputFilter>(1)
        valueFilter[0] = LengthFilter(valueCharLimit)
        valueInput.filters = valueFilter

        val dialog: Dialog =
            AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(getString(R.string.dialog_edit_title))
                .setPositiveButton(
                    resources.getText(R.string.dialog_button_apply)
                ) { d: DialogInterface?, which: Int ->
                    var newName: String? = nameInput.text.toString().trim { it <= ' ' }
                    if (newName!!.isEmpty()) {
                        newName = oldName
                    }

                    var newValue: Int
                    val valueInputContents = valueInput.text.toString()
                    if (valueInputContents != "") {
                        try {
                            newValue = valueInputContents.toInt()
                        } catch (e: NumberFormatException) {
                            Log.w(TAG, "Unable to parse new value", e)
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.toast_unable_to_modify),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            newValue = CounterFragment.DEFAULT_VALUE
                        }
                    } else {
                        newValue = CounterFragment.DEFAULT_VALUE
                    }

                    val storage = CounterApplication.component!!.localStorage()

                    storage!!.delete(oldName!!)
                    storage!!.write(IntegerCounter(newName!!, newValue))
                    BroadcastHelper(requireContext()).sendSelectCounterBroadcast(newName)
                }
                .setNegativeButton(resources.getText(R.string.dialog_button_cancel), null)
                .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return dialog
    }

    companion object {
        val TAG: String = EditDialog::class.java.simpleName

        private const val BUNDLE_ARGUMENT_NAME = "name"
        private const val BUNDLE_ARGUMENT_VALUE = "value"

        fun newInstance(counterName: String, counterValue: Int): EditDialog {
            val dialog = EditDialog()

            val arguments = Bundle()
            arguments.putString(BUNDLE_ARGUMENT_NAME, counterName)
            arguments.putInt(BUNDLE_ARGUMENT_VALUE, counterValue)
            dialog.arguments = arguments

            return dialog
        }
    }

}
