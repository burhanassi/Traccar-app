package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemDeliveredPackageBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.LogesTechsActivity

class DeliveredPackageCellAdapter(
    private var packagesList: ArrayList<Package?>,
    var context: Context?
) :
    RecyclerView.Adapter<DeliveredPackageCellAdapter.DeliveredPackageViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): DeliveredPackageViewHolder {
        val inflater =
            ItemDeliveredPackageBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return DeliveredPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        InCarPackageViewHolder: DeliveredPackageViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        InCarPackageViewHolder.setIsRecyclable(false);
        InCarPackageViewHolder.bind(pkg)
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

    class DeliveredPackageViewHolder(
        private var binding: ItemDeliveredPackageBinding,
        private var parent: ViewGroup,
        private var mAdapter: DeliveredPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()

            binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
            binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()

            binding.textCod.text = pkg?.cod?.format()

            binding.itemPackageBarcode.textItem.text = pkg?.barcode

            if (pkg?.notes?.trim().isNullOrEmpty()) {
                binding.itemNotes.root.visibility = View.GONE
            } else {
                binding.itemNotes.root.visibility = View.VISIBLE
                binding.itemNotes.textItem.text = pkg?.notes
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

            //Receiver Contact Actions
            binding.imageViewReceiverCall.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).callMobileNumber(pkg?.receiverPhone)
                }
            }

            binding.imageViewReceiverSms.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendSms(
                        pkg?.receiverPhone,
                        Helper.getInterpretedMessageFromTemplate(
                            pkg,
                            false,
                            ""
                        )
                    )
                }
            }

            binding.imageViewReceiverWhatsApp.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                        Helper.formatNumberForWhatsApp(
                            pkg?.receiverPhone
                        ), Helper.getInterpretedMessageFromTemplate(
                            pkg,
                            false,
                            ""
                        )
                    )
                }
            }

            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
                binding.imageViewReceiverWhatsAppSecondary.visibility = View.VISIBLE
                binding.imageViewReceiverWhatsAppSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                            Helper.formatNumberForWhatsApp(
                                pkg?.receiverPhone,
                                true
                            ), Helper.getInterpretedMessageFromTemplate(
                                pkg,
                                false,
                                ""
                            )
                        )
                    }
                }
            } else {
                binding.imageViewReceiverWhatsAppSecondary.visibility = View.GONE
            }

            if (pkg?.receiverPhone2?.isNotEmpty() == true) {
                binding.imageViewReceiverCallSecondary.visibility = View.VISIBLE
                binding.imageViewReceiverCallSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).callMobileNumber(pkg.receiverPhone2)
                    }
                }
            } else {
                binding.imageViewReceiverCallSecondary.visibility = View.GONE
            }

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

            binding.itemPackageBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, pkg?.barcode)
            }
        }
    }
}