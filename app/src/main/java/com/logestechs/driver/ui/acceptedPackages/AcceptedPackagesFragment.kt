package com.logestechs.driver.ui.acceptedPackages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Village
import com.logestechs.driver.databinding.FragmentAcceptedPackagesBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.AcceptedPackageVillageCellAdapter
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import com.logestechs.driver.utils.interfaces.DriverPackagesByStatusViewPagerActivityDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AcceptedPackagesFragment : LogesTechsFragment(), AcceptedPackagesCardListener {

    private var _binding: FragmentAcceptedPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: DriverPackagesByStatusViewPagerActivityDelegate? = null
    private var doesUpdateData = true
    private var enableUpdateData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentAcceptedPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accepted_packages,
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
        callGetAcceptedPackages()
        activityDelegate = activity as DriverPackagesByStatusViewPagerActivityDelegate
        binding.textTitle.text = getString(R.string.packages_view_pager_pending_packages)
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
            callGetAcceptedPackages()
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

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvVillages.adapter = AcceptedPackageVillageCellAdapter(
            ArrayList(),
            super.getContext(),
            this
        )
        binding.rvVillages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            callGetAcceptedPackages()
        }
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvVillages.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvVillages.visibility = View.GONE
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
    private fun callGetAcceptedPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getAcceptedPackages()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvVillages.adapter as AcceptedPackageVillageCellAdapter).update(
                                body?.villages as ArrayList<Village?>
                            )
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body.villages?.size ?: 0)
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

    override fun scanForPickup(customer: Customer?) {
        enableUpdateData = true
        val mIntent = Intent(super.getContext(), BarcodeScannerActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_FOR_PICKUP.name, customer)
        startActivity(mIntent)
    }
}