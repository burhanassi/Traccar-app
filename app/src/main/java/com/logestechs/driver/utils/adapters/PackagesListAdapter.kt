package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemAcceptedPackagesBinding
import com.logestechs.driver.utils.dialogs.AddPackageNoteDialog
import com.logestechs.driver.utils.interfaces.AcceptedPackagesFragmentListener
import com.logestechs.driver.utils.interfaces.AddPackageNoteDialogListener
import com.logestechs.driver.utils.interfaces.PackagesListCardListener


class PackagesListAdapter(
    val list: ArrayList<Package>,
    var listener: PackagesListCardListener
    ) :
    RecyclerView.Adapter<PackagesListAdapter.PackagesViewHolder>() {
    var mContext: Context? = null
    var loadedImagesList: java.util.ArrayList<LoadedImage> = java.util.ArrayList()
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

        fun bind(packages: Package) {
            binding.textTitle.text =  packages.getFullReceiverName()
            binding.textAddress.text =  packages.destinationAddress?.city

            val context = mAdapter.mContext
//            val noteListener = mAdapter.noteListener

            binding.buttonPickup?.setOnClickListener {
                mAdapter.listener?.onPickupPackage(packages.barcode!!)
            }
            binding.buttonAddNote?.setOnClickListener {
                mAdapter.listener.onShowPackageNoteDialog(packages)
            }
        }
    }
}