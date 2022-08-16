package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.*
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemInCarPackageCellBinding
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.dialogs.*
import com.logestechs.driver.utils.interfaces.*


class InCarPackageCellAdapter(
    var packagesList: ArrayList<Package?>,
    var context: Context?,
    var listener: InCarPackagesCardListener?,
    var parentIndex: Int?,
    var isGrouped: Boolean = true
) :
    RecyclerView.Adapter<InCarPackageCellAdapter.InCarPackageCellViewHolder>(),
    ReturnPackageDialogListener,
    FailDeliveryDialogListener,
    PostponePackageDialogListener,
    ChangePackageTypeDialogListener,
    AddPackageNoteDialogListener,
    ChangeCodDialogListener {

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
        return InCarPackageCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        InCarPackageViewHolder: InCarPackageCellViewHolder,
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

    class InCarPackageCellViewHolder(
        private var binding: ItemInCarPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarPackageCellAdapter
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

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.in_car_package_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_return_package -> {
                                ReturnPackageDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
                            }

                            R.id.action_postpone_package -> {
                                PostponePackageDialog(
                                    mAdapter.context!!,
                                    mAdapter,
                                    pkg
                                ).showDialog()
                            }

                            R.id.action_edit_package_type -> {
                                ChangePackageTypeDialog(
                                    mAdapter.context!!,
                                    mAdapter,
                                    pkg
                                ).showDialog()
                            }
                            R.id.action_fail_delivery -> {
                                FailDeliveryDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
                            }

                            R.id.action_add_note -> {
                                AddPackageNoteDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
                            }

                            R.id.action_edit_package_cod -> {
                                ChangeCodDialog(mAdapter.context!!, mAdapter, pkg).showDialog()
                            }
                        }
                    } else {

                    }
                    true
                }

                popup.show()
            }
        }
    }

    override fun onPackageReturned(returnPackageRequestBody: ReturnPackageRequestBody?) {
        listener?.onPackageReturned(returnPackageRequestBody)
    }

    override fun onFailDelivery(body: FailDeliveryRequestBody?) {
        listener?.onFailDelivery(body)
    }

    override fun onPackagePostponed(postponePackageRequestBody: PostponePackageRequestBody) {
        listener?.onPackagePostponed(postponePackageRequestBody)
    }

    override fun onPackageTypeChanged(changePackageTypeRequestBody: ChangePackageTypeRequestBody) {
        listener?.onPackageTypeChanged(changePackageTypeRequestBody)
    }

    override fun onPackageNoteAdded(addNoteRequestBody: AddNoteRequestBody?) {
        listener?.onPackageNoteAdded(addNoteRequestBody)
    }

    override fun onCodChanged(codChangeRequestBody: CodChangeRequestBody?) {
        listener?.onCodChanged(codChangeRequestBody)
    }
}