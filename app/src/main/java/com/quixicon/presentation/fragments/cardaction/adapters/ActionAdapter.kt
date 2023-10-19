package com.quixicon.presentation.fragments.cardaction.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView

class ActionAdapter(
    context: Context,
    private val layoutRes: Int,
    private val objects: List<String>
) : ArrayAdapter<String>(context, layoutRes, objects) {

    var selected = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, layoutRes, null)
        (view as? CheckedTextView)?.run {
            text = objects[position]
            isChecked = position == selected
        }
        return view!!
    }
}
