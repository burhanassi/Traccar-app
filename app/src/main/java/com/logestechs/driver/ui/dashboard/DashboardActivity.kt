package com.logestechs.driver.ui.dashboard

import android.os.Bundle
import com.logestechs.driver.databinding.ActivityDashboardBinding
import com.logestechs.driver.utils.LogesTechsActivity

class DashboardActivity : LogesTechsActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}