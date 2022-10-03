package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.DropdownItem
import com.logestechs.driver.utils.DropdownTag

interface OnDropDownItemClickListener {
    fun onItemClick(item: DropdownItem, tag: DropdownTag)
}