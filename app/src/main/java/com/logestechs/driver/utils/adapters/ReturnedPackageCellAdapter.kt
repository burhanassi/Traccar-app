package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemReturnedPackageCellBinding
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.setThrottleClickListener

class ReturnedPackageCellAdapter(
    var packagesList: List<Package?>,
    var context: Context?,
    var listener: ReturnedPackagesCardListener?,
    var parentIndex: Int
) :
    RecyclerView.Adapter<ReturnedPackageCellAdapter.ReturnedPackageViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ReturnedPackageViewHolder {
        val inflater =
            ItemReturnedPackageCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        inflater.root.layoutParams = ViewGroup.LayoutParams(
            (viewGroup.width * 0.7).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ReturnedPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        ReturnedPackageViewHolder: ReturnedPackageViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        ReturnedPackageViewHolder.setIsRecyclable(false)
        ReturnedPackageViewHolder.bind(pkg)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    class ReturnedPackageViewHolder(
        private var binding: ItemReturnedPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: ReturnedPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemPackageBarcode.textItem.text = pkg?.barcode
            binding.itemSenderName.textItem.text = pkg?.getFullReceiverName()
            binding.itemSenderAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()

            binding.buttonDeliverToSender.setThrottleClickListener({
                mAdapter.listener?.deliverPackage(mAdapter.parentIndex, adapterPosition)
            })

        }
    }
}