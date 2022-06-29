package com.logestechs.driver.ui.pendingPackages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.databinding.FragmentPendingPackagesBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.PendingPackageCellAdapter
import com.logestechs.driver.utils.adapters.PendingPackageCustomerCellAdapter
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener
import kotlinx.coroutines.*
import org.json.JSONObject

class PendingPackagesFragment : LogesTechsFragment(), PendingPackagesCardListener {

    private var _binding: FragmentPendingPackagesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentPendingPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pending_packages,
            container,
            false
        )
        _binding = v
        return v.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
    }

    override fun onResume() {
        super.onResume()
        callGetPendingPackages()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            context
        )
        binding.rvCustomers.adapter = PendingPackageCustomerCellAdapter(
            ArrayList(), super.getContext(), listener = this
        )
        binding.rvCustomers.layoutManager = layoutManager
    }

    //APIs
    private fun callGetPendingPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getPendingPackages()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).update(
                                body?.customers as ArrayList<Customer?>
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

    private fun callAcceptCustomerPackages(customerId: Long?, parentIndex: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.acceptCustomerPackages(customerId)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(super.getContext(), "Customer Accepted")
                            removeCustomerCell(parentIndex)
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

    private fun callAcceptPackage(packageId: Long?, parentIndex: Int, childIndex: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.acceptPackage(packageId)
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(super.getContext(), "Package Accepted")
                            removePackageCell(parentIndex, childIndex)
                        }
                        launch {
                            delay(500)
                            withContext(Dispatchers.Main) {
                                hideWaitDialog()
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

    private fun callRejectCustomerPackages(customerId: Long?, parentIndex: Int) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectCustomerPackages(
                        customerId,
                        RejectPackageRequestBody("test note")
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(super.getContext(), "Customer Rejected")
                            removeCustomerCell(parentIndex)
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

    private fun callRejectPackage(packageId: Long?, parentIndex: Int, childIndex: Int) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectPackage(
                        packageId,
                        RejectPackageRequestBody("inner package test note")
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(super.getContext(), "Package Rejected")
                            removePackageCell(parentIndex, childIndex)
                        }
                        launch {
                            delay(500)
                            withContext(Dispatchers.Main) {
                                hideWaitDialog()
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


    //Recycler view manipulation
    private fun removeCustomerCell(parentIndex: Int) {
        (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).deleteItem(
            parentIndex
        )
    }

    private fun removePackageCell(parentIndex: Int, childIndex: Int) {
        val parentAdapter = binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter
        val customerViewHolder =
            (binding.rvCustomers.findViewHolderForAdapterPosition(parentIndex) as PendingPackageCustomerCellAdapter.CustomerViewHolder)
        val childRecyclerViewAdapter =
            customerViewHolder.binding.rvPackages.adapter as PendingPackageCellAdapter

        if (parentAdapter.customersList[parentIndex]?.packages?.size == 1) {
            removeCustomerCell(parentIndex)
        } else {
            parentAdapter.customersList[parentIndex]?.packages?.removeAt(childIndex)
            parentAdapter.customersList[parentIndex]?.packagesNo =
                (parentAdapter.customersList[parentIndex]?.packagesNo ?: 1) - 1
            parentAdapter.notifyItemChanged(parentIndex)
            childRecyclerViewAdapter.removeItem(childIndex)
        }
    }

    // card interface
    override fun acceptPackage(parentIndex: Int, childIndex: Int) {
        callAcceptPackage(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
                childIndex
            )?.id, parentIndex, childIndex
        )
    }

    override fun acceptCustomerPackages(parentIndex: Int) {
        callAcceptCustomerPackages(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.id,
            parentIndex
        )
    }

    override fun rejectPackage(parentIndex: Int, childIndex: Int) {
        callRejectPackage(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
                childIndex
            )?.id, parentIndex, childIndex
        )
    }

    override fun rejectCustomerPackages(parentIndex: Int) {
        callRejectCustomerPackages(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.id,
            parentIndex
        )
    }
}