package com.logestechs.driver.ui.driverPackagesByStatusViewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.logestechs.driver.ui.acceptedPackages.AcceptedPackagesFragment
import com.logestechs.driver.ui.deliveredPackages.DeliveredPackagesFragment
import com.logestechs.driver.ui.inCarPackages.InCarPackagesFragment
import com.logestechs.driver.ui.pendingPackages.PendingPackagesFragment

class PackagesByStatusViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PendingPackagesFragment()
            }
            1 -> {
                AcceptedPackagesFragment()
            }
            2 -> {
                InCarPackagesFragment()
            }
            3 -> {
                DeliveredPackagesFragment()
            }
            else -> {
                PendingPackagesFragment()
            }
        }
    }

}