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


class NotificationsListAdapter(val list: ArrayList<Notification>) :
    RecyclerView.Adapter<NotificationsListAdapter.NotificationViewHolder>() {

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        mContext = parent.context
        val inflater = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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

    class NotificationViewHolder(
        var binding: ItemNotificationBinding,
        parent: ViewGroup,
        mAdapter: NotificationsListAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
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
            sizeIcons()
            if (notification.type == "RECEIVE_MESSAGE") {
                handleCardExpansion(adapterPosition)
                binding.root.setOnClickListener {
                    onCardClick(adapterPosition)
                    binding.imageArrow.visibility = View.VISIBLE
                }
            } else {
                // If the type is not RECEIVE_MESSAGE, hide the expandable section and remove click listener.
                hideExpandableSection()
                binding.root.setOnClickListener(null)
                binding.imageArrow.visibility = View.GONE
            }
        }

        private fun sizeIcons() {
            val iconTitle: Drawable? = ContextCompat.getDrawable(itemView.context, R.drawable.ic_title_message)
            val iconSender: Drawable? = ContextCompat.getDrawable(itemView.context, R.drawable.ic_sender_message)

            val iconWidth = 60
            val iconHeight = 60

            if (iconTitle is VectorDrawable) {
                iconTitle.setBounds(0, 0, iconWidth, iconHeight)
            }
            if (iconSender is VectorDrawable) {
                iconSender.setBounds(0, 0, iconWidth, iconHeight)
            }

            binding.textTitleMessage.setCompoundDrawablesRelative(iconTitle, null, null, null)
            binding.textSenderMessage.setCompoundDrawablesRelative(iconSender, null, null, null)
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.list[position]?.isExpanded == true) {
                mAdapter.list[position]?.isExpanded = false
                hideExpandableSection()
            } else {
                mAdapter.list[position]?.isExpanded = true
                showExpandableSection(mAdapter.list[position])
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

            // Show the message title and body views when the card is expanded
            binding.textSenderMessage.text = "${itemView.context.getString(R.string.sender_message)} ${notification.title}"
            binding.textTitleMessage.text = "${itemView.context.getString(R.string.title_message)} ${notification.title}"
            binding.textBodyMessage.text = "${notification.body}"
            binding.textSenderMessage.visibility = View.VISIBLE
            binding.textTitleMessage.visibility = View.VISIBLE
            binding.textBodyMessage.visibility = View.VISIBLE
        }

        private fun hideExpandableSection() {
            binding.rvNotifications.visibility = View.GONE
            binding.imageArrow.setImageDrawable(
                ContextCompat.getDrawable(
                    mAdapter.mContext!!,
                    R.drawable.ic_card_arrow_down_pink
                )
            )

            // Hide the message title and body views when the card is collapsed
            binding.textSenderMessage.visibility = View.GONE
            binding.textTitleMessage.visibility = View.GONE
            binding.textBodyMessage.visibility = View.GONE
        }
    }
}