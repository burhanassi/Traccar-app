package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemPendingPackageCellBinding

class PendingPackageCellAdapter(
    private var packagesList: List<Package?>,
    var context: Context?
) :
    RecyclerView.Adapter<PendingPackageCellAdapter.PendingPackageViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PendingPackageViewHolder {
        val inflater =
            ItemPendingPackageCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        inflater.root.layoutParams = ViewGroup.LayoutParams(
            (viewGroup.width * 0.7).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return PendingPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        pendingPackageViewHolder: PendingPackageViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        pendingPackageViewHolder.setIsRecyclable(false)
        pendingPackageViewHolder.bind(pkg)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    class PendingPackageViewHolder(
        private var binding: ItemPendingPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: PendingPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()
        }
    }
}