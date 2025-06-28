package com.logestechs.traccarApp.utils.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class DropdownListRecycler : RecyclerView {
    private val ITEM_HEIGHT = 40
    var isExpanded = false
    val maxItems = 5

    constructor(context: Context) :
            super(context)


    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    fun expand(numberOfItems: Int) {
        if (numberOfItems == 0) {
            collapse()
        } else {
            this.visibility = View.VISIBLE
            val height: Int = if (numberOfItems > maxItems) {
                (maxItems * ITEM_HEIGHT)
            } else {
                (numberOfItems * ITEM_HEIGHT)
            }
            val heightInPixels: Float = height * context.resources.displayMetrics.density
            this.layoutParams.height = (heightInPixels.toInt() + 60)
            isExpanded = true
        }
    }

    fun collapse() {
        this.visibility = View.GONE
        isExpanded = false
    }
}