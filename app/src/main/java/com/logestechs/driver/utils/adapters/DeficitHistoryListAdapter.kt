package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DeficitBalanceHistory
import com.logestechs.driver.databinding.ItemDeficitsHistoryBinding


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

        fun bind(notification: DeficitBalanceHistory) {
            binding.textBy.text = notification.updatedBy
            binding.textTitle.text = notification.msg
            binding.textDate.text = notification.updatedAt
        }
    }
}