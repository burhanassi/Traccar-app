package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemPendingPackageCellBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.dialogs.RejectPackageDialog
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener
import com.logestechs.driver.utils.interfaces.RejectPackageDialogListener
import com.logestechs.driver.utils.setThrottleClickListener

class PendingPackageCellAdapter(
    var packagesList: List<Package?>,
    var context: Context?,
    var listener: PendingPackagesCardListener?,
    var rejectPackageDialogListener: RejectPackageDialogListener?,
    var parentIndex: Int
) :
    RecyclerView.Adapter<PendingPackageCellAdapter.PendingPackageViewHolder>(), RejectPackageDialogListener {
    val companyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var rejectedPackagePosition: Int = 0
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
        pendingPackageViewHolder.setIsRecyclable(false);
        pendingPackageViewHolder.bind(pkg)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    override fun onPackageRejected(rejectPackageRequestBody: RejectPackageRequestBody) {
        listener?.rejectPackage(parentIndex, rejectedPackagePosition, rejectPackageRequestBody)
    }

    class PendingPackageViewHolder(
        private var binding: ItemPendingPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: PendingPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemPackageBarcode.textItem.text = pkg?.barcode
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }

            binding.buttonAccept.setThrottleClickListener({
                mAdapter.listener?.acceptPackage(mAdapter.parentIndex, adapterPosition)
            })

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.pending_package_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item?.itemId) {
                        R.id.action_reject_package -> {
//                            mAdapter.listener?.rejectPackage(mAdapter.parentIndex, adapterPosition)
                            mAdapter.rejectedPackagePosition = adapterPosition
                            RejectPackageDialog(
                                mAdapter.context!!,
                                mAdapter
                            ).showDialog()

                        }
                    }
                    true
                }

                popup.show()
            }

            if (mAdapter.companyConfigurations?.isAllowDriverRejectingOrders == false) {
                binding.buttonContextMenu.visibility = View.GONE
            } else {
                binding.buttonContextMenu.visibility = View.VISIBLE
            }
        }
    }
}