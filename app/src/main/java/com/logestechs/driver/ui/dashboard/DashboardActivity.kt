package com.logestechs.driver.ui.dashboard

import android.os.Bundle
import android.view.View
import com.logestechs.driver.R
import com.logestechs.driver.databinding.ActivityDashboardBinding
import com.logestechs.driver.utils.LogesTechsActivity

class DashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dashEntryAcceptedPackages.textCount.text = "55"
        binding.buttonShowDashboardSubEntries.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_show_dashboard_sub_entries -> {
                if (binding.containerDashboardSubEntries.visibility == View.VISIBLE) {
                    binding.containerDashboardSubEntries.visibility = View.GONE
                } else {
                    binding.containerDashboardSubEntries.visibility = View.VISIBLE
                }
            }
        }
    }
}