package com.logestechs.driver.ui.driverDraftPickupsByStatusViewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.logestechs.driver.ui.acceptedDraftPickups.AcceptedDraftPickupsFragment
import com.logestechs.driver.ui.inCarDraftPickups.InCarDraftPickupsFragment
import com.logestechs.driver.ui.pendingDraftPickups.PendingDraftPickupsFragment

class DraftPickupsByStatusViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PendingDraftPickupsFragment()
            }
            1 -> {
                AcceptedDraftPickupsFragment()
            }
            2 -> {
                InCarDraftPickupsFragment()
            }
            else -> {
                PendingDraftPickupsFragment()
            }
        }
    }

}