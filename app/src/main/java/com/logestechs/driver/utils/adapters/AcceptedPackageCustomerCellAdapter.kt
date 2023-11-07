package com.logestechs.driver.utils.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.databinding.ItemAcceptedPackageCustomerCellBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.bottomSheets.AcceptedPackagesBottomSheet
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import kotlin.contracts.contract


class AcceptedPackageCustomerCellAdapter(
    var customersList: List<Customer?>,
    var context: Context?,
    var fragmentManager: FragmentManager,
    var listener: AcceptedPackagesCardListener?,
    var parentIndex: Int,
    var isSprint: Boolean = false
) :
    RecyclerView.Adapter<AcceptedPackageCustomerCellAdapter.AcceptedPackageCustomerCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): AcceptedPackageCustomerCellViewHolder {
        val inflater =
            ItemAcceptedPackageCustomerCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        inflater.root.layoutParams = ViewGroup.LayoutParams(
            (viewGroup.width * 0.7).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return AcceptedPackageCustomerCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        AcceptedPackageCustomerCellViewHolder: AcceptedPackageCustomerCellViewHolder,
        position: Int
    ) {
        val customer: Customer? = customersList[position]
        AcceptedPackageCustomerCellViewHolder.setIsRecyclable(false)
        AcceptedPackageCustomerCellViewHolder.bind(customer)
    }

    override fun getItemCount(): Int {
        return customersList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    class AcceptedPackageCustomerCellViewHolder(
        private var binding: ItemAcceptedPackageCustomerCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: AcceptedPackageCustomerCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer?) {
            binding.itemSenderName.textItem.text = customer?.getFullName()
            binding.itemSenderAddress.textItem.text = customer?.address?.toStringAddress()
            binding.textCount.text = customer?.packagesNo.toString()

            binding.imageViewCall.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).callMobileNumber(customer?.phone)
                }
            }

            binding.imageViewSms.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendSms(
                        customer?.phone,
                        ""
                    )
                }
            }

            binding.imageViewWhatsApp.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                        Helper.formatNumberForWhatsApp(
                            customer?.phone
                        ), ""
                    )
                }
            }

            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
                binding.imageViewWhatsAppSecondary.visibility = View.VISIBLE
                binding.imageViewWhatsAppSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                            Helper.formatNumberForWhatsApp(
                                customer?.phone,
                                true
                            ), ""
                        )
                    }
                }
            } else {
                binding.imageViewWhatsAppSecondary.visibility = View.GONE
            }

            if (customer?.phone2?.isNotEmpty() == true) {
                binding.imageViewCallSecondary.visibility = View.VISIBLE
                binding.imageViewCallSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).callMobileNumber(customer.phone2)
                    }
                }
            } else {
                binding.imageViewCallSecondary.visibility = View.GONE
            }

            if ((customer?.address?.latitude != null && customer.address.latitude != 0.0) && (customer.address.longitude != null && customer.address.longitude != 0.0)) {
                binding.imageViewLocation.visibility = View.VISIBLE
                binding.imageViewLocation.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).showLocationInGoogleMaps(customer.address)
                    }
                }
            } else {
                binding.imageViewLocation.visibility = View.GONE
            }

            if (mAdapter.companyConfigurations?.isDriverPickupPackagesByScanDisabled == true) {
                binding.buttonScanPackagesBarcodes.visibility = View.GONE
            }

            binding.buttonScanPackagesBarcodes.setOnClickListener {
                mAdapter.listener?.scanForPickup(customer)
            }

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.accepted_pickup_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_show_packages -> {
                                mAdapter.listener?.getAcceptedPackages(customer)
                            }

                            R.id.action_print -> {
//                                mAdapter.listener?.printAwb(customer)
                            }
                        }
                    }
                    true
                }
                popup.show()
            }

            if (mAdapter.isSprint) {
                binding.buttonScanPackagesBarcodes.text =
                    parent.context.getString(R.string.button_scan_packages_barcodes_sprint)
            }

        }
    }
}