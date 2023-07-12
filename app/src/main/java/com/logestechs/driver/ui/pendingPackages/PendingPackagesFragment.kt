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
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.PendingPackageCellAdapter
import com.logestechs.driver.utils.adapters.PendingPackageCustomerCellAdapter
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener
import com.logestechs.driver.utils.interfaces.RejectPackageDialogListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.*
import org.json.JSONObject

class PendingPackagesFragment : LogesTechsFragment(), PendingPackagesCardListener {

    private var _binding: FragmentPendingPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null

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
        initListeners()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        binding.textTitle.text = getString(R.string.packages_view_pager_pending_packages)
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            callGetPendingPackages()
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvCustomers.adapter = PendingPackageCustomerCellAdapter(
            ArrayList(),
            requireContext(),
            this@PendingPackagesFragment,
            null
        )
        binding.rvCustomers.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            callGetPendingPackages()
        }
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
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body.customers?.size ?: 0)
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
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            removeCustomerCell(parentIndex)
                            activityDelegate?.updateCountValues()
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
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            removePackageCell(parentIndex, childIndex)
                            activityDelegate?.updateCountValues()
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

    private fun callRejectCustomerPackages(customerId: Long?, parentIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectCustomerPackages(
                        customerId,
                        rejectPackageRequestBody
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            removeCustomerCell(parentIndex)
                            activityDelegate?.updateCountValues()
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

    private fun callRejectPackage(packageId: Long?, parentIndex: Int, childIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectPackage(
                        packageId,
                        rejectPackageRequestBody
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            removePackageCell(parentIndex, childIndex)
                            activityDelegate?.updateCountValues()
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

    override fun rejectPackage(parentIndex: Int, childIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody) {
        callRejectPackage(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
                childIndex
            )?.id, parentIndex, childIndex,
            rejectPackageRequestBody
        )
    }

    override fun rejectCustomerPackages(parentIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody) {
        callRejectCustomerPackages(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.id,
            parentIndex,
            rejectPackageRequestBody
        )
    }

}