package com.logestechs.driver.utils.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Notification
import com.logestechs.driver.databinding.ItemNotificationBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity


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

    fun update(shipmentsList: ArrayList<Notification>) {
        list.addAll(shipmentsList)
        this.notifyDataSetChanged()
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

        private var mAdapter = mAdapter

        init {
            mTitleTextView = itemView.findViewById(R.id.text_title)
            mDateTextView = itemView.findViewById(R.id.text_date)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val notification = mAdapter.list[position]
                    if (notification.packageID != 0L) {
                        itemClickListener.onItemClick(notification.packageID, notification.id)
                    }
                }
            }

        }

        fun bind(notification: Notification) {
            mTitleTextView?.text = notification.title
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

            when (notification.type) {
                "RECEIVE_MESSAGE" -> {
                    handleCardExpansion(adapterPosition)
                    binding.root.setOnClickListener {
                        onCardClick(adapterPosition)
                        binding.imageArrow.visibility = View.VISIBLE
                    }
                }
                "FULFILLMENT_ORDER" -> {
                    handleCardExpansion(adapterPosition)
                    binding.root.setOnClickListener {
                        onCardClick(adapterPosition)
                        binding.imageArrow.visibility = View.VISIBLE
                    }
                }
                else -> {
                    hideExpandableSection()
                    binding.root.setOnClickListener {
                        (mAdapter.context as? LogesTechsActivity)?.setNotificationRead(mAdapter.list[adapterPosition].id)
                    }
                    binding.imageArrow.visibility = View.GONE
                }
            }
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.list[position]?.isExpanded == true) {
                mAdapter.list[position]?.isExpanded = false
                hideExpandableSection()
            } else {
                mAdapter.list[position]?.isExpanded = true
                showExpandableSection(mAdapter.list[position])
                (mAdapter.context as? LogesTechsActivity)?.setNotificationRead(mAdapter.list[position].id)
            }
        }

        private fun handleCardExpansion(position: Int) {
            if (mAdapter.list[position]?.isExpanded == true) {
                showExpandableSection(mAdapter.list[position])
            } else {
                hideExpandableSection()
            }
        }

        private fun showExpandableSection(notification: Notification) {
            binding.rvNotifications.visibility = View.VISIBLE
            binding.imageArrow.setImageDrawable(
                ContextCompat.getDrawable(
                    mAdapter.mContext!!,
                    R.drawable.ic_card_arrow_up_pink
                )
            )

            if (notification.type == "RECEIVE_MESSAGE") {
                binding.textSenderMessage.text = "${itemView.context.getString(R.string.sender_message)} ${notification.senderName ?: ""}"
                binding.textTitleMessage.text = "${itemView.context.getString(R.string.title_message)} ${notification.title}"
                binding.textBodyMessage.text = "${notification.body}"
                binding.textSenderMessage.visibility = View.VISIBLE
                binding.textTitleMessage.visibility = View.VISIBLE
                binding.textBodyMessage.visibility = View.VISIBLE
            } else if (notification.type == "FULFILLMENT_ORDER") {
                binding.textBodyMessage.text = "${notification.body}"
                binding.textBodyMessage.visibility = View.VISIBLE
            }

        }

        private fun hideExpandableSection() {
            binding.rvNotifications.visibility = View.GONE
            binding.imageArrow.setImageDrawable(
                ContextCompat.getDrawable(
                    mAdapter.mContext!!,
                    R.drawable.ic_card_arrow_down_pink
                )
            )

            binding.textSenderMessage.visibility = View.GONE
            binding.textTitleMessage.visibility = View.GONE
            binding.textBodyMessage.visibility = View.GONE
        }
    }
}