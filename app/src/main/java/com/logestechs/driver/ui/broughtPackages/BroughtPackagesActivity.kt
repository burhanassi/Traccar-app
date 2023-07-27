package com.logestechs.driver.ui.broughtPackages

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.databinding.ActivityBroughtPackagesBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.adapters.BroughtGroupedPackagesAdapter
import com.logestechs.driver.utils.adapters.InCarPackageCellAdapter
import com.logestechs.driver.utils.interfaces.BroughtPackagesCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BroughtPackagesActivity : LogesTechsActivity(), BroughtPackagesCardListener,
    View.OnClickListener {
    private lateinit var binding: ActivityBroughtPackagesBinding

    private var doesUpdateData = true
    private var enableUpdateData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroughtPackagesBinding.inflate(layoutInflater)
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
            callGetBroughtPackagesUngrouped()
//            callGetCustomersWithReturnedPackages()
        }
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvPackages.adapter = BroughtGroupedPackagesAdapter(
            ArrayList(),
            super.getContext(),
            this
        )
        binding.rvPackages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutPackages.setOnRefreshListener {
            callGetBroughtPackagesUngrouped()
//            callGetCustomersWithReturnedPackages()
        }

        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvPackages.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvPackages.visibility = View.GONE
        }
    }

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
            binding.refreshLayoutPackages.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }

    //apis
    private fun callGetBroughtPackagesUngrouped() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getInCarPackagesUngrouped(
                            status = selectedStatus.value,
                            packageType = selectedPackageType.name,
                            searchWord
                        )

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as InCarPackageCellAdapter).update(
                                body?.pkgs ?: ArrayList()
                            )
//                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body?.numberOfPackages ?: 0)
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
//    private fun callGetCustomersWithReturnedPackages() {
//        showWaitDialog()
//        if (Helper.isInternetAvailable(super.getContext())) {
//            GlobalScope.launch(Dispatchers.IO) {
//                try {
//                    val response = ApiAdapter.apiClient.getCustomersWithReturnedPackages()
//                    withContext(Dispatchers.Main) {
//                        hideWaitDialog()
//                    }
//                    if (response?.isSuccessful == true && response.body() != null) {
//                        val body = response.body()
//                        withContext(Dispatchers.Main) {
//                            withContext(Dispatchers.Main) {
//                                (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).update(
//                                    body?.customers as ArrayList<Customer?>
//                                )
//                                handleNoPackagesLabelVisibility(body.customers?.size ?: 0)
//                            }
//                        }
//                    } else {
//                        try {
//                            val jObjError = JSONObject(response?.errorBody()!!.string())
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    jObjError.optString(AppConstants.ERROR_KEY)
//                                )
//                            }
//
//                        } catch (e: java.lang.Exception) {
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    getString(R.string.error_general)
//                                )
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    hideWaitDialog()
//                    Helper.logException(e, Throwable().stackTraceToString())
//                    withContext(Dispatchers.Main) {
//                        if (e.message != null && e.message!!.isNotEmpty()) {
//                            Helper.showErrorMessage(super.getContext(), e.message)
//                        } else {
//                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
//                        }
//                    }
//                }
//            }
//        } else {
//            hideWaitDialog()
//            Helper.showErrorMessage(
//                super.getContext(), getString(R.string.error_check_internet_connection)
//            )
//        }
//    }
//
//    private fun callGetCustomerReturnedPackages(
//        customerId: Long?,
//        barcode: String?,
//        position: Int
//    ) {
//        showWaitDialog()
//        if (Helper.isInternetAvailable(super.getContext())) {
//            GlobalScope.launch(Dispatchers.IO) {
//                try {
//                    val response =
//                        ApiAdapter.apiClient.getCustomerReturnedPackages(customerId, barcode)
//                    withContext(Dispatchers.Main) {
//                        hideWaitDialog()
//                    }
//                    if (response?.isSuccessful == true && response.body() != null) {
//                        val body = response.body()
//                        withContext(Dispatchers.Main) {
//                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[position]?.packages =
//                                body?.pkgs
//                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[position]?.isExpanded =
//                                true
//                            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).notifyItemChanged(
//                                position
//                            )
//                        }
//                    } else {
//                        try {
//                            val jObjError = JSONObject(response?.errorBody()!!.string())
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    jObjError.optString(AppConstants.ERROR_KEY)
//                                )
//                            }
//
//                        } catch (e: java.lang.Exception) {
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    getString(R.string.error_general)
//                                )
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    hideWaitDialog()
//                    Helper.logException(e, Throwable().stackTraceToString())
//                    withContext(Dispatchers.Main) {
//                        if (e.message != null && e.message!!.isNotEmpty()) {
//                            Helper.showErrorMessage(super.getContext(), e.message)
//                        } else {
//                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
//                        }
//                    }
//                }
//            }
//        } else {
//            hideWaitDialog()
//            Helper.showErrorMessage(
//                super.getContext(), getString(R.string.error_check_internet_connection)
//            )
//        }
//    }
//
//    override fun deliverPackage(parentIndex: Int, childIndex: Int) {
//        val pkg =
//            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
//                childIndex
//            )
//        val mIntent = Intent(this, ReturnedPackageDeliveryActivity::class.java)
//        mIntent.putExtra(IntentExtrasKeys.PACKAGE_TO_DELIVER.name, pkg)
//        startActivityForResult(mIntent, 1)
//    }
//
//    override fun deliverCustomerPackages(parentIndex: Int) {
//        val customer =
//            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]
//        val mIntent = Intent(this, ReturnedPackageDeliveryActivity::class.java)
//        mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_TO_RETURN.name, customer)
//        startActivityForResult(mIntent, 1)
//    }
//
//    override fun getCustomerPackages(parentIndex: Int) {
//        val customer =
//            (binding.rvCustomers.adapter as ReturnedPackageCustomerCellAdapter).customersList[parentIndex]
//        callGetCustomerReturnedPackages(
//            customer?.customerId,
//            customer?.massReturnedPackagesReportBarcode,
//            parentIndex
//        )
//    }

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