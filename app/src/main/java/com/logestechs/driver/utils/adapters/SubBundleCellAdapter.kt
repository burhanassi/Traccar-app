package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.SubBundle
import com.logestechs.driver.databinding.ItemSubBundleBinding

class SubBundleCellAdapter(
    var subBundlesList: ArrayList<SubBundle?>,
    var context: Context?,
) : RecyclerView.Adapter<SubBundleCellAdapter.SubBundlesCellViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): SubBundleCellAdapter.SubBundlesCellViewHolder {
        val inflater =
            ItemSubBundleBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return SubBundleCellAdapter.SubBundlesCellViewHolder(inflater, this)
    }

    override fun onBindViewHolder(
        subBundlesCellViewHolder: SubBundleCellAdapter.SubBundlesCellViewHolder,
        position: Int
    ) {
        val subBundle: SubBundle? = subBundlesList[position]
        subBundlesCellViewHolder.setIsRecyclable(false)
        subBundlesCellViewHolder.bind(subBundle)
    }

    override fun getItemCount(): Int {
        return subBundlesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<SubBundle?>) {
        this.subBundlesList.clear()
        this.subBundlesList.addAll(list)
        this.notifyDataSetChanged()
    }

    class SubBundlesCellViewHolder(
        private var binding: ItemSubBundleBinding,
        private var mAdapter: SubBundleCellAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subBundle: SubBundle?) {
            binding.itemSkuBundle.text = subBundle?.sku
            binding.itemBundleName.text = subBundle?.name
            binding.itemBundleQuantity.text = subBundle?.subProductQuantity.toString()

            val layoutParams = binding.root.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.root.layoutParams = layoutParams
        }
    }
}
