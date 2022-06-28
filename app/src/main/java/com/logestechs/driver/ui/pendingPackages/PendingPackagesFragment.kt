package com.logestechs.driver.ui.pendingPackages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.databinding.FragmentPendingPackagesBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.PendingPackageCustomerCellAdapter
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // card interface
    override fun acceptPackage(packageId: Long?) {
        Helper.showErrorMessage(super.getContext(), "accept package id ${packageId}")
    }

    override fun acceptCustomerPackages(customerId: Long?) {
        Helper.showErrorMessage(super.getContext(), "accept customer id ${customerId}")
    }

    override fun rejectPackage(packageId: Long?) {
        Helper.showErrorMessage(super.getContext(), "reject package id ${packageId}")
    }

    override fun rejectCustomerPackages(customerId: Long?) {
        Helper.showErrorMessage(super.getContext(), "reject customer id ${customerId}")
    }
}