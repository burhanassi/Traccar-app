package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemInCarPackageCellBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.dialogs.ChangeCodDialog
import com.logestechs.driver.utils.dialogs.ChangePackageTypeDialog
import com.logestechs.driver.utils.dialogs.FailDeliveryDialog
import com.logestechs.driver.utils.dialogs.PostponePackageDialog
import com.logestechs.driver.utils.interfaces.BroughtPackagesCardListener
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener

class BroughtPackageAdapter (
    var packagesList: ArrayList<Package?>,
    var context: Context?,
    var listener: BroughtPackagesCardListener?,
    var parentIndex: Int?,
    var isGrouped: Boolean = true
) :
    RecyclerView.Adapter<BroughtPackageAdapter.BroughtPackageViewHolder>(){
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): BroughtPackageAdapter.BroughtPackageViewHolder {
        val inflater =
            ItemInCarPackageCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        if (isGrouped) {
            inflater.root.layoutParams = ViewGroup.LayoutParams(
                (viewGroup.width * 0.7).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return BroughtPackageAdapter.BroughtPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(holder: BroughtPackageViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    class BroughtPackageViewHolder(
        private var binding: ItemInCarPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: BroughtPackageAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()

            binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
            binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()

            binding.textCod.text = pkg?.cod?.format()

            if (pkg?.shipmentType != null) {
                binding.itemShipmentType.root.visibility = View.VISIBLE

                when (pkg.shipmentType) {
                    PackageType.COD.name -> {
                        binding.itemShipmentType.textItem.text =
                            mAdapter.context?.getString(R.string.shipment_type_cod)
                    }
                    PackageType.REGULAR.name -> {
                        binding.itemShipmentType.textItem.text =
                            mAdapter.context?.getString(R.string.shipment_type_regular)
                    }
                    PackageType.SWAP.name -> {
                        binding.itemShipmentType.textItem.text =
                            mAdapter.context?.getString(R.string.shipment_type_swap)
                    }
                    PackageType.BRING.name -> {
                        binding.itemShipmentType.textItem.text =
                            mAdapter.context?.getString(R.string.shipment_type_bring)
                    }
                    else -> {
                        binding.itemShipmentType.root.visibility = View.GONE
                    }
                }
            } else {
                binding.itemShipmentType.root.visibility = View.GONE
            }

            binding.itemPackageBarcode.textItem.text = pkg?.barcode

            if (pkg?.notes?.trim().isNullOrEmpty()) {
                binding.itemNotes.root.visibility = View.GONE
            } else {
                binding.itemNotes.root.visibility = View.VISIBLE
                binding.itemNotes.textItem.text = pkg?.notes
            }

            if (pkg?.invoiceNumber?.trim().isNullOrEmpty()) {
                binding.itemInvoiceNumber.root.visibility = View.GONE
            } else {
                binding.itemInvoiceNumber.root.visibility = View.VISIBLE
                binding.itemInvoiceNumber.textItem.text = pkg?.invoiceNumber
            }

            if (pkg?.quantity != null && pkg.quantity != 0) {
                binding.itemPackageQuantity.root.visibility = View.VISIBLE
                binding.itemPackageQuantity.textItem.text = pkg.quantity.toString()
            } else {
                binding.itemPackageQuantity.root.visibility = View.GONE
            }

            //Sender Contact Actions
            binding.imageViewSenderCall.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).callMobileNumber(pkg?.senderPhone)
                }
            }

            binding.imageViewSenderSms.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendSms(
                        pkg?.senderPhone,
                        ""
                    )
                }
            }

            binding.imageViewSenderWhatsApp.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                        Helper.formatNumberForWhatsApp(
                            pkg?.senderPhone
                        ), ""
                    )
                }
            }

            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
                binding.imageViewSenderWhatsAppSecondary.visibility = View.VISIBLE
                binding.imageViewSenderWhatsAppSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                            Helper.formatNumberForWhatsApp(
                                pkg?.senderPhone,
                                true
                            ), ""
                        )
                    }
                }
            } else {
                binding.imageViewSenderWhatsAppSecondary.visibility = View.GONE
            }

            if (pkg?.senderPhone2?.isNotEmpty() == true) {
                binding.imageViewSenderCallSecondary.visibility = View.VISIBLE
                binding.imageViewSenderCallSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).callMobileNumber(pkg.senderPhone2)
                    }
                }
            } else {
                binding.imageViewSenderCallSecondary.visibility = View.GONE
            }

//            //Receiver Contact Actions
//            binding.imageViewReceiverCall.setOnClickListener {
//                mAdapter.listener?.onCallReceiver(pkg, pkg?.receiverPhone)
//            }
//
//            binding.imageViewReceiverSms.setOnClickListener {
//                mAdapter.listener?.onSendSmsMessage(pkg)
//            }
//
//            binding.imageViewReceiverWhatsApp.setOnClickListener {
//                mAdapter.listener?.onSendWhatsAppMessage(pkg)
//            }
//
//            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
//                binding.imageViewReceiverWhatsAppSecondary.visibility = View.VISIBLE
//                binding.imageViewReceiverWhatsAppSecondary.setOnClickListener {
//                    mAdapter.listener?.onSendWhatsAppMessage(pkg, true)
//                }
//            } else {
//                binding.imageViewReceiverWhatsAppSecondary.visibility = View.GONE
//            }
//
//            if (pkg?.receiverPhone2?.isNotEmpty() == true) {
//                binding.imageViewReceiverCallSecondary.visibility = View.VISIBLE
//                binding.imageViewReceiverCallSecondary.setOnClickListener {
//                    mAdapter.listener?.onCallReceiver(pkg, pkg.receiverPhone2)
//                }
//            } else {
//                binding.imageViewReceiverCallSecondary.visibility = View.GONE
//            }

            if (pkg?.destinationAddress != null) {
                if (pkg.destinationAddress!!.latitude != 0.0 || pkg.destinationAddress!!.longitude != 0.0) {
                    binding.imageViewReceiverLocation.visibility = View.VISIBLE
                    binding.imageViewReceiverLocation.setOnClickListener {
                        if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                            (mAdapter.context as LogesTechsActivity).showLocationInGoogleMaps(pkg.destinationAddress)
                        }
                    }
                } else {
                    binding.imageViewReceiverLocation.visibility = View.GONE
                }
            } else {
                binding.imageViewReceiverLocation.visibility = View.GONE
            }

//            binding.buttonContextMenu.setOnClickListener {
//                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
//                popup.inflate(R.menu.in_car_package_context_menu)
//                popup.setOnMenuItemClickListener { item: MenuItem? ->
//
//                    if (mAdapter.context != null) {
//                        when (item?.itemId) {
//                            R.id.action_return_package -> {
//                                mAdapter.listener?.onShowReturnPackageDialog(pkg)
//                            }
//
//                            R.id.action_postpone_package -> {
//                                PostponePackageDialog(
//                                    mAdapter.context!!,
//                                    mAdapter,
//                                    pkg
//                                ).showDialog()
//                            }
//                            R.id.action_view_attachment -> {
//                                mAdapter.listener?.onShowAttachmentsDialog(pkg)
//                            }
//                            R.id.action_edit_package_type -> {
//                                ChangePackageTypeDialog(
//                                    mAdapter.context!!,
//                                    mAdapter,
//                                    pkg
//                                ).showDialog()
//                            }
//                            R.id.action_fail_delivery -> {
//                                FailDeliveryDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
//                            }
//
//                            R.id.action_add_note -> {
//                                mAdapter.listener?.onShowPackageNoteDialog(pkg)
//                            }
//
//                            R.id.action_edit_package_cod -> {
//                                ChangeCodDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
//                            }
//                        }
//                    }
//                    true
//                }
//                if (mAdapter.companyConfigurations?.isDriverCanRequestCodChange != true) {
//                    popup.menu.findItem(R.id.action_edit_package_cod).isVisible = false
//                }
//                if (mAdapter.companyConfigurations?.isDriverCanReturnPackage != true) {
//                    popup.menu.findItem(R.id.action_return_package).isVisible = false
//                }
//                if (mAdapter.companyConfigurations?.isDriverCanFailPackageDisabled == true) {
//                    popup.menu.findItem(R.id.action_fail_delivery).isVisible = false
//                }
//                popup.show()
//            }

//            binding.buttonDeliverPackage.setOnClickListener {
//                mAdapter.listener?.onDeliverPackage(pkg)
//            }

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }

            binding.itemInvoiceNumber.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.invoiceNumber)
            }
        }
    }
}