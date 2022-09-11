package com.logestechs.driver.ui.returnedPackages

import android.os.Bundle
import com.logestechs.driver.databinding.ActivityReturnedPackagesBinding
import com.logestechs.driver.utils.LogesTechsActivity

class ReturnedPackagesActivity : LogesTechsActivity() {
    private lateinit var binding: ActivityReturnedPackagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReturnedPackagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}