package com.logestechs.driver.ui.deliverToWarehouse.deliverOrderToWarehouseScreen

import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.requests.DeliverToWarehouseRequestBody
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.databinding.ActivityOrderDeliverToWarehouseBinding
import com.logestechs.driver.ui.deliverToWarehouse.DeliverToWarehouseActivity
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.ConfirmationDialogAction
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.ConfirmationDialogActionListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
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


class OrderDeliverToWarehouseActivity : LogesTechsActivity(), View.OnClickListener, ThumbnailsListListener,
    ConfirmationDialogActionListener {
    private lateinit var binding: ActivityOrderDeliverToWarehouseBinding

    private var path: String? = null
    private var file: File? = null
    private var gestureTouch = false
    private var order: FulfilmentOrder? = null

    private var selectedPodImageUri: Uri? = null
    private var mCurrentPhotoPath: String? = null

    private var loadedImagesList: ArrayList<LoadedImage> = ArrayList()

    private var isCameraAction = false

    private var notes: String? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDeliverToWarehouseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        initializeUi()
        initListeners()
        initData()
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
            adapter = ThumbnailsAdapter(loadedImagesList, this@OrderDeliverToWarehouseActivity)
        }
    }

    private fun isSignatureEntered(): Boolean {
        return binding.gestureViewSignature.gesture != null && binding.gestureViewSignature.gesture.length > 0
    }

    private fun initData() {
        binding.itemReceiverName.textItem.text = order?.receiverName
        binding.itemReceiverAddress.textItem.text = order?.receiverAddress?.toStringAddress()
        binding.itemPackageBarcode.textItem.text = order?.barcode
        binding.textCod.text = order?.cod?.format()

        if (order?.notes?.trim().isNullOrEmpty()) {
            binding.itemNotes.root.visibility = View.GONE
        } else {
            binding.itemNotes.root.visibility = View.VISIBLE
            binding.itemNotes.textItem.text = order?.notes
            notes = order?.notes
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

        binding.itemPackageBarcode.buttonCopy.setOnClickListener {
            Helper.copyTextToClipboard(this, order?.barcode)
        }

        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.buttonClearSignature.setOnClickListener(this)
        binding.buttonDeliverOrder.setOnClickListener(this)
        binding.buttonCaptureImage.setOnClickListener(this)
        binding.buttonLoadImage.setOnClickListener(this)
        binding.buttonContextMenu.setOnClickListener(this)

        if (SharedPreferenceWrapper.getNotificationsCount() == "0") {
            binding.toolbarMain.notificationCount.visibility = View.GONE
        }
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            order = extras.getParcelable(IntentExtrasKeys.FULFILMENT_ORDER.name)
        }
    }

    private fun validateInput(): Boolean {
        if (!isSignatureEntered()) {
            Helper.showErrorMessage(this, getString(R.string.error_enter_signature))
            return false
        }
        if (loadedImagesList.isEmpty()) {
            Helper.showErrorMessage(
                this,
                getString(R.string.error_add_attachments)
            )
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

            AppConstants.REQUEST_VERIFY_PACKAGE -> {
                if (resultCode == RESULT_OK) {
                    val verificationStatus = data?.getBooleanExtra("verificationStatus", false)
                    if (verificationStatus == true) {
                        handlePackageDelivery()
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_camera_and_storage_permissions)
                        )
                    }
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

    private fun uploadPackageSignature() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Create bitmap from gesture view
                    val bitmap = Bitmap.createBitmap(
                        binding.gestureViewSignature.width,
                        binding.gestureViewSignature.height,
                        Bitmap.Config.ARGB_8888
                    ).apply {
                        val canvas = Canvas(this)
                        binding.gestureViewSignature.draw(canvas)
                    }

                    // Save the bitmap to a file
                    file?.createNewFile()
                    FileOutputStream(file).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }

                    // Resize and compress the bitmap to reasonable size
                    val bytes = ByteArrayOutputStream().apply {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, AppConstants.IMAGE_FULL_QUALITY, this)
                    }

                    val fileName = "${order?.id}__signature_image_${System.currentTimeMillis()}.jpg"
                    val fileUrl = "file://${file?.absolutePath}/$fileName"

                    hideWaitDialog()

                    // Create a LoadedImage object and append it to the list
                    val loadedImage = LoadedImage(imageUri = Uri.parse(fileUrl), imageUrl = fileUrl)
                    loadedImagesList.add(loadedImage)

                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())

                    withContext(Dispatchers.Main) {
                        Helper.showErrorMessage(
                            super.getContext(),
                            e.message?.takeIf { it.isNotEmpty() } ?: getString(R.string.error_general)
                        )
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

                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("en")).format(Date())
                    val imageFileName = "JPEG_$timeStamp"

                    val fileUrl = "file://${file.absolutePath}/$imageFileName.jpeg"

                    hideWaitDialog()

                    // Update the LoadedImage object and adapter
                    withContext(Dispatchers.Main) {
                        loadedImagesList[loadedImagesList.size - 1].imageUrl = fileUrl
                        (binding.rvThumbnails.adapter as ThumbnailsAdapter).updateItem(
                            loadedImagesList.size - 1
                        )
                        binding.containerThumbnails.visibility = View.VISIBLE
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
                        loadedImagesList.removeAt(position)
                        (binding.rvThumbnails.adapter as ThumbnailsAdapter).deleteItem(position)
                        if (loadedImagesList.isEmpty()) {
                            binding.containerThumbnails.visibility = View.GONE
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

    //Apis
    private fun callDeliverPackage() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val body: DeliverToWarehouseRequestBody? = DeliverToWarehouseRequestBody(
                        listOf(order?.id!!),
                        binding.etNotes.text.toString(),
                        getPodImagesUrls()
                    )
                    val response = ApiAdapter.apiClient.deliverToWarehouse(
                        body
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val returnIntent = Intent(this@OrderDeliverToWarehouseActivity, DeliverToWarehouseActivity::class.java)
                            startActivity(returnIntent)
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
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun handlePackageDelivery() {
        if (Helper.isStoragePermissionNeeded(this)) {
            Helper.showAndRequestStorageDialog(this)
        } else {
            callDeliverPackage()
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

            R.id.button_deliver_order -> {
                if (validateInput()) {
                        (this as LogesTechsActivity).showConfirmationDialog(
                            getString(R.string.warning_deliver_package),
                            order,
                            ConfirmationDialogAction.DELIVER_TO_WAREHOUSE,
                            this
                        )
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

            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }
        }
    }


    override fun onDeleteImage(position: Int) {
        callDeletePodImage(position)
    }

    override fun confirmAction(data: Any?, action: ConfirmationDialogAction) {
        if (action == ConfirmationDialogAction.DELIVER_TO_WAREHOUSE) {
            callDeliverPackage()
        }

    }
}