package com.logestechs.driver.ui.packageDelivery

import android.os.Bundle
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityPackageDeliveryBinding
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity

class PackageDeliveryActivity : LogesTechsActivity() {
    private lateinit var binding: ActivityPackageDeliveryBinding

    private var pkg: Package? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            pkg = extras.getParcelable(IntentExtrasKeys.PACKAGE_TO_DELIVER.name)
        }
    }
}