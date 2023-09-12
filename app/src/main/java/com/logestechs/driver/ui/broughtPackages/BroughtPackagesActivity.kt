package com.logestechs.driver.ui.broughtPackages

import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityBroughtPackagesBinding
import com.logestechs.driver.ui.packageDeliveryScreens.packageDelivery.PackageDeliveryActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.ConfirmationDialogAction
import com.logestechs.driver.utils.DeliveryAttemptType
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.InCarPackagesViewMode
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.InCarPackageGroupedCellAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.dialogs.AddPackageNoteDialog
import com.logestechs.driver.utils.dialogs.FailDeliveryDialog
import com.logestechs.driver.utils.dialogs.ReturnPackageDialog
import com.logestechs.driver.utils.dialogs.ShowAttachmentsDialog
import com.logestechs.driver.utils.interfaces.AddPackageNoteDialogListener
import com.logestechs.driver.utils.interfaces.ConfirmationDialogActionListener
import com.logestechs.driver.utils.interfaces.FailDeliveryDialogListener
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener
import com.logestechs.driver.utils.interfaces.ReturnPackageDialogListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.IOException

class BroughtPackagesActivity : LogesTechsActivity(), InCarPackagesCardListener,
    ConfirmationDialogActionListener,
    ReturnPackageDialogListener,
    AddPackageNoteDialogListener,
    FailDeliveryDialogListener,
    View.OnClickListener {
    private lateinit var binding: ActivityBroughtPackagesBinding

    private var doesUpdateData = true
    private var enableUpdateData = false
    var selectedStatus: InCarPackageStatus = InCarPackageStatus.TO_DELIVER

    private val companyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()
    private var activityDelegate: ViewPagerCountValuesDelegate? = null
    var loadedImagesList: java.util.ArrayList<LoadedImage> = java.util.ArrayList()
    var addPackageNoteDialog: AddPackageNoteDialog? = null
    private val messageTemplates = companyConfigurations?.messageTemplates
    private var isCameraAction = false
    var mCurrentPhotoPath: String? = null

    var failDeliveryDialog: FailDeliveryDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroughtPackagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        callGetInCarPackagesGrouped()
        initRecycler()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
//            callGetInCarPackagesGrouped()
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
            callGetInCarPackagesGrouped()
        }
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
            callGetInCarPackagesGrouped()
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
    private fun callGetInCarPackagesGrouped() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var response: Response<GetInCarPackagesGroupedResponse?>? = null
                            response = ApiAdapter.apiClient.getInCarPackagesByVillage(
                                status = selectedStatus.value,
                                packageType = "BRING"
                            )


                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as InCarPackageGroupedCellAdapter).update(
                                body?.inCarPackages as ArrayList<GroupedPackages?>, InCarPackagesViewMode.BY_VILLAGE
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
                            callGetInCarPackagesGrouped()
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
    private fun callGetAttachments(packageId: Long?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.packageAttachments(packageId)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            response.body()?.let { imageUrls ->
                                ShowAttachmentsDialog(
                                    this@BroughtPackagesActivity,
                                    packageId,
                                    imageUrls
                                ).showDialog()
                                Helper.showSuccessMessage(
                                    super.getContext(),
                                    getString(R.string.success_operation_completed)
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
                            callGetInCarPackagesGrouped()
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
                            callGetInCarPackagesGrouped()
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
                            callGetInCarPackagesGrouped()
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
                            callGetInCarPackagesGrouped()
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
                            callGetInCarPackagesGrouped()
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
                                (this@BroughtPackagesActivity as LogesTechsActivity).sendSms(
                                    pkg?.receiverPhone,
                                    Helper.getInterpretedMessageFromTemplate(
                                        pkg,
                                        false,
                                        messageTemplates?.distribution,
                                        body?.name
                                    )
                                )
                            } else {
                                (this@BroughtPackagesActivity as LogesTechsActivity).sendWhatsAppMessage(
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
    private fun openCamera(): String? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this@BroughtPackagesActivity?.packageManager!!) != null) {
            var photoFile: File? = null
            photoFile = try {
                Helper.createImageFile(this as LogesTechsActivity)
            } catch (ex: IOException) {
                return ""
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this@BroughtPackagesActivity?.applicationContext!!,
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

    override fun onPackageReturned(body: ReturnPackageRequestBody?) {
        callReturnPackage(body?.pkg?.id, body)
    }

    override fun onShowReturnPackageDialog(pkg: Package?) {
        if (pkg?.isReceiverPayCost == true) {
            (this as LogesTechsActivity).showConfirmationDialog(
                getString(R.string.warning_receiver_pays_cost),
                pkg,
                ConfirmationDialogAction.RETURN_PACKAGE,
                this
            )
        } else {
            ReturnPackageDialog(this, this, pkg).showDialog()
        }
    }

    override fun onShowAttachmentsDialog(pkg: Package?) {
        if(pkg?.isAttachmentExist == true){
            callGetAttachments(pkg.id)
        }else{
            Toast.makeText(
                this,
                getString(R.string.cannot_open_attachments),
                Toast.LENGTH_SHORT
            ).show()
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

    override fun onShowFailDeliveryDialog(pkg: Package?) {
        loadedImagesList.clear()
        failDeliveryDialog = FailDeliveryDialog(this, this, pkg, loadedImagesList)
        failDeliveryDialog?.showDialog()
    }

    override fun onCaptureImage() {
        isCameraAction = true
        if (Helper.isStorageAndCameraPermissionNeeded(this as LogesTechsActivity)) {
            Helper.showAndRequestCameraAndStorageDialog(this)
        } else {
            mCurrentPhotoPath = openCamera()
        }
    }

    override fun onLoadImage() {
        isCameraAction = false
        if (Helper.isStorageAndCameraPermissionNeeded(this as LogesTechsActivity)) {
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
        addPackageNoteDialog = AddPackageNoteDialog(this, this, pkg, loadedImagesList)
        addPackageNoteDialog?.showDialog()
    }

    override fun onCodChanged(body: CodChangeRequestBody?) {
        callCodChangeRequestApi(body)
    }

    override fun onDeliverPackage(pkg: Package?) {
        val mIntent = Intent(this@BroughtPackagesActivity, PackageDeliveryActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.PACKAGE_TO_DELIVER.name, pkg)
        startActivity(mIntent)
    }

    override fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.WHATSAPP_SMS.name)
        if (pkg?.partnerId != null && pkg.partnerId != 0L) {
            callGetPartnerNameById(pkg.id, pkg, false, isSecondary)
        } else {
            (this@BroughtPackagesActivity as LogesTechsActivity).sendWhatsAppMessage(
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
            (this@BroughtPackagesActivity as LogesTechsActivity).sendSms(
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
        (this as LogesTechsActivity).callMobileNumber(receiverPhone)
    }

    override fun confirmAction(data: Any?, action: ConfirmationDialogAction) {
        if (action == ConfirmationDialogAction.RETURN_PACKAGE) {
            ReturnPackageDialog(this@BroughtPackagesActivity, this, data as Package?).showDialog()
        }
    }
}