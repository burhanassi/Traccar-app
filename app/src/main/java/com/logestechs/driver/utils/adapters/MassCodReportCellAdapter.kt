package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.MassCodReport
import com.logestechs.driver.databinding.ItemMassCodReportBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.interfaces.MassCodReportCardListener

class MassCodReportCellAdapter(
    var massCodReportsList: ArrayList<MassCodReport?>,
    var context: Context?,
    var listener: MassCodReportCardListener?,
) :
    RecyclerView.Adapter<MassCodReportCellAdapter.MassCodReportViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): MassCodReportViewHolder {
        val inflater =
            ItemMassCodReportBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return MassCodReportViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        ReturnedPackageViewHolder: MassCodReportViewHolder,
        position: Int
    ) {
        val massCodReport: MassCodReport? = massCodReportsList[position]
        ReturnedPackageViewHolder.bind(massCodReport)
    }

    override fun getItemCount(): Int {
        return massCodReportsList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    fun clearList() {
        val size: Int = massCodReportsList.size
        massCodReportsList.clear()
        notifyItemRangeRemoved(0, size)
    }

    class MassCodReportViewHolder(
        private var binding: ItemMassCodReportBinding,
        private var parent: ViewGroup,
        private var mAdapter: MassCodReportCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(massCodReport: MassCodReport?) {
            binding.itemPackageBarcode.textItem.text = massCodReport?.barcode
            binding.itemReceiverName.textItem.text = massCodReport?.customerName
            binding.itemReceiverAddress.textItem.text =
                "${massCodReport?.customerVillage ?: ""} - ${massCodReport?.customerCity ?: ""}"

            binding.textCodSum.text = massCodReport?.totalCodWithoutCost?.format()
            binding.textCodPackagesCount.text = massCodReport?.codPackagesNumber.toString()

            binding.imageViewReceiverCall.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).callMobileNumber(massCodReport?.customerPhone)
                }
            }

            binding.imageViewReceiverSms.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendSms(
                        massCodReport?.customerPhone,
                        Helper.getInterpretedMessageFromTemplate(
                            massCodReport,
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
                            massCodReport?.customerPhone
                        ), Helper.getInterpretedMessageFromTemplate(
                            massCodReport,
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
                                massCodReport?.customerPhone,
                                true
                            ), Helper.getInterpretedMessageFromTemplate(
                                massCodReport,
                                false,
                                ""
                            )
                        )
                    }
                }
            } else {
                binding.imageViewReceiverWhatsAppSecondary.visibility = View.GONE
            }

            binding.buttonDeliver.setOnClickListener {
                mAdapter.listener?.onDeliverMassReport(adapterPosition)
            }
        }
    }
}