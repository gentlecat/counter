package me.tsukanov.counter.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.ListFragment
import me.tsukanov.counter.CounterApplication
import me.tsukanov.counter.R
import me.tsukanov.counter.infrastructure.Actions
import me.tsukanov.counter.infrastructure.BroadcastHelper
import me.tsukanov.counter.view.dialogs.AddDialog

class CountersListFragment : ListFragment() {

    private var listAdapter: CountersListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.menu, null)

        val addButton = view.findViewById<LinearLayout>(R.id.add_counter)
        addButton.setOnClickListener { v: View? -> showAddDialog() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateList()

        val counterSetChangeFilter =
            IntentFilter(Actions.COUNTER_SET_CHANGE.actionName)
        counterSetChangeFilter.addCategory(Intent.CATEGORY_DEFAULT)

        ContextCompat.registerReceiver(
            requireActivity().application,
            UpdateReceiver(),
            counterSetChangeFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onListItemClick(
        lv: ListView, v: View, position: Int, id: Long
    ) {
        BroadcastHelper(this.requireContext())
            .sendSelectCounterBroadcast(listAdapter!!.getItem(position)!!.name)
    }

    private fun updateList() {
        if (!isFragmentActive) {
            return
        }

        val counters = CounterApplication.component!!.localStorage()!!.readAll(false)

        listAdapter = CountersListAdapter(requireActivity())
        for (c in counters) {
            listAdapter!!.add(c)
        }
        setListAdapter(listAdapter)
    }

    private fun showAddDialog() {
        val dialog = AddDialog()
        dialog.show(parentFragmentManager, TAG)
    }

    private inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateList()
        }
    }

    private val isFragmentActive: Boolean
        get() = isAdded && !isDetached && !isRemoving

    companion object {
        private val TAG: String = CountersListFragment::class.java.simpleName
    }
}
