package com.logestechs.driver.ui.inCarPackages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.api.requests.ReturnPackageRequestBody
import com.logestechs.driver.api.responses.GetInCarPackagesGroupedResponse
import com.logestechs.driver.data.model.GroupedPackages
import com.logestechs.driver.databinding.FragmentInCarPackagesBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.InCarPackageCellAdapter
import com.logestechs.driver.utils.adapters.InCarPackageGroupedCellAdapter
import com.logestechs.driver.utils.dialogs.InCarStatusFilterDialog
import com.logestechs.driver.utils.dialogs.InCarViewModeDialog
import com.logestechs.driver.utils.interfaces.DriverPackagesByStatusViewPagerActivityDelegate
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener
import com.logestechs.driver.utils.interfaces.InCarStatusFilterDialogListener
import com.logestechs.driver.utils.interfaces.InCarViewModeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class InCarPackagesFragment : LogesTechsFragment(), View.OnClickListener,
    InCarViewModeDialogListener, InCarStatusFilterDialogListener, InCarPackagesCardListener {
    private var _binding: FragmentInCarPackagesBinding? = null
    private val binding get() = _binding!!

    private var activityDelegate: DriverPackagesByStatusViewPagerActivityDelegate? = null
    private var doesUpdateData = true
    private var enableUpdateData = false

    var selectedViewMode: InCarPackagesViewMode = InCarPackagesViewMode.BY_VILLAGE
    var selectedStatus: InCarPackageStatus = InCarPackageStatus.TO_DELIVER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentInCarPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_in_car_packages,
            container,
            false
        )
        _binding = v
        return v.root
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
            getPackagesBySelectedMode()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        activityDelegate = activity as DriverPackagesByStatusViewPagerActivityDelegate
        binding.textTitle.text = getString(R.string.packages_view_pager_in_car_packages)
        binding.textSelectedStatus.text =
            "(${Helper.getLocalizedInCarStatus(super.getContext(), selectedStatus)})"
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvPackages.adapter = InCarPackageGroupedCellAdapter(
            ArrayList(),
            super.getContext(),
            this
        )
        binding.rvPackages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutPackages.setOnRefreshListener {
            selectedStatus = InCarPackageStatus.TO_DELIVER
            binding.textSelectedStatus.text =
                "(${Helper.getLocalizedInCarStatus(super.getContext(), selectedStatus)})"
            getPackagesBySelectedMode()
        }

        binding.buttonViewMode.setOnClickListener(this)
        binding.buttonStatusFilter.setOnClickListener(this)
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


    private fun getPackagesBySelectedMode() {
        if (selectedViewMode == InCarPackagesViewMode.BY_VILLAGE
            || selectedViewMode == InCarPackagesViewMode.BY_CUSTOMER
            || selectedViewMode == InCarPackagesViewMode.BY_RECEIVER
        ) {
            if (binding.rvPackages.adapter !is InCarPackageGroupedCellAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvPackages.adapter = InCarPackageGroupedCellAdapter(
                    ArrayList(),
                    super.getContext(),
                    this
                )
                binding.rvPackages.layoutManager = layoutManager
            }
            callGetInCarPackagesGrouped()
        } else {
            if (binding.rvPackages.adapter !is InCarPackageCellAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvPackages.adapter = InCarPackageCellAdapter(
                    ArrayList(),
                    super.getContext(),
                    this,
                    null,
                    isGrouped = false
                )
                binding.rvPackages.layoutManager = layoutManager
            }
            callGetInCarPackagesUngrouped()
        }
    }

    //APIs
    private fun callGetInCarPackagesGrouped() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var response: Response<GetInCarPackagesGroupedResponse?>? = null
                    response = when (selectedViewMode) {
                        InCarPackagesViewMode.BY_VILLAGE -> {
                            ApiAdapter.apiClient.getInCarPackagesByVillage(status = selectedStatus.value)
                        }
                        InCarPackagesViewMode.BY_CUSTOMER -> {
                            ApiAdapter.apiClient.getInCarPackagesByCustomer(status = selectedStatus.value)
                        }
                        InCarPackagesViewMode.BY_RECEIVER -> {
                            ApiAdapter.apiClient.getInCarPackagesByReceiver(status = selectedStatus.value)
                        }
                        else -> {
                            ApiAdapter.apiClient.getInCarPackagesByVillage(status = selectedStatus.value)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as InCarPackageGroupedCellAdapter).update(
                                body?.inCarPackages as ArrayList<GroupedPackages?>, selectedViewMode
                            )
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body.numberOfPackages ?: 0)
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

    private fun callGetInCarPackagesUngrouped() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getInCarPackagesUngrouped(status = selectedStatus.value)

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as InCarPackageCellAdapter).update(
                                body?.pkgs ?: ArrayList()
                            )
                            activityDelegate?.updateCountValues()
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

    private fun callReturnPackage(packageId: Long?, body: ReturnPackageRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.returnPackage(
                        packageId,
                        body
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
                            activityDelegate?.updateCountValues()
                            getPackagesBySelectedMode()
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

    private fun callFailDelivery(packageId: Long?, body: FailDeliveryRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.failDelivery(
                        packageId,
                        body
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
                            activityDelegate?.updateCountValues()
                            getPackagesBySelectedMode()
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_status_filter -> {
                InCarStatusFilterDialog(requireContext(), this, selectedStatus).showDialog()
            }

            R.id.button_view_mode -> {
                InCarViewModeDialog(requireContext(), this, selectedViewMode).showDialog()
            }
        }
    }

    override fun onViewModeChanged(selectedViewMode: InCarPackagesViewMode) {
        this.selectedViewMode = selectedViewMode
        getPackagesBySelectedMode()
    }

    override fun onStatusChanged(selectedStatus: InCarPackageStatus) {
        this.selectedStatus = selectedStatus
        binding.textSelectedStatus.text =
            "(${Helper.getLocalizedInCarStatus(super.getContext(), selectedStatus)})"
        getPackagesBySelectedMode()
    }

    override fun onPackageReturned(body: ReturnPackageRequestBody?) {
        callReturnPackage(body?.packageId, body)
    }

    override fun onFailDelivery(body: FailDeliveryRequestBody?) {
        callFailDelivery(body?.packageId, body)
    }
}
