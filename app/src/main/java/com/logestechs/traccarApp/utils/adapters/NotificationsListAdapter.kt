package com.logestechs.traccarApp.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.traccarApp.R
import com.logestechs.traccarApp.data.model.Notification
import com.logestechs.traccarApp.databinding.ItemNotificationBinding


class NotificationsListAdapter(
    val list: ArrayList<Notification>,
    private val itemClickListener: OnItemClickListener,
    private val context: Context
) :
    RecyclerView.Adapter<NotificationsListAdapter.NotificationViewHolder>() {

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        mContext = parent.context
        val inflater = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(inflater, parent, this, itemClickListener)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification: Notification = list[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(shipmentsList: ArrayList<Notification>) {
        list.addAll(shipmentsList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }
    interface OnItemClickListener {
        fun onItemClick(packageId: Long, notificationId: Long)
    }

    class NotificationViewHolder(
        var binding: ItemNotificationBinding,
        parent: ViewGroup,
        mAdapter: NotificationsListAdapter,
        private val itemClickListener: OnItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private var mTitleTextView: TextView? = null
        private var mDateTextView: TextView? = null
        private var mReadMark: ImageView? = null

        private var mAdapter = mAdapter

        init {
            mTitleTextView = itemView.findViewById(R.id.text_title)
            mDateTextView = itemView.findViewById(R.id.text_date)
        }

        fun bind(notification: Notification) {
            mTitleTextView?.text = notification.title
            mReadMark?.visibility = if (notification.isRead) View.GONE else View.VISIBLE
        }
    }
}