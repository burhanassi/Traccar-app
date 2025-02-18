package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemReturnedPackageCellBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.setThrottleClickListener

class ReturnedBundlesCellAdapter(
    var packagesList: List<Package?>,
    var context: Context?,
    var listener: ReturnedPackagesCardListener?,
    var parentIndex: Int
) :
    RecyclerView.Adapter<ReturnedBundlesCellAdapter.ReturnedBundlesViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ReturnedBundlesViewHolder {
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
        return ReturnedBundlesViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        ReturnedBundlesViewHolder: ReturnedBundlesViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        ReturnedBundlesViewHolder.setIsRecyclable(false)
        ReturnedBundlesViewHolder.bind(pkg)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    class ReturnedBundlesViewHolder(
        private var binding: ItemReturnedPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: ReturnedBundlesCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemPackageBarcode.textItem.text = pkg?.barcode
            binding.itemSenderName.textItem.text = pkg?.getFullReceiverName()

            if (pkg?.invoiceNumber?.isNotEmpty() == true) {
                binding.itemInvoiceNumber.root.visibility = View.VISIBLE
                binding.itemInvoiceNumber.textItem.text = pkg.invoiceNumber
            } else {
                binding.itemInvoiceNumber.root.visibility = View.GONE
            }
            binding.itemSenderAddress.root.visibility = View.GONE
            binding.buttonDeliverToSender.visibility = View.GONE

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }
            binding.itemPackageStatus.root.visibility = View.GONE

        }
    }
}