package me.tsukanov.counter.view.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.ViewGroup
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

class AddDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity as MainActivity?

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_name)

        val valueInput = dialogView.findViewById<EditText>(R.id.edit_value)
        val valueFilter = arrayOfNulls<InputFilter>(1)
        valueFilter[0] = LengthFilter(valueCharLimit)
        valueInput.filters = valueFilter

        val dialog: Dialog =
            AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(getString(R.string.dialog_add_title))
                .setPositiveButton(
                    resources.getText(R.string.dialog_button_add)
                ) { d: DialogInterface?, which: Int ->
                    var name = nameInput.text.toString().trim { it <= ' ' }
                    if (name.isEmpty()) {
                        name = generateDefaultName()
                    }

                    var value: Int
                    val valueInputContents = valueInput.text.toString().trim { it <= ' ' }
                    if (!valueInputContents.isEmpty()) {
                        try {
                            value = valueInputContents.toInt()
                        } catch (e: NumberFormatException) {
                            Log.w(TAG, "Unable to parse new value", e)
                            Toast.makeText(
                                activity,
                                resources.getText(R.string.toast_unable_to_modify),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            value = CounterFragment.DEFAULT_VALUE
                        }
                    } else {
                        value = CounterFragment.DEFAULT_VALUE
                    }
                    addCounter(IntegerCounter(name, value))
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

    private fun addCounter(counter: IntegerCounter) {
        CounterApplication.component!!.localStorage()!!.write(counter)
        BroadcastHelper(requireContext()).sendSelectCounterBroadcast(counter.name)
    }

    private fun generateDefaultName(): String {
        val existingNames: MutableSet<String> = HashSet()
        for (c in CounterApplication.component!!.localStorage()!!.readAll(false)) {
            existingNames.add(c!!.name)
        }

        val namePrefix = getString(R.string.app_name) + " "
        var nameSuffix = existingNames.size + 1
        while (existingNames.contains(namePrefix + nameSuffix)) {
            nameSuffix++
        }
        return namePrefix + nameSuffix
    }

    companion object {
        val TAG: String = AddDialog::class.java.simpleName
    }

}
