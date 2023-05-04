package com.logestechs.driver.ui.inCarPackages

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.AddNoteRequestBody
import com.logestechs.driver.api.requests.ChangePackageTypeRequestBody
import com.logestechs.driver.api.requests.CodChangeRequestBody
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.api.requests.ReturnPackageRequestBody
import com.logestechs.driver.api.responses.GetInCarPackagesGroupedResponse
import com.logestechs.driver.data.model.GroupedPackages
import com.logestechs.driver.data.model.LatLng
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.FragmentInCarPackagesBinding
import com.logestechs.driver.ui.googleMapActivity.GoogleMapActivity
import com.logestechs.driver.ui.packageDeliveryScreens.packageDelivery.PackageDeliveryActivity
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.ConfirmationDialogAction
import com.logestechs.driver.utils.DeliveryAttemptType
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.InCarPackagesViewMode
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.InCarPackageCellAdapter
import com.logestechs.driver.utils.adapters.InCarPackageGroupedCellAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.dialogs.AddPackageNoteDialog
import com.logestechs.driver.utils.dialogs.InCarStatusFilterDialog
import com.logestechs.driver.utils.dialogs.InCarViewModeDialog
import com.logestechs.driver.utils.dialogs.PackageTypeFilterDialog
import com.logestechs.driver.utils.dialogs.ReturnPackageDialog
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.AddPackageNoteDialogListener
import com.logestechs.driver.utils.interfaces.ConfirmationDialogActionListener
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener
import com.logestechs.driver.utils.interfaces.InCarStatusFilterDialogListener
import com.logestechs.driver.utils.interfaces.InCarViewModeDialogListener
import com.logestechs.driver.utils.interfaces.PackageTypeFilterDialogListener
import com.logestechs.driver.utils.interfaces.ReturnPackageDialogListener
import com.logestechs.driver.utils.interfaces.SearchPackagesDialogListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class InCarPackagesFragment(
    var selectedStatus: InCarPackageStatus = InCarPackageStatus.TO_DELIVER
) : LogesTechsFragment(),
    View.OnClickListener,
    InCarViewModeDialogListener,
    InCarStatusFilterDialogListener,
    InCarPackagesCardListener,
    SearchPackagesDialogListener,
    AddPackageNoteDialogListener,
    ReturnPackageDialogListener,
    ConfirmationDialogActionListener,
    PackageTypeFilterDialogListener {

    private var _binding: FragmentInCarPackagesBinding? = null
    private val binding get() = _binding!!

    private var activityDelegate: ViewPagerCountValuesDelegate? = null
    private var searchWord: String? = null

    private val companyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()

    private val messageTemplates = companyConfigurations?.messageTemplates
    var selectedViewMode: InCarPackagesViewMode = InCarPackagesViewMode.BY_VILLAGE

    var addPackageNoteDialog: AddPackageNoteDialog? = null
    var selectedPodImageUri: Uri? = null
    var mCurrentPhotoPath: String? = null

    var loadedImagesList: java.util.ArrayList<LoadedImage> = java.util.ArrayList()
    private var isCameraAction = false
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var selectedPackageType: PackageType = PackageType.ALL

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
        if (!LogesTechsApp.isInBackground) {
            getPackagesBySelectedMode()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        activityDelegate = activity as ViewPagerCountValuesDelegate
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
            searchWord = null
            selectedStatus = InCarPackageStatus.TO_DELIVER
            selectedPackageType = PackageType.ALL
            binding.textSelectedStatus.text =
                "(${Helper.getLocalizedInCarStatus(super.getContext(), selectedStatus)})"
            getPackagesBySelectedMode()
        }

        binding.buttonViewMode.setOnClickListener(this)
        binding.buttonStatusFilter.setOnClickListener(this)
        binding.buttonSearch.setOnClickListener(this)
        binding.buttonOptions.setOnClickListener(this)
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

    private fun showContextMenu() {
        val popup = PopupMenu(context, binding.buttonOptions)

        popup.inflate(R.menu.in_car_packages_fragment_context_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.action_payment_method -> {
                    PackageTypeFilterDialog(
                        requireContext(),
                        this,
                        selectedPackageType
                    ).showDialog()
                }

                R.id.action_send_sms_to_all -> {
                    val numbers: ArrayList<String?> = ArrayList()
                    if (binding.rvPackages.adapter is InCarPackageGroupedCellAdapter) {
                        val groupedPackagesList =
                            (binding.rvPackages.adapter as InCarPackageGroupedCellAdapter).packagesList
                        for (item in groupedPackagesList) {
                            if (item?.pkgs != null) {
                                for (pkg in item.pkgs!!) {
                                    numbers.add(pkg?.receiverPhone)
                                }
                            }
                        }
                    } else if (binding.rvPackages.adapter is InCarPackageCellAdapter) {
                        val packagesList =
                            (binding.rvPackages.adapter as InCarPackageCellAdapter).packagesList
                        for (item in packagesList) {
                            numbers.add(item?.receiverPhone)
                        }
                    }

                    if (numbers.isNotEmpty()) {
                        (super.getContext() as LogesTechsActivity).sendSmsToMultiple(
                            Helper.removeDuplicates(
                                numbers
                            ),
                            Helper.getInterpretedMessageFromTemplate(
                                null,
                                true,
                                SharedPreferenceWrapper.getDriverCompanySettings()?.messageTemplates?.distribution
                            )
                        )
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_no_packages_for_delivery)
                        )
                    }
                }

                R.id.action_driver_packages_locations -> {
                    getMyLocation()
                }
            }
            true
        }

        popup.menu.findItem(R.id.action_driver_packages_locations).isVisible =
            companyConfigurations?.driverCompanyConfigurations?.hasRouteOptimization == true
        popup.show()
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


    private fun getMyLocation() {
        showWaitDialog()
        val permissionLocation = ContextCompat.checkSelfPermission(
            super.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            if (fusedLocationProviderClient == null) {
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(
                        super.requireContext()
                    )
            }


            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    SharedPreferenceWrapper.saveLastSyncLocation(latLng)
                    callGetDriverPackagesLocations(latLng)
                }
            }?.addOnFailureListener {
                Toast.makeText(
                    context,
                    getString(R.string.app_needs_location_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                            ApiAdapter.apiClient.getInCarPackagesByVillage(
                                status = selectedStatus.value,
                                packageType = selectedPackageType.name,
                                searchWord
                            )
                        }

                        InCarPackagesViewMode.BY_CUSTOMER -> {
                            ApiAdapter.apiClient.getInCarPackagesByCustomer(
                                status = selectedStatus.value,
                                packageType = selectedPackageType.name,
                                searchWord
                            )
                        }

                        InCarPackagesViewMode.BY_RECEIVER -> {
                            ApiAdapter.apiClient.getInCarPackagesByReceiver(
                                status = selectedStatus.value,
                                packageType = selectedPackageType.name,
                                searchWord
                            )
                        }

                        else -> {
                            ApiAdapter.apiClient.getInCarPackagesByVillage(
                                status = selectedStatus.value,
                                packageType = selectedPackageType.name,
                                searchWord
                            )
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

    private fun callPostponePackage(packageId: Long?, body: PostponePackageRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.postponePackage(
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

    private fun callChangePackageType(packageId: Long?, body: ChangePackageTypeRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.changePackageType(
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

    private fun callAddPackageNote(packageId: Long?, body: AddNoteRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.addPackageNote(
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

    private fun callCodChangeRequestApi(body: CodChangeRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.codChangeRequest(
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

    private fun callGetPartnerNameById(
        packageId: Long?,
        pkg: Package?,
        isSms: Boolean,
        isSecondary: Boolean
    ) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getPartnerNameByPackageId(
                        packageId ?: -1
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            if (isSms) {
                                (context as LogesTechsActivity).sendSms(
                                    pkg?.receiverPhone,
                                    Helper.getInterpretedMessageFromTemplate(
                                        pkg,
                                        false,
                                        messageTemplates?.distribution,
                                        body?.name
                                    )
                                )
                            } else {
                                (context as LogesTechsActivity).sendWhatsAppMessage(
                                    Helper.formatNumberForWhatsApp(
                                        pkg?.receiverPhone,
                                        isSecondary
                                    ), Helper.getInterpretedMessageFromTemplate(
                                        pkg,
                                        false,
                                        messageTemplates?.distribution,
                                        body?.name
                                    )
                                )
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

    private fun callGetDriverPackagesLocations(latLng: LatLng) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDriverPackagesLocations(
                        latLng.lat,
                        latLng.lng
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if ((response.body()?.items?.size ?: 0) > 0) {
                                val mIntent = Intent(context, GoogleMapActivity::class.java)
                                mIntent.putExtra(
                                    IntentExtrasKeys.DRIVER_PACKAGES_LOCATIONS.name,
                                    response.body()
                                )
                                startActivity(mIntent)
                            } else {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_no_destinations_on_map)
                                )
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


    private fun callDeliveryAttempt(packageId: Long?, deliveryAttemptType: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = ApiAdapter.apiClient.deliveryAttempt(packageId, deliveryAttemptType)
            } catch (e: Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
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

            R.id.button_search -> {
                SearchPackagesDialog(requireContext(), this, searchWord).showDialog()
            }

            R.id.button_options -> {
                showContextMenu()
            }
        }
    }


    private fun openCamera(): String? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(context?.packageManager!!) != null) {
            var photoFile: File? = null
            photoFile = try {
                Helper.createImageFile(activity as LogesTechsActivity)
            } catch (ex: IOException) {
                return ""
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    context?.applicationContext!!,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    photoFile
                )
                val mCurrentPhotoPath = "file:" + photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.clipData = ClipData.newRawUri("", photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                this.startActivityForResult(
                    takePictureIntent,
                    AppConstants.REQUEST_TAKE_PHOTO
                )
                return mCurrentPhotoPath
            }
            return ""
        }
        return ""
    }

    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK)
        pickPhoto.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(
            pickPhoto,
            AppConstants.REQUEST_LOAD_PHOTO
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_TAKE_PHOTO -> if (resultCode == AppCompatActivity.RESULT_OK) {
                selectedPodImageUri = Uri.parse(mCurrentPhotoPath)
                if (selectedPodImageUri != null) {
                    val compressedImage =
                        Helper.validateCompressedImage(
                            selectedPodImageUri!!,
                            true,
                            super.getContext()
                        )
                    if (compressedImage != null) {
                        loadedImagesList.add(compressedImage)
                        if (loadedImagesList.size > 0 && loadedImagesList[loadedImagesList.size - 1]
                                .imageUrl == null
                        ) {
                            callUploadPodImage(loadedImagesList[loadedImagesList.size - 1])
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.error_image_capture_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_image_capture_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_LOAD_PHOTO -> if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                selectedPodImageUri = data.data
                if (selectedPodImageUri != null) {
                    val compressedImage =
                        Helper.validateCompressedImage(
                            selectedPodImageUri!!,
                            false,
                            super.getContext()
                        )
                    if (compressedImage != null) {
                        loadedImagesList.add(compressedImage)
                        if (loadedImagesList.size > 0 && loadedImagesList[loadedImagesList.size - 1].imageUrl == null
                        ) {
                            callUploadPodImage(loadedImagesList[loadedImagesList.size - 1])
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.error_image_loading),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.error_image_loading),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_SCAN_BARCODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    searchWord = data.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name)
                } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                    SearchPackagesDialog(requireContext(), this, searchWord).showDialog()
                }
            }

            else -> {}
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (Helper.isStorageAndCameraPermissionNeeded(activity as LogesTechsActivity)
                ) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    if (isCameraAction) {
                        mCurrentPhotoPath = openCamera()
                        isCameraAction = false
                    } else {
                        openGallery()
                    }
                }
            } else {
                if (Helper.shouldShowCameraAndStoragePermissionDialog(activity as LogesTechsActivity)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_camera_and_storage_permissions)
                    )
                }
            }
        }
    }

    private fun callUploadPodImage(loadedImage: LoadedImage?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val file: File = File(
                        Helper.getRealPathFromURI(
                            super.getContext(),
                            loadedImage?.imageUri!!
                        ) ?: ""
                    )
                    val bitmap: Bitmap = MediaStore.Images.Media
                        .getBitmap(super.getContext()?.contentResolver, Uri.fromFile(file))

                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        AppConstants.IMAGE_FULL_QUALITY,
                        bytes
                    )

                    val reqFile: RequestBody =
                        bytes.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())

                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("en")).format(Date())
                    val imageFileName = "JPEG_" + timeStamp + "_"

                    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "file",
                        "$imageFileName.jpeg", reqFile
                    )

                    val response = ApiAdapter.apiClient.uploadPodImage(
                        addPackageNoteDialog?.pkg?.id ?: -1,
                        true,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                            loadedImagesList[loadedImagesList.size - 1].imageUrl =
                                response.body()?.fileUrl
                            (addPackageNoteDialog?.binding?.rvThumbnails?.adapter as ThumbnailsAdapter).updateItem(
                                loadedImagesList.size - 1
                            )
                            addPackageNoteDialog?.binding?.containerThumbnails?.visibility =
                                View.VISIBLE
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

    private fun callDeletePodImage(position: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.deletePodImage(DeleteImageRequestBody(loadedImagesList[position].imageUrl))
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            loadedImagesList.removeAt(position)
                            (addPackageNoteDialog?.binding?.rvThumbnails?.adapter as ThumbnailsAdapter).deleteItem(
                                position
                            )
                            if (loadedImagesList.isEmpty()) {
                                addPackageNoteDialog?.binding?.containerThumbnails?.visibility =
                                    View.GONE
                            }
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
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
        callReturnPackage(body?.pkg?.id, body)
    }

    override fun onShowReturnPackageDialog(pkg: Package?) {
        if (pkg?.isReceiverPayCost == true) {
            (requireActivity() as LogesTechsActivity).showConfirmationDialog(
                getString(R.string.warning_receiver_pays_cost),
                pkg,
                ConfirmationDialogAction.RETURN_PACKAGE,
                this
            )
        } else {
            ReturnPackageDialog(context, this, pkg).showDialog()
        }
    }

    override fun onFailDelivery(body: FailDeliveryRequestBody?) {
        callFailDelivery(body?.packageId, body)
    }

    override fun onPackagePostponed(body: PostponePackageRequestBody?) {
        callPostponePackage(body?.packageId, body)
    }

    override fun onPackageTypeChanged(body: ChangePackageTypeRequestBody?) {
        callChangePackageType(body?.packageId, body)
    }

    override fun onPackageNoteAdded(body: AddNoteRequestBody?) {
        callAddPackageNote(body?.packageId, body)
    }

    override fun onCaptureImage() {
        isCameraAction = true
        if (Helper.isStorageAndCameraPermissionNeeded(activity as LogesTechsActivity)) {
            Helper.showAndRequestCameraAndStorageDialog(this)
        } else {
            mCurrentPhotoPath = openCamera()
        }
    }

    override fun onLoadImage() {
        isCameraAction = false
        if (Helper.isStorageAndCameraPermissionNeeded(activity as LogesTechsActivity)) {
            Helper.showAndRequestCameraAndStorageDialog(this)
        } else {
            openGallery()
        }
    }

    override fun onDeleteImage(position: Int) {
        callDeletePodImage(position)
    }

    override fun onShowPackageNoteDialog(pkg: Package?) {
        loadedImagesList.clear()
        addPackageNoteDialog = AddPackageNoteDialog(requireContext(), this, pkg, loadedImagesList)
        addPackageNoteDialog?.showDialog()
    }

    override fun onCodChanged(body: CodChangeRequestBody?) {
        callCodChangeRequestApi(body)
    }

    override fun onDeliverPackage(pkg: Package?) {
        val mIntent = Intent(context, PackageDeliveryActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.PACKAGE_TO_DELIVER.name, pkg)
        startActivity(mIntent)
    }

    override fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.WHATSAPP_SMS.name)
        if (pkg?.partnerId != null && pkg.partnerId != 0L) {
            callGetPartnerNameById(pkg.id, pkg, false, isSecondary)
        } else {
            (context as LogesTechsActivity).sendWhatsAppMessage(
                Helper.formatNumberForWhatsApp(
                    pkg?.receiverPhone,
                    isSecondary
                ), Helper.getInterpretedMessageFromTemplate(
                    pkg,
                    false,
                    messageTemplates?.distribution
                )
            )
        }
    }

    override fun onSendSmsMessage(pkg: Package?) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.PHONE_SMS.name)
        if (pkg?.partnerId != null && pkg.partnerId != 0L) {
            callGetPartnerNameById(pkg.id, pkg, isSms = true, isSecondary = false)
        } else {
            (context as LogesTechsActivity).sendSms(
                pkg?.receiverPhone,
                Helper.getInterpretedMessageFromTemplate(
                    pkg,
                    false,
                    messageTemplates?.distribution,
                )
            )
        }
    }

    override fun onCallReceiver(pkg: Package?, receiverPhone: String?) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.PHONE_CALL.name)
        (activity as LogesTechsActivity).callMobileNumber(receiverPhone)
    }

    override fun onPackageSearch(keyword: String?) {
        searchWord = keyword
        getPackagesBySelectedMode()
    }

    override fun onStartBarcodeScan() {
        val scanBarcode = Intent(context, SingleScanBarcodeScanner::class.java)
        this.startActivityForResult(
            scanBarcode,
            AppConstants.REQUEST_SCAN_BARCODE
        )
    }

    override fun confirmAction(data: Any?, action: ConfirmationDialogAction) {
        if (action == ConfirmationDialogAction.RETURN_PACKAGE) {
            ReturnPackageDialog(context, this, data as Package?).showDialog()
        }
    }

    override fun onPackageTypeSelected(selectedPackageType: PackageType) {
        this.selectedPackageType = selectedPackageType
        getPackagesBySelectedMode()
    }
}
