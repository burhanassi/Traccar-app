package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemPendingPackageCellBinding
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener

class PendingPackageCellAdapter(
    var packagesList: List<Package?>,
    var context: Context?,
    var listener: PendingPackagesCardListener?,
    var parentIndex: Int
) :
    RecyclerView.Adapter<PendingPackageCellAdapter.PendingPackageViewHolder>() {

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

    class PendingPackageViewHolder(
        private var binding: ItemPendingPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: PendingPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()

            binding.buttonAccept.setOnClickListener {
                mAdapter.listener?.acceptPackage(mAdapter.parentIndex, adapterPosition)
            }


            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)

                popup.inflate(R.menu.pending_customer_packages_context_menu)

                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item?.itemId) {
                        R.id.action_reject_customer_packages -> {
                            mAdapter.listener?.rejectPackage(mAdapter.parentIndex, adapterPosition)
                        }
                    }
                    true
                }

                popup.show()
            }
        }
    }
}