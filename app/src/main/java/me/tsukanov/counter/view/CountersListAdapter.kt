package me.tsukanov.counter.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.tsukanov.counter.R
import me.tsukanov.counter.domain.IntegerCounter

internal class CountersListAdapter(context: Context) :
    ArrayAdapter<IntegerCounter?>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_row, null)
        }
        val titleView = convertView!!.findViewById<TextView>(R.id.row_title)
        val countView = convertView.findViewById<TextView>(R.id.row_count)

        val counter = getItem(position)
        if (counter != null) {
            titleView.text = counter.name
            countView.text = counter.value.toString()
        } else {
            Log.v(TAG, String.format("Failed to get item in position %s", position))
            titleView.text = "???"
            countView.text = "???"
        }

        return convertView
    }

    companion object {
        private val TAG: String = CountersListAdapter::class.java.simpleName
    }
}
