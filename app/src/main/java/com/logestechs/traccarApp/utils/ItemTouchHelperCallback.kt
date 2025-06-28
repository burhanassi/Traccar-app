package com.logestechs.traccarApp.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min
import kotlin.math.max


class ItemTouchHelperCallback() :
    ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        val startPosition = min(fromPosition, toPosition)
        val endPosition = max(fromPosition, toPosition)


        for (position in startPosition..endPosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.translationY = 0f
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not needed for drag-and-drop reordering
    }
}


