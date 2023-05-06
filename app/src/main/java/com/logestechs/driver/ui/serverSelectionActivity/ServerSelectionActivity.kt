package com.logestechs.driver.ui.serverSelectionActivity

import android.content.Intent
import android.os.Bundle
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.databinding.ActivityServerSelectionBinding
import com.logestechs.driver.ui.splash.SplashActivity
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper

class ServerSelectionActivity : LogesTechsActivity() {

    private lateinit var binding: ActivityServerSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etServerIp.setText(SharedPreferenceWrapper.getSelectedServerIp())

        binding.buttonDone.setOnClickListener {
            if (binding.etServerIp.getText().isEmpty()) {
                binding.etServerIp.makeInvalid()
            } else {
                binding.etServerIp.makeValid()
                SharedPreferenceWrapper.saveSelectedServerIp(binding.etServerIp.getText())
                ApiAdapter.recreateApiClient()
                navigateIntoApp()
            }
        }
    }

    private fun navigateIntoApp() {
        val mIntent = Intent(this, SplashActivity::class.java)
        startActivity(mIntent)
        finish()
    }
}