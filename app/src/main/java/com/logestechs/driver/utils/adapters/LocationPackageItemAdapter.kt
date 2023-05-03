package com.logestechs.driver.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.ServerLatLng

class LocationPackageItemAdapter(private var locations: List<ServerLatLng?>) :
    RecyclerView.Adapter<LocationPackageItemAdapter.ViewHolder>() {

    fun update(locations: List<ServerLatLng?>) {
        this.locations = locations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_label, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(locations[i])
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: AppCompatTextView

        init {
            textView = itemView.findViewById(R.id.text_title)
        }

        fun bind(location: ServerLatLng?) {
            textView.text = location?.label
        }
    }
}