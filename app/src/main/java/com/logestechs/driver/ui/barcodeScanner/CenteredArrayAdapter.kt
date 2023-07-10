package com.logestechs.driver.ui.barcodeScanner

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.logestechs.driver.R

class CenteredArrayAdapter<T>(
    context: Context,
    resource: Int,
    objects: Array<T>
) : ArrayAdapter<T>(context, resource, objects) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item_centered, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)?.toString()
        return view
    }
}

