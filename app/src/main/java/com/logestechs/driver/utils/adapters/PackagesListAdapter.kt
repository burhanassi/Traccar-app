package com.logestechs.driver.utils.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemAcceptedPackagesBinding
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.bottomSheets.PackageTrackBottomSheet
import com.logestechs.driver.utils.interfaces.PackagesListCardListener


class PackagesListAdapter(
    val list: ArrayList<Package>,
    var listener: PackagesListCardListener,
    private val fragmentManager: FragmentManager
    ) :
    RecyclerView.Adapter<PackagesListAdapter.PackagesViewHolder>() {
    var mContext: Context? = null

    val companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PackagesViewHolder {
        mContext = viewGroup.context
        val inflater =
            ItemAcceptedPackagesBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return PackagesViewHolder(
            inflater,
            viewGroup,
            this
        )
    }

    override fun onBindViewHolder(holder: PackagesViewHolder, position: Int) {
        val packages: Package = list[position]
        holder.bind(packages)
    }

    override fun getItemCount(): Int = list.size

    fun update(shipmentsList: ArrayList<Package>) {
        list.addAll(shipmentsList)
        this.notifyDataSetChanged()
    }

    class PackagesViewHolder(
        private var  binding: ItemAcceptedPackagesBinding,
        private var parent: ViewGroup,
        private var mAdapter: PackagesListAdapter
    ) :
        RecyclerView.ViewHolder(binding.root){
        val driverCompanyConfigurations =
            SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations
        fun bind(pkg: Package) {
            binding.textBarcode.text = pkg.barcode
            binding.textTitle.text =  pkg.getFullReceiverName()
            binding.textAddress.text =  pkg.destinationAddress?.city

            if (mAdapter.companyConfigurations?.isShowPackageContentForDrivers == true) {
                binding.buttonShowPackageContent.setOnClickListener {
                    mAdapter.listener.onShowPackageContent(pkg)
                }
            }

            if(driverCompanyConfigurations?.isDriverPickupAcceptedPackages!! || pkg.shipmentType == PackageType.BRING.name){
                binding.buttonPickup.setOnClickListener {
                    mAdapter.listener.onPickupPackage(pkg.barcode!!)
                }
            }else {
                binding.buttonPickup.visibility = View.GONE
            }
            binding.buttonAddNote.setOnClickListener {
                mAdapter.listener.onShowPackageNoteDialog(pkg)
            }
            binding.buttonInfo.setOnClickListener {
                val bottomSheet = PackageTrackBottomSheet()
                val bundle = Bundle()
                bundle.putParcelable(BundleKeys.PKG_KEY.toString(), pkg)
                bottomSheet.arguments = bundle
                bottomSheet.show(mAdapter.fragmentManager, "exampleBottomSheet")
            }
        }
    }
}