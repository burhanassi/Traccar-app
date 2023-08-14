package com.logestechs.driver.ui.driverRouteActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.databinding.ActivityDriverRouteBinding
import com.logestechs.driver.databinding.ActivityReturnedPackagesBinding
import com.logestechs.driver.ui.packageDeliveryScreens.returnedPackageDelivery.ReturnedPackageDeliveryActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.adapters.ReturnedPackageCustomerCellAdapter
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DriverRouteActivity : LogesTechsActivity(),
    View.OnClickListener {
    private lateinit var binding: ActivityDriverRouteBinding

    private var doesUpdateData = true
    private var enableUpdateData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
//            callGetCustomersWithReturnedPackages()
        } else {
            doesUpdateData = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (enableUpdateData) {
            doesUpdateData = true
            enableUpdateData = false
        } else {
            doesUpdateData = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Helper.showSuccessMessage(
                super.getContext(),
                getString(R.string.success_operation_completed)
            )
//            callGetCustomersWithReturnedPackages()
        }
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
    }

    private fun initListeners() {
        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
    }

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
//            binding.refreshLayoutCustomers.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }

    //apis

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }
        }
    }
}