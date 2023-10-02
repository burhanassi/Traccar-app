package com.logestechs.driver.ui.returnedPackages

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Bundles
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.databinding.ActivityReturnedPackagesBinding
import com.logestechs.driver.ui.packageDeliveryScreens.returnedPackageDelivery.ReturnedPackageDeliveryActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.ReturnedPackageStatus
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.ReturnedBundlesCustomerCellAdapter
import com.logestechs.driver.utils.adapters.ReturnedPackageCustomerCellAdapter
import com.logestechs.driver.utils.dialogs.ReturnedStatusFilterDialog
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.interfaces.ReturnedStatusFilterDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ReturnedPackagesActivity : LogesTechsActivity(), ReturnedPackagesCardListener,
        ReturnedStatusFilterDialogListener,
    View.OnClickListener {
    private lateinit var binding: ActivityReturnedPackagesBinding

    private var doesUpdateData = true
    private var enableUpdateData = false

    private var selectedStatus: ReturnedPackageStatus = ReturnedPackageStatus.ALL

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReturnedPackagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textSelectedStatus.text =
            "(${Helper.getLocalizedReturnedStatus(super.getContext(), selectedStatus)})"
        if (companyConfigurations?.isSupportReturnedBundles!!) {
            binding.textTitle.text = getText(R.string.title_returned_bundles)
            binding.textSelectedStatus.visibility = View.GONE
            binding.buttonStatusFilter.visibility = View.GONE
        }
        initRecycler()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
            if (companyConfigurations?.isSupportReturnedBundles!!) {
                callGetCustomersWithReturnedBundles()
            } else {
                callGetCustomersWithReturnedPackages()
            }
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
            if (companyConfigurations?.isSupportReturnedBundles!!) {
                callGetCustomersWithReturnedBundles()
            } else {
                callGetCustomersWithReturnedPackages()
            }
        }
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        if (companyConfigurations?.isSupportReturnedBundles!!) {
            binding.rvCustomers.adapter = ReturnedBundlesCustomerCellAdapter(
                ArrayList(), super.getContext(), listener = this
            )
            binding.rvCustomers.layoutManager = layoutManager
        } else {
            binding.rvCustomers.adapter = ReturnedPackageCustomerCellAdapter(
                ArrayList(), super.getContext(), listener = this
            )
            binding.rvCustomers.layoutManager = layoutManager
        }
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            selectedStatus = ReturnedPackageStatus.ALL
            binding.textSelectedStatus.text =
                "(${Helper.getLocalizedReturnedStatus(super.getContext(), selectedStatus)})"
            if (companyConfigurations?.isSupportReturnedBundles!!) {
                callGetCustomersWithReturnedBundles()
            } else {
                callGetCustomersWithReturnedPackages()
            }
        }

        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.buttonStatusFilter.setOnClickListener(this)
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvCustomers.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvCustomers.visibility = View.GONE
        }
    }

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
            binding.refreshLayoutCustomers.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }

    //apis
    private fun callGetCustomersWithReturnedPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getCustomersWithReturnedPackages(selectedStatus)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            withContext(Dispatchers.Main) {
                                (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).update(
                                    body?.customers as ArrayList<Customer?>
                                )
                                handleNoPackagesLabelVisibility(body.customers?.size ?: 0)
                            }
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(super.getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callGetCustomerReturnedPackages(
        customerId: Long?,
        barcode: String?,
        position: Int
    ) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getCustomerReturnedPackages(
                            customerId,
                            barcode,
                            selectedStatus
                        )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[position]?.packages =
                                body?.pkgs
                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[position]?.isExpanded =
                                true
                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).notifyItemChanged(
                                position
                            )
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(super.getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callGetCustomerReturnedBundles(
        bundleId: Long?,
        position: Int
    ) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getCustomerReturnedBundles(
                            bundleId
                        )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).bundlesList[position]?.packages =
                                body?.pkgs
                            (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).bundlesList[position]?.isExpanded =
                                true
                            (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).notifyItemChanged(
                                position
                            )
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(super.getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callGetCustomersWithReturnedBundles() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getCustomersWithReturnedBundles()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            withContext(Dispatchers.Main) {
                                (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).update(
                                    body?.bundles as ArrayList<Bundles?>
                                )
                                handleNoPackagesLabelVisibility(body.bundles?.size ?: 0)
                            }
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(super.getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    override fun deliverPackage(parentIndex: Int, childIndex: Int) {
        val pkg =
            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
                childIndex
            )
        val mIntent = Intent(this, ReturnedPackageDeliveryActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.PACKAGE_TO_DELIVER.name, pkg)
        startActivityForResult(mIntent, 1)
    }

    override fun deliverCustomerPackages(parentIndex: Int) {
        if (companyConfigurations?.isSupportReturnedBundles!!) {
            val bundle =
                (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).bundlesList[parentIndex]
            val mIntent = Intent(this, ReturnedPackageDeliveryActivity::class.java)
            mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_BUNDLES_TO_RETURN.name, bundle)
            startActivityForResult(mIntent, 1)
        } else {
            val customer =
                (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]
            val mIntent = Intent(this, ReturnedPackageDeliveryActivity::class.java)
            mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_TO_RETURN.name, customer)
            startActivityForResult(mIntent, 1)
        }
    }

    override fun getCustomerPackages(parentIndex: Int) {

        if (companyConfigurations?.isSupportReturnedBundles!!) {
            val bundle =
                (binding.rvCustomers.adapter as ReturnedBundlesCustomerCellAdapter).bundlesList[parentIndex]
            callGetCustomerReturnedBundles(
                bundle?.id,
                parentIndex
            )
        } else {
            val customer =
                (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]
            callGetCustomerReturnedPackages(
                customer?.customerId,
                customer?.massReturnedPackagesReportBarcode,
                parentIndex
            )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }

            R.id.button_status_filter -> {
                ReturnedStatusFilterDialog(context = getContext(), this, selectedStatus).showDialog()
            }
        }
    }

    override fun onStatusChanged(selectedStatus: ReturnedPackageStatus) {
        this.selectedStatus = selectedStatus
        binding.textSelectedStatus.text =
            "(${Helper.getLocalizedReturnedStatus(super.getContext(), selectedStatus)})"
        if (companyConfigurations?.isSupportReturnedBundles!!) {
            callGetCustomersWithReturnedBundles()
        } else {
            callGetCustomersWithReturnedPackages()
        }
    }
}