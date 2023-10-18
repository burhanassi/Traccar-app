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
            var enteredIp = binding.etServerIp.getText().toString()

            if (enteredIp.isEmpty()) {
                binding.etServerIp.makeInvalid()
            } else {
                if (!enteredIp.contains(":") && enteredIp.startsWith("192.")) {
                    enteredIp += ":8080"
                }

                binding.etServerIp.makeValid()
                SharedPreferenceWrapper.saveSelectedServerIp(enteredIp)
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