package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Notification
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper


class NotificationsListAdapter(val list: ArrayList<Notification>) :
    RecyclerView.Adapter<NotificationViewHolder>() {

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return NotificationViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification: Notification = list[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = list.size

    fun update(shipmentsList: ArrayList<Notification>) {
        list.addAll(shipmentsList)
        this.notifyDataSetChanged()
    }

}

class NotificationViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    mAdapter: NotificationsListAdapter
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_notification, parent, false)) {
    private var mTitleTextView: TextView? = null
    private var mDateTextView: TextView? = null

    private var mAdapter = mAdapter

    init {
        mTitleTextView = itemView.findViewById(R.id.text_title)
        mDateTextView = itemView.findViewById(R.id.text_date)

    }

    fun bind(notification: Notification) {
        mTitleTextView?.text = notification.bodyArabic
        mDateTextView?.text = "${
            Helper.formatServerDateLocalized(
                notification.createdDate,
                DateFormats.NOTIFICATION_DAY_FORMAT
            )
        } ,${
            Helper.formatServerDate(
                notification.createdDate,
                DateFormats.NOTIFICATION_LIST_ITEM_FORMAT
            )
        }"
    }

}
