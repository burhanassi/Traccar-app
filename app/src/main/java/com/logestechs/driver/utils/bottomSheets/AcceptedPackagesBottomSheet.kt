package com.logestechs.driver.utils.bottomSheets

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.BottomSheetAcceptedPackagesBinding
import com.logestechs.driver.ui.acceptedPackages.AcceptedPackagesFragment
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.ui.barcodeScanner.SubBundlesBarcodeScannerActivity
import com.logestechs.driver.utils.AdminPackageStatus
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsBottomSheetFragment
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.NotificationsListAdapter
import com.logestechs.driver.utils.adapters.PackagesListAdapter
import com.logestechs.driver.utils.adapters.ScannedBarcodeCellAdapter
import com.logestechs.driver.utils.interfaces.AcceptedPackagesFragmentListener
import kotlinx.android.synthetic.main.fragment_accepted_packages.refresh_layout_customers
import kotlinx.android.synthetic.main.fragment_in_car_shipping_plans.refresh_layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AcceptedPackagesBottomSheet: LogesTechsBottomSheetFragment(),
    AcceptedPackagesFragmentListener {
    private var _binding: BottomSheetAcceptedPackagesBinding? = null
    private val binding get() = _binding!!
    private var packagesList: ArrayList<Package> = ArrayList()
    private var packagesCount = 0

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 2

    val listener: AcceptedPackagesFragmentListener = this

    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: BottomSheetAcceptedPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_accepted_packages,
            container,
            false
        )

        packagesList=
            arguments?.getParcelableArrayList(BundleKeys.PACKAGES_KEY.toString())
                ?: ArrayList()
        packagesCount =
            arguments?.getInt(BundleKeys.PACKAGES_COUNT.toString()) ?: 0

        _binding = v
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = PackagesListAdapter(packagesList, listener = listener)
            addOnScrollListener(recyclerViewOnScrollListener)
        }

        binding.textPackagesCount.text = packagesCount.toString()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvPackages.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvPackages.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvPackages.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
//                        getNotifications()
                    }
                }
            }
        }

    private fun callPickupPackage(barcode: String) {
        activity?.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.pickupPackage(
                        barcode,
                        driverCompanyConfigurations?.isBundlePodEnabled
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
                            dismiss()
                            (activity as AcceptedPackagesFragment).onResume()
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
    override fun callPickupPackageFromFragment(barcode: String) {
        callPickupPackage(barcode)
    }
}