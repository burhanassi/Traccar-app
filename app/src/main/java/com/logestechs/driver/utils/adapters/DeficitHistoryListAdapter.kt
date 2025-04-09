package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DeficitBalanceHistory
import com.logestechs.driver.databinding.ItemDeficitsHistoryBinding
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class DeficitHistoryListAdapter(
    val list: ArrayList<DeficitBalanceHistory>,
    private val context: Context
) :
    RecyclerView.Adapter<DeficitHistoryListAdapter.DeficitHistoryViewHolder>() {

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeficitHistoryViewHolder {
        mContext = parent.context
        val inflater = ItemDeficitsHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeficitHistoryViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: DeficitHistoryViewHolder, position: Int) {
        val notification: DeficitBalanceHistory = list[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(shipmentsList: ArrayList<DeficitBalanceHistory>) {
        list.addAll(shipmentsList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    class DeficitHistoryViewHolder(
        var binding: ItemDeficitsHistoryBinding,
        parent: ViewGroup,
        mAdapter: DeficitHistoryListAdapter,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private var mTitleTextView: TextView? = null
        private var mDateTextView: TextView? = null
        private var mReadMark: ImageView? = null

        private var mAdapter = mAdapter

        init {
            mTitleTextView = itemView.findViewById(R.id.text_title)
            mDateTextView = itemView.findViewById(R.id.text_date)
            mReadMark = itemView.findViewById(R.id.read_mark)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(notification: DeficitBalanceHistory) {
            binding.textBy.text = notification.updatedBy
            binding.textTitle.text = notification.msg
            binding.textDate.text = formatDateTime(notification.updatedAt)
            if (notification.type == "INCREASE") {
                binding.itemImage.setImageResource(R.drawable.ic_up)
            } else {
                binding.itemImage.setImageResource(R.drawable.ic_down)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatDateTime(input: String): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val dateTime = ZonedDateTime.parse(input, formatter)

            val dayFormatter = DateTimeFormatter.ofPattern("EEEE") // Full day name
            val dayOfWeek = dateTime.format(dayFormatter)

            return "${dateTime.toLocalDate()} ${dateTime.toLocalTime()} $dayOfWeek"
        }
    }
}