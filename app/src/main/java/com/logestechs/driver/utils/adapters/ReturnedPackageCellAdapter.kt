package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemReturnedPackageCellBinding
import com.logestechs.driver.utils.AdminPackageStatus
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.setThrottleClickListener
import com.yariksoffice.lingver.Lingver

class ReturnedPackageCellAdapter(
    var packagesList: List<Package?>,
    var context: Context?,
    var listener: ReturnedPackagesCardListener?,
    var parentIndex: Int
) :
    RecyclerView.Adapter<ReturnedPackageCellAdapter.ReturnedPackageViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

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

            if (mAdapter.companyConfigurations?.isEnablePinCodeForMassCodReportsAndMassReturnedPackages!!) {
                binding.buttonsContainer.visibility = View.GONE
            }

            val statusText = when (Lingver.getInstance().getLocale().toString()) {
                AppLanguages.ARABIC.value -> pkg?.status?.arabic
                else -> pkg?.status?.english
            }
            binding.itemPackageStatus.textItem.text = statusText
            if (pkg?.invoiceNumber?.isNotEmpty() == true) {
                binding.itemInvoiceNumber.root.visibility = View.VISIBLE
                binding.itemInvoiceNumber.textItem.text = pkg.invoiceNumber
            } else {
                binding.itemInvoiceNumber.root.visibility = View.GONE
            }

            binding.buttonDeliverToSender.setThrottleClickListener({
                mAdapter.listener?.deliverPackage(mAdapter.parentIndex, adapterPosition)
            })

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }

        }
    }
}