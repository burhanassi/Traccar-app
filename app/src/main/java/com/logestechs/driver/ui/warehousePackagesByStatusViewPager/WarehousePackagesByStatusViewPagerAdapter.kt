package com.logestechs.driver.ui.warehousePackagesByStatusViewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.logestechs.driver.ui.warehousePackagesFragments.ArrivedShippingPlansFragment
import com.logestechs.driver.ui.warehousePackagesFragments.AssignedShippingPlansFragment
import com.logestechs.driver.ui.warehousePackagesFragments.InCarShippingPlansFragment
import com.logestechs.driver.ui.warehousePackagesFragments.ReturnedShippingPlansFragment

class WarehousePackagesByStatusViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AssignedShippingPlansFragment()
            }
            1 -> {
                InCarShippingPlansFragment()
            }
            2 -> {
                ArrivedShippingPlansFragment()
            }
            3 -> {
                ReturnedShippingPlansFragment()
            }
            else -> {
                AssignedShippingPlansFragment()
            }
        }
    }

}