package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.*
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemDriverRoutePackagesCellBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.dialogs.*
import com.logestechs.driver.utils.interfaces.*
import java.util.Collections

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
}
class DriverRoutePackagesCellAdapter(
    var packagesList: ArrayList<Package?>,
    var context: Context?,
    var listener: InCarPackagesCardListener?,
    var parentIndex: Int?,
    var isGrouped: Boolean = true
) :
    RecyclerView.Adapter<DriverRoutePackagesCellAdapter.DriverRoutePackagesCellViewHolder>(),
    ItemTouchHelperAdapter {

    val companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var dragStartListener: OnStartDragListener? = null

    fun startDrag(viewHolder: RecyclerView.ViewHolder) {
        dragStartListener?.onStartDrag(viewHolder)
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): DriverRoutePackagesCellViewHolder {
        val inflater =
            ItemDriverRoutePackagesCellBinding.inflate(
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

        return DriverRoutePackagesCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        driverRoutePackagesCellViewHolder: DriverRoutePackagesCellViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        driverRoutePackagesCellViewHolder.setIsRecyclable(false)
        driverRoutePackagesCellViewHolder.bind(pkg, position)
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
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(packagesList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        // Not needed for drag-and-drop reordering
    }
    interface ItemTouchHelperAdapter {
        fun onItemMove(fromPosition: Int, toPosition: Int)
        fun onItemDismiss(position: Int)
    }

    class DriverRoutePackagesCellViewHolder(
        private var binding: ItemDriverRoutePackagesCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: DriverRoutePackagesCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnTouchListener {

        init {
            itemView.setOnTouchListener(this)
        }

        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                mAdapter.startDrag(this)
            }
            return false
        }

        fun bind(pkg: Package?, position: Int) {
            binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
            binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()
            binding.textCod.text = pkg?.cod?.format()

            bindPositionNumber(position)
        }
        fun bindPositionNumber(position: Int) {
            binding.positionNumber.text = (position + 1).toString()
        }
    }
}