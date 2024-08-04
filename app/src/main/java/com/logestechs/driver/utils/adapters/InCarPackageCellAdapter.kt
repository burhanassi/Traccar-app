package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.*
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemInCarPackageCellBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.Helper.Companion.formatServerDate
import com.logestechs.driver.utils.dialogs.*
import com.logestechs.driver.utils.interfaces.*


class InCarPackageCellAdapter(
    var packagesList: ArrayList<Package?>,
    var context: Context?,
    var listener: InCarPackagesCardListener?,
    var parentIndex: Int?,
    var isGrouped: Boolean = true,
    var isSprint: Boolean = false
) :
    RecyclerView.Adapter<InCarPackageCellAdapter.InCarPackageCellViewHolder>(),
    ChangePackageTypeDialogListener,
    ChangeCodDialogListener,
    ChangePackageWeightDialogListener {

    val companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): InCarPackageCellViewHolder {
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
        if (loginResponse?.user?.companyID == 240.toLong() || loginResponse?.user?.companyID == 313.toLong()) {
            isSprint = true
        }
        return InCarPackageCellViewHolder(inflater, viewGroup, this, context!!)
    }

    override fun onBindViewHolder(
        InCarPackageViewHolder: InCarPackageCellViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        InCarPackageViewHolder.setIsRecyclable(false);
        InCarPackageViewHolder.bind(pkg, position)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Package?>) {
        this.packagesList.clear()
        this.packagesList.addAll(list)
        this.notifyDataSetChanged()
    }

    class InCarPackageCellViewHolder(
        private var binding: ItemInCarPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarPackageCellAdapter,
        private var context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            pkg: Package?,
            position: Int
        ) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()
            if (pkg?.serviceTypeName != null && pkg.serviceTypeName!!.isNotEmpty()) {
                binding.containerServiceType.visibility = View.VISIBLE
                binding.serviceType.text = pkg.serviceTypeName
            }

            if (mAdapter.loginResponse?.user?.isHideSenderInfo == true) {
                if (mAdapter.loginResponse?.user?.isShowSenderPhone == false) {
                    binding.containerSenderPhoneNumber.visibility = View.GONE
                }
            }

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

            if (pkg?.supplierInvoice?.trim().isNullOrEmpty()) {
                binding.itemSupplierInvoiceNumber.root.visibility = View.GONE
            } else {
                binding.itemSupplierInvoiceNumber.root.visibility = View.VISIBLE
                binding.itemSupplierInvoiceNumber.textItem.text = pkg?.supplierInvoice
            }

            if (pkg?.quantity != null && pkg.quantity != 0) {
                binding.itemPackageQuantity.root.visibility = View.VISIBLE
                binding.itemPackageQuantity.textItem.text = pkg.quantity.toString()
            } else {
                binding.itemPackageQuantity.root.visibility = View.GONE
            }

            if (pkg?.pickupDate == null || pkg.pickupDate!!.isEmpty()) {
                binding.itemPickupDate.root.visibility = View.GONE
            } else {
                binding.itemPickupDate.root.visibility = View.VISIBLE
                binding.itemPickupDate.textItem.text = formatServerDate(
                    pkg.pickupDate.toString(),
                    DateFormats.MESSAGE_TEMPLATE_WITH_TIME
                )
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

            //Receiver Contact Actions
            binding.imageViewReceiverCall.setOnClickListener {
                mAdapter.listener?.onCallReceiver(pkg, pkg?.receiverPhone)
            }

            binding.imageViewReceiverSms.setOnClickListener {
                mAdapter.listener?.onSendSmsMessage(pkg)
            }

            binding.imageViewReceiverWhatsApp.setOnClickListener {
                mAdapter.listener?.onSendWhatsAppMessage(pkg)
            }

            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
                binding.imageViewReceiverWhatsAppSecondary.visibility = View.VISIBLE
                binding.imageViewReceiverWhatsAppSecondary.setOnClickListener {
                    mAdapter.listener?.onSendWhatsAppMessage(pkg, true)
                }
            } else {
                binding.imageViewReceiverWhatsAppSecondary.visibility = View.GONE
            }

            if (pkg?.receiverPhone2?.isNotEmpty() == true) {
                binding.imageViewReceiverCallSecondary.visibility = View.VISIBLE
                binding.imageViewReceiverCallSecondary.setOnClickListener {
                    mAdapter.listener?.onCallReceiver(pkg, pkg.receiverPhone2)
                }
            } else {
                binding.imageViewReceiverCallSecondary.visibility = View.GONE
            }

            if (pkg?.destinationAddress != null) {
                if (pkg.destinationAddress!!.latitude != 0.0 || pkg.destinationAddress!!.longitude != 0.0) {
                    binding.imageViewReceiverLocation.visibility = View.VISIBLE
                    binding.imageViewReceiverLocation.setOnClickListener {
                        if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                            (mAdapter.context as LogesTechsActivity).showNavigationOptionsDialog(
                                mAdapter.context as LogesTechsActivity,
                                pkg.destinationAddress
                            )
                        }
                    }
                } else {
                    binding.imageViewReceiverLocation.visibility = View.GONE
                }
                if (Helper.getCompanyCurrency() == AppCurrency.SAR.value &&
                    pkg.destinationAddress!!.nationalAddress != null &&
                    pkg.destinationAddress!!.nationalAddress?.isNotEmpty()!!) {
                    binding.imageViewReceiverLocationKsa.visibility = View.VISIBLE
                    binding.imageViewReceiverLocationKsa.setOnClickListener {
                        if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                            (mAdapter.context as LogesTechsActivity).showNavigationOptionsDialog(
                                mAdapter.context as LogesTechsActivity,
                                pkg.destinationAddress,
                                true
                            )
                        }
                    }
                } else {
                    binding.imageViewReceiverLocationKsa.visibility = View.GONE
                }
            } else {
                binding.imageViewReceiverLocation.visibility = View.GONE
                binding.imageViewReceiverLocationKsa.visibility = View.GONE
            }

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.in_car_package_context_menu)
                if(mAdapter.companyConfigurations?.isAllowDriversToViewAttachments == false){
                    popup.menu.findItem(R.id.action_view_attachment)?.isVisible = false
                }
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_return_package -> {
                                mAdapter.listener?.onShowReturnPackageDialog(pkg)
                            }

                            R.id.action_postpone_package -> {
                                mAdapter.listener?.onShowPostponePackageDialog(pkg)
                            }
                            R.id.action_view_attachment -> {
                                mAdapter.listener?.onShowAttachmentsDialog(pkg)
                            }
                            R.id.action_edit_package_type -> {
                                ChangePackageTypeDialog(
                                    mAdapter.context!!,
                                    mAdapter,
                                    pkg
                                ).showDialog()
                            }
                            R.id.action_edit_package_weight -> {
                                ChangePackageWeightDialog(
                                    mAdapter.context!!,
                                    mAdapter,
                                    pkg
                                ).showDialog()
                            }
                            R.id.action_fail_delivery -> {
                                mAdapter.listener?.onShowFailDeliveryDialog(pkg)
                            }

                            R.id.action_add_note -> {
                                mAdapter.listener?.onShowPackageNoteDialog(pkg)
                            }

                            R.id.action_edit_package_cod -> {
                                ChangeCodDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
                            }

                            R.id.action_show_package_content -> {
                                ShowPackageContentDialog(mAdapter.context!!, pkg?.description).showDialog()
                            }
                        }
                    }
                    true
                }
                if (mAdapter.companyConfigurations?.isDriverCanRequestCodChange != true) {
                    popup.menu.findItem(R.id.action_edit_package_cod).isVisible = false
                }
                if (mAdapter.companyConfigurations?.isDriverCanReturnPackage != true) {
                    popup.menu.findItem(R.id.action_return_package).isVisible = false
                }
                if (mAdapter.companyConfigurations?.isDriverCanFailPackageDisabled == true) {
                    popup.menu.findItem(R.id.action_fail_delivery).isVisible = false
                }
                if (mAdapter.companyConfigurations?.isAllowDriversToChangePkgWeight == false) {
                    popup.menu.findItem(R.id.action_edit_package_weight).isVisible = false
                }
                if (mAdapter.companyConfigurations?.isShowPackageContentForDrivers == false ||
                    !pkg?.description.isNullOrEmpty()
                    ) {
                    popup.menu.findItem(R.id.action_show_package_content).isVisible = false
                }
                if (mAdapter.isSprint) {
                    popup.menu.findItem(R.id.action_edit_package_type).title =
                        mAdapter.context!!.getString(R.string.change_package_type_sprint)
                    popup.menu.findItem(R.id.action_add_note).isVisible = false
                }
                popup.show()
            }

            binding.buttonDeliverPackage.setOnClickListener {
                mAdapter.listener?.onDeliverPackage(pkg, position)
            }

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }

            binding.itemInvoiceNumber.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.invoiceNumber)
            }

            if (mAdapter.companyConfigurations?.isPreventDriversDeliveredPickupPackages == true &&
                (pkg?.shipmentType == PackageType.BRING.name || pkg?.pickupDate == pkg?.firstPickupDate)
            ) {
                binding.buttonsContainer.visibility = View.GONE
            }
        }
    }

    override fun onPackageTypeChanged(changePackageTypeRequestBody: ChangePackageTypeRequestBody) {
        listener?.onPackageTypeChanged(changePackageTypeRequestBody)
    }

    override fun onCodChanged(codChangeRequestBody: CodChangeRequestBody?) {
        listener?.onCodChanged(codChangeRequestBody)
    }

    override fun onPackageWeightChanged(packageId: Long?, body: ChangePackageWeightRequestBody) {
        listener?.onPackageWeightChanged(packageId, body)
    }
}