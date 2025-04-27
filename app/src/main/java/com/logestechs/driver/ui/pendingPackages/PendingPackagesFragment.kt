package com.logestechs.driver.ui.pendingPackages

import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.FragmentPendingPackagesBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.DeliveryAttemptType
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.PendingPackageCellAdapter
import com.logestechs.driver.utils.adapters.PendingPackageCustomerCellAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.dialogs.RejectPackageDialog
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.CallDurationListener
import com.logestechs.driver.utils.interfaces.ImageActionListener
import com.logestechs.driver.utils.interfaces.PendingPackagesCardListener
import com.logestechs.driver.utils.interfaces.RejectPackageDialogListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PendingPackagesFragment : LogesTechsFragment(), PendingPackagesCardListener, CallDurationListener, ImageActionListener {

    private var _binding: FragmentPendingPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null
    var packageIdToSaveCallDuration: Long? = null

    var childAdapter: PendingPackageCellAdapter? = null
    var rejectPackageDialog: RejectPackageDialog? = null
    var selectedPodImageUri: Uri? = null
    var mCurrentPhotoPath: String? = null
    var parentIndex: Int = -1
    var childIndex: Int = -1
    var loadedImagesList: java.util.ArrayList<LoadedImage> = java.util.ArrayList()
    private var isCameraAction = false

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
            null,
            loadedImagesList
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

    private fun callDeliveryAttempt(packageId: Long?, deliveryAttemptType: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = ApiAdapter.apiClient.deliveryAttempt(packageId, deliveryAttemptType)
            } catch (e: Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    private fun callSaveCallDuration(callDuration: Double) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                ApiAdapter.apiClient.saveCallDuration(packageIdToSaveCallDuration!!, callDuration)
            } catch (e: Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
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
        this.parentIndex = parentIndex
        this.childIndex = childIndex
        callRejectPackage(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.packages?.get(
                childIndex
            )?.id, parentIndex, childIndex,
            rejectPackageRequestBody
        )
    }

    override fun rejectCustomerPackages(parentIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody) {
        this.parentIndex = parentIndex
        callRejectCustomerPackages(
            (binding.rvCustomers.adapter as PendingPackageCustomerCellAdapter).customersList[parentIndex]?.id,
            parentIndex,
            rejectPackageRequestBody
        )
    }

    override fun onShowRejectPackageDialog(parentIndex: Int, childIndex: Int) {
        loadedImagesList.clear()
        val customerViewHolder =
            (binding.rvCustomers.findViewHolderForAdapterPosition(parentIndex) as PendingPackageCustomerCellAdapter.CustomerViewHolder)
        val childRecyclerViewAdapter =
            customerViewHolder.binding.rvPackages.adapter as PendingPackageCellAdapter
        rejectPackageDialog = RejectPackageDialog(requireContext(), childRecyclerViewAdapter, this, loadedImagesList)
        rejectPackageDialog?.showDialog()
    }

    override fun onShowRejectCustomerPkgsDialog() {
        loadedImagesList.clear()
        rejectPackageDialog = RejectPackageDialog(requireContext(), PendingPackageCustomerCellAdapter(
            ArrayList(),
            requireContext(),
            this@PendingPackagesFragment,
            null,
            loadedImagesList
        ), this, loadedImagesList)
        rejectPackageDialog?.showDialog()
    }

    override fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.WHATSAPP_SMS.name)
        val message = pkg?.notificationTemplate?.takeIf { it.isNotEmpty() } ?: Helper.getInterpretedMessageFromTemplate(
            pkg,
            false,
            ""
        )
        (context as LogesTechsActivity).sendWhatsAppMessage(
            Helper.formatNumberForWhatsApp(
                pkg?.receiverPhone,
                isSecondary
            ), message
        )
    }

    override fun onSendSmsMessage(pkg: Package?) {
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.PHONE_SMS.name)
        val message = pkg?.notificationTemplate?.takeIf { it.isNotEmpty() } ?: Helper.getInterpretedMessageFromTemplate(
            pkg,
            false,
            ""
        )
        (context as LogesTechsActivity).sendSms(
            pkg?.receiverPhone,
            message
        )
    }

    override fun onCallReceiver(pkg: Package?, receiverPhone: String?) {
        packageIdToSaveCallDuration = pkg?.id
        callDeliveryAttempt(pkg?.id, DeliveryAttemptType.PHONE_CALL.name)
        (activity as LogesTechsActivity).callMobileNumber(receiverPhone, this)
    }

    override fun saveCallDuration(callDuration: Double) {
        callSaveCallDuration(callDuration)
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

                    val response = ApiAdapter.apiClient.uploadAttachmentImage(
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                            loadedImagesList[loadedImagesList.size - 1].imageUrl =
                                response.body()?.fileUrl

                            rejectPackageDialog?.let { dialog ->
                                (dialog.binding.rvThumbnails.adapter as ThumbnailsAdapter).updateItem(
                                    loadedImagesList.size - 1
                                )
                                dialog.binding.containerThumbnails.visibility = View.VISIBLE
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
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                        withContext(Dispatchers.Main) {
                            loadedImagesList.removeAt(position)

                            rejectPackageDialog?.let { dialog ->
                                (dialog.binding.rvThumbnails.adapter as ThumbnailsAdapter).deleteItem(
                                    position
                                )
                                if (loadedImagesList.isEmpty()) {
                                    dialog.binding.containerThumbnails.visibility =
                                        View.GONE
                                }
                            }

                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
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

}