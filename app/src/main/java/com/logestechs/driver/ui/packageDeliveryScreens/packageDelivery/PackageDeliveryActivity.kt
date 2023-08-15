package com.logestechs.driver.ui.packageDeliveryScreens.packageDelivery

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.AddNoteRequestBody
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.requests.DeliverPackageRequestBody
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.PackageItemsToDeliver
import com.logestechs.driver.data.model.Status
import com.logestechs.driver.databinding.ActivityPackageDeliveryBinding
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.DeliveryType
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.PaymentType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.VerificationStatus
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.dialogs.DeliveryCodeVerificationDialog
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
import com.logestechs.driver.utils.interfaces.VerificationCodeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PackageDeliveryActivity : LogesTechsActivity(), View.OnClickListener, ThumbnailsListListener,
    VerificationCodeDialogListener {
    private lateinit var binding: ActivityPackageDeliveryBinding

    private var path: String? = null
    private var file: File? = null
    private var bitmap: Bitmap? = null
    private var gestureTouch = false
    private var pkg: Package? = null

    private var paymentTypeButtonsList: ArrayList<StatusSelector> = ArrayList()
    private var selectedPaymentType: StatusSelector? = null

    private var selectedDeliveryType: DeliveryType? = null


    private var selectedPodImageUri: Uri? = null
    private var mCurrentPhotoPath: String? = null

    private var loadedImagesList: ArrayList<LoadedImage> = ArrayList()

    private var isCameraAction = false

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    var items: List<PackageItemsToDeliver?>? = null
    private val checkedItems = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        initializeUi()
        initListeners()
        initData()
        initPaymentMethodsControls()
        fillButtonsList()
    }

    private fun initializeUi() {
        path =
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/signature.png"
        file = File(path ?: "")
        file?.delete()

        binding.gestureViewSignature.isHapticFeedbackEnabled = false
        binding.gestureViewSignature.cancelLongPress()
        binding.gestureViewSignature.cancelClearAnimation()

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(super.getContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@PackageDeliveryActivity)
        }

        if (companyConfigurations?.isPartialDeliveryEnabled == true) {
            binding.containerPartialDeliveryControls.visibility = View.VISIBLE
        }

        if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true) {
            binding.containerSignature.visibility = View.GONE
        }

        if (companyConfigurations?.isShowPaymentTypesWhenDriverDeliver == false) {
            binding.containerPaymentType.visibility = View.GONE
        }

        if (pkg?.shipmentType == PackageType.REGULAR.name) {
            binding.containerPaymentType.visibility = View.GONE
        }

        handleWarningText()
    }

    private fun handleWarningText() {
        var hasWarnings = false
        val warningText = StringBuilder()
        if (pkg?.shipmentType == PackageType.SWAP.name) {
            if (warningText.isNotEmpty()) {
                warningText.append("\n")
                warningText.append("\n")
            }
            warningText.append("*")
            warningText.append(getString(R.string.warning_pickup_returned_packages))
            hasWarnings = true
        }

        binding.textWarningMessage.text = warningText.toString()

        if (hasWarnings) {
            binding.containerWarningMessage.visibility = View.VISIBLE
        }
    }

    private fun isSignatureEntered(): Boolean {
        return binding.gestureViewSignature.gesture != null && binding.gestureViewSignature.gesture.length > 0
    }

    private fun initData() {
        items = pkg?.packageItemsToDeliver
        binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
        binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()
        binding.itemPackageBarcode.textItem.text = pkg?.barcode
        binding.textCod.text = pkg?.cod?.format()

        if (pkg?.notes?.trim().isNullOrEmpty()) {
            binding.itemNotes.root.visibility = View.GONE
        } else {
            binding.itemNotes.root.visibility = View.VISIBLE
            binding.itemNotes.textItem.text = pkg?.notes
        }

        if (companyConfigurations?.isPartialDeliveryEnabled == true) {
            selectedDeliveryType = DeliveryType.FULL
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initListeners() {
        binding.gestureViewSignature.addOnGestureListener(object :
            GestureOverlayView.OnGestureListener {
            override fun onGesture(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
            }

            override fun onGestureCancelled(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
            }

            override fun onGestureEnded(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
                binding.containerScrollView.isEnableScrolling = true
            }

            override fun onGestureStarted(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
                binding.containerScrollView.isEnableScrolling = false
                gestureTouch = motionEvent.action != MotionEvent.ACTION_MOVE
            }
        })

        if(companyConfigurations?.isSupportDeliveringPackageItemsPartially == true){
            binding.radioGroupPartialDelivery.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.radio_button_full_delivery) {
                    binding.containerPartialDeliveryNote.visibility = View.GONE
                    selectedDeliveryType = DeliveryType.FULL
                } else if (checkedId == R.id.radio_button_partial_delivery) {
                    binding.containerPartialDeliveryNote.visibility = View.VISIBLE
                    selectedDeliveryType = DeliveryType.PARTIAL

                    val checkBoxContainer = findViewById<LinearLayout>(R.id.check_box_container)
                    for (item in items!!) {
                        val checkBox = CheckBox(this)
                        checkBox.text = Html.fromHtml("${item?.name}, <b>Price:</b> ${item?.cod ?: 0}", Html.FROM_HTML_MODE_LEGACY)
                        checkBox.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        checkBox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                item?.status = Status.DELIVERED
                            } else {
                                item?.status = Status.RETURNED
                            }
                        }
                        checkBoxContainer.addView(checkBox)
                        checkChosen()
                    }
                }
            }
        }

        binding.itemPackageBarcode.buttonCopy.setOnClickListener {
            Helper.copyTextToClipboard(this, pkg?.barcode)
        }

        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.buttonClearSignature.setOnClickListener(this)
        binding.buttonDeliverPackage.setOnClickListener(this)
        binding.selectorCash.setOnClickListener(this)
        binding.selectorDigitalWallet.setOnClickListener(this)
        binding.selectorCheque.setOnClickListener(this)
        binding.selectorPrepaid.setOnClickListener(this)
        binding.selectorCardPayment.setOnClickListener(this)
        binding.selectorBankTransfer.setOnClickListener(this)
        binding.buttonCaptureImage.setOnClickListener(this)
        binding.buttonLoadImage.setOnClickListener(this)
        binding.buttonContextMenu.setOnClickListener(this)
    }

    private fun checkChosen(){
        for(item in items!!){
            if(item?.status == null){
                item?.status = Status.RETURNED
            }
        }
    }
    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            pkg = extras.getParcelable(IntentExtrasKeys.PACKAGE_TO_DELIVER.name)
        }
    }

    private fun initPaymentMethodsControls() {
        binding.selectorCash.makeSelected()
        selectedPaymentType = binding.selectorCash
        binding.selectorCash.enumValue = PaymentType.CASH
        binding.selectorDigitalWallet.enumValue = PaymentType.DIGITAL_WALLET
        binding.selectorCheque.enumValue = PaymentType.CHEQUE
        binding.selectorPrepaid.enumValue = PaymentType.PREPAID
        binding.selectorCardPayment.enumValue = PaymentType.CARD
        binding.selectorBankTransfer.enumValue = PaymentType.BANK_TRANSFER
    }

    private fun fillButtonsList() {
        paymentTypeButtonsList.add(binding.selectorCash)
        paymentTypeButtonsList.add(binding.selectorDigitalWallet)
        paymentTypeButtonsList.add(binding.selectorCheque)
        paymentTypeButtonsList.add(binding.selectorPrepaid)
        paymentTypeButtonsList.add(binding.selectorCardPayment)
        paymentTypeButtonsList.add(binding.selectorBankTransfer)
    }

    private fun unselectAllPaymentMethods() {
        for (item in paymentTypeButtonsList) {
            item.makeUnselected()
        }
    }

    private fun validateInput(): Boolean {
        if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled != true) {
            if (!isSignatureEntered()) {
                Helper.showErrorMessage(this, getString(R.string.error_enter_signature))
                return false
            }
        }

        if (selectedDeliveryType == DeliveryType.PARTIAL) {
            if (binding.etPartialDeliveryNote.text.toString().isEmpty()) {
                Helper.showErrorMessage(
                    super.getContext(),
                    getString(R.string.error_enter_partial_delivery_note)
                )
                return false
            }
        }

        var deliveredItemFound = false
        for (item in items!!) {
            if (item?.status == Status.DELIVERED) {
                deliveredItemFound = true
                break
            }
        }

        if (!deliveredItemFound) {
            Helper.showErrorMessage(this, getString(R.string.error_no_delivered_items))
            return false
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getPodImagesUrls(): List<String?>? {
        return if (loadedImagesList.isNotEmpty()) {
            val list: ArrayList<String?> = ArrayList()
            for (item in loadedImagesList) {
                list.add(item.imageUrl)
            }
            list
        } else {
            null
        }
    }

    //Media Picker methods
    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK)
        pickPhoto.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(
            pickPhoto,
            AppConstants.REQUEST_LOAD_PHOTO
        )
    }

    private fun openCamera(): String? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            var photoFile: File? = null
            photoFile = try {
                Helper.createImageFile(this)
            } catch (ex: IOException) {
                return ""
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    photoFile
                )
                val mCurrentPhotoPath = "file:" + photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                if (SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_TAKE_PHOTO -> if (resultCode == RESULT_OK) {
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
                            applicationContext,
                            getString(R.string.error_image_capture_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_image_capture_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_LOAD_PHOTO -> if (resultCode == RESULT_OK && data != null) {
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
                            applicationContext,
                            getString(R.string.error_image_loading),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_image_loading),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_SCAN_BARCODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    callAddPackageNote(
                        pkg?.id,
                        AddNoteRequestBody(
                            data.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name),
                            null,
                            packageId = pkg?.id
                        )
                    )
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
                if (Helper.isStorageAndCameraPermissionNeeded(this)
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
                if (Helper.shouldShowCameraAndStoragePermissionDialog(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_camera_and_storage_permissions)
                    )
                }
            }
        } else if (requestCode == AppConstants.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (Helper.isStoragePermissionNeeded(this)
                ) {
                    Helper.showAndRequestStorageDialog(this)
                } else {
                    uploadPackageSignature()
                }
            } else {
                if (Helper.shouldShowStoragePermissionDialog(this)) {
                    Helper.showAndRequestStorageDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_storage_permission)
                    )
                }
            }
        }
    }

    //Apis
    private fun uploadPackageSignature() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    bitmap = Bitmap.createBitmap(
                        binding.gestureViewSignature.width,
                        binding.gestureViewSignature.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap!!)
                    binding.gestureViewSignature.draw(canvas)
                    file?.createNewFile()
                    val fos = FileOutputStream(file)
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()

                    // resize and compress to reasonable size
                    val bytes = ByteArrayOutputStream()
                    bitmap?.compress(
                        Bitmap.CompressFormat.JPEG,
                        AppConstants.IMAGE_FULL_QUALITY,
                        bytes
                    )

                    val reqFile: RequestBody =
                        bytes.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "file",
                        pkg?.id.toString() +
                                "__signature_image" +
                                "_" + System.currentTimeMillis() +
                                ".jpg", reqFile
                    )

                    val response = ApiAdapter.apiClient.uploadPackageSignature(
                        pkg?.id ?: -1,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            callDeliverPackage(response.body()?.fileUrl)
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
                        .getBitmap(super.getContext().contentResolver, Uri.fromFile(file))

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
                        pkg?.id ?: -1,
                        true,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                            loadedImagesList[loadedImagesList.size - 1].imageUrl =
                                response.body()?.fileUrl
                            (binding.rvThumbnails.adapter as ThumbnailsAdapter).updateItem(
                                loadedImagesList.size - 1
                            )
                            binding.containerThumbnails.visibility = View.VISIBLE
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
                            (binding.rvThumbnails.adapter as ThumbnailsAdapter).deleteItem(position)
                            if (loadedImagesList.isEmpty()) {
                                binding.containerThumbnails.visibility = View.GONE
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

    private fun callDeliverPackage(signatureUrl: String?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(super.getContext())
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    val latitude = location?.latitude
                    val longitude = location?.longitude
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            var note: String? = null
                            if (selectedDeliveryType == DeliveryType.PARTIAL) {
                                note = binding.etPartialDeliveryNote.text.toString()
                            }
                            val response = ApiAdapter.apiClient.deliverPackage(
                                pkg?.barcode,
                                selectedDeliveryType?.name,
                                note,
                                body = DeliverPackageRequestBody(
                                    pkg?.id,
                                    longitude,
                                    latitude,
                                    0.0,
                                    0.0,
                                    signatureUrl,
                                    getPodImagesUrls(),
                                    null,
                                    null,
                                    (selectedPaymentType?.enumValue as PaymentType).name,
                                    items
                                )
                            )
                            withContext(Dispatchers.Main) {
                                hideWaitDialog()
                            }
                            if (response?.isSuccessful == true && response.body() != null) {
                                withContext(Dispatchers.Main) {
                                    val returnIntent = Intent()
                                    setResult(RESULT_OK, returnIntent)
                                    finish()
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
                                    Helper.showErrorMessage(
                                        super.getContext(),
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        }
                    }
            }
        }else {
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

    private fun requestPinCodeSms(isFirstPin: Boolean = true) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.requestPinCodeSms(
                        pkg?.id
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            pkg?.verificationStatus = VerificationStatus.SENT.toString()
                            if (isFirstPin) {
                                showDeliveryCodeVerificationDialog()
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

    private fun showDeliveryCodeVerificationDialog() {
        DeliveryCodeVerificationDialog(super.getContext(), this, pkg).showDialog()
    }

    private fun needsPinVerification(): Boolean {
        if (companyConfigurations != null) {
            if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true || isSignatureEntered()) {
                if ((pkg?.shipmentType == PackageType.REGULAR.toString() || pkg?.cod == 0.0) && companyConfigurations?.isEnableDeliveryVerificationPinCodeForPkgs == true) {
                    return when (pkg?.verificationStatus) {
                        VerificationStatus.NOT_SENT.toString() -> {
                            requestPinCodeSms()
                            true
                        }

                        VerificationStatus.SENT.toString() -> {
                            showDeliveryCodeVerificationDialog()
                            true
                        }

                        VerificationStatus.VERIFIED.toString() -> {
                            false
                        }

                        else -> {
                            false
                        }
                    }
                } else if (companyConfigurations?.isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan != null && (pkg?.cod
                        ?: 0.0) >= companyConfigurations!!.isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan!!
                ) {
                    return when (pkg?.verificationStatus) {
                        VerificationStatus.NOT_SENT.toString() -> {
                            requestPinCodeSms()
                            true
                        }

                        VerificationStatus.SENT.toString() -> {
                            showDeliveryCodeVerificationDialog()
                            true
                        }

                        VerificationStatus.VERIFIED.toString() -> {
                            false
                        }

                        else -> {
                            false
                        }
                    }
                }
            } else {
                return false
            }
        } else {
            return false
        }
        return false
    }

    private fun handlePackageDelivery() {
        if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true) {
            callDeliverPackage(null)
        } else {
            if (Helper.isStoragePermissionNeeded(this)) {
                Helper.showAndRequestStorageDialog(this)
            } else {
                uploadPackageSignature()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_clear_signature -> {
                binding.gestureViewSignature.invalidate()
                binding.gestureViewSignature.clear(true)
                binding.gestureViewSignature.clearAnimation()
                binding.gestureViewSignature.cancelClearAnimation()
                gestureTouch = false
            }

            R.id.button_deliver_package -> {
                if (validateInput()) {
                    if (!needsPinVerification()) {
                        handlePackageDelivery()
                    }
                }
            }

            R.id.button_capture_image -> {
                isCameraAction = true
                if (Helper.isStorageAndCameraPermissionNeeded(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    mCurrentPhotoPath = openCamera()
                }
            }

            R.id.button_load_image -> {
                isCameraAction = false
                if (Helper.isStorageAndCameraPermissionNeeded(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    openGallery()
                }
            }

            R.id.selector_cash -> {
                unselectAllPaymentMethods()
                binding.selectorCash.makeSelected()
                selectedPaymentType = binding.selectorCash
            }

            R.id.selector_digital_wallet -> {
                unselectAllPaymentMethods()
                binding.selectorDigitalWallet.makeSelected()
                selectedPaymentType = binding.selectorDigitalWallet
            }

            R.id.selector_cheque -> {
                unselectAllPaymentMethods()
                binding.selectorCheque.makeSelected()
                selectedPaymentType = binding.selectorCheque
            }

            R.id.selector_prepaid -> {
                unselectAllPaymentMethods()
                binding.selectorPrepaid.makeSelected()
                selectedPaymentType = binding.selectorPrepaid
            }

            R.id.selector_card_payment -> {
                unselectAllPaymentMethods()
                binding.selectorCardPayment.makeSelected()
                selectedPaymentType = binding.selectorCardPayment
            }

            R.id.selector_bank_transfer -> {
                unselectAllPaymentMethods()
                binding.selectorBankTransfer.makeSelected()
                selectedPaymentType = binding.selectorBankTransfer
            }

            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }

            R.id.button_context_menu -> {
                binding.buttonContextMenu.setOnClickListener {
                    val popup = PopupMenu(super.getContext(), binding.buttonContextMenu)
                    popup.inflate(R.menu.package_delivery_context_menu)
                    popup.setOnMenuItemClickListener { item: MenuItem? ->
                        when (item?.itemId) {
                            R.id.action_scan_barcode -> {
                                val scanBarcode =
                                    Intent(super.getContext(), SingleScanBarcodeScanner::class.java)
                                this.startActivityForResult(
                                    scanBarcode,
                                    AppConstants.REQUEST_SCAN_BARCODE
                                )
                            }
                        }
                        true
                    }
                    popup.show()
                }
            }
        }
    }


    override fun onDeleteImage(position: Int) {
        callDeletePodImage(position)
    }

    override fun onPackageVerified() {
        handlePackageDelivery()
    }

    override fun onResendPinSms() {
        requestPinCodeSms(isFirstPin = false)
    }
}