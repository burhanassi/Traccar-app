package com.logestechs.driver.utils.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Notification
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemAcceptedPackageCustomerCellBinding
import com.logestechs.driver.databinding.ItemAcceptedPackagesBinding
import com.logestechs.driver.databinding.ItemNotificationBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import com.logestechs.driver.utils.interfaces.AcceptedPackagesFragmentListener


class PackagesListAdapter(
    val list: ArrayList<Package>,
    var listener: AcceptedPackagesFragmentListener?
    ) :
    RecyclerView.Adapter<PackagesListAdapter.PackagesViewHolder>() {
    lateinit var mContext: Context
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PackagesViewHolder {
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

        fun bind(packages: Package) {
            binding.textTitle.text =  packages.getFullReceiverName()
            binding.textAddress.text =  packages.destinationAddress?.city

            binding.buttonPickup?.setOnClickListener {
                mAdapter.listener?.callPickupPackageFromFragment(packages.barcode!!)
            }
        }
    }
}