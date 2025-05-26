package com.logestechs.driver.ui.barcodeScanner

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.BarcodeRequestBody
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.data.model.Bin
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.data.model.WarehouseLocation
import com.logestechs.driver.databinding.ActivityFulfilmentReturnOrderBarcodeScannerBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.FulfilmentOrderItemCellAdapter
import com.logestechs.driver.utils.adapters.ReturnedItemCellAdapter
import com.logestechs.driver.utils.adapters.ScannedShippingPlanItemCellAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.dialogs.ItemQuantityDialog
import com.logestechs.driver.utils.dialogs.RejectItemDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import com.logestechs.driver.utils.interfaces.RejectItemDialogListener
import com.logestechs.driver.utils.interfaces.ScannedShippingPlanItemCardListener
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FulfilmentReturnScanMode {
    BIN,
    ITEM_INTO_BIN
}

class FulfilmentReturnOrderBarcodeScannerActivity :
    LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener, RejectItemDialogListener, ScannedShippingPlanItemCardListener {

    private lateinit var binding: ActivityFulfilmentReturnOrderBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var scannedBarcode = ""

    private var selectedScanMode: FulfilmentReturnScanMode? = null
    private var selectedFulfilmentOrder: FulfilmentOrder? = null
    private var returnedItemsList: ArrayList<ProductItem?> = ArrayList()

    private var scannedWarehouseLocation: WarehouseLocation? = null
    private var scannedBin: Bin? = null
    private var isBinScan = true
    private var isReject: Boolean = false
    private var productBarcode: String? = null

    var loadedImagesList: java.util.ArrayList<LoadedImage> = java.util.ArrayList()
    private var isCameraAction = false
    var mCurrentPhotoPath: String? = null
    var selectedPodImageUri: Uri? = null
    var rejectItemDialog: RejectItemDialog? = null

    var rejectItemDialogListener: RejectItemDialogListener? = null

    private var isBarcodeScanningAllowed = true

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentReturnOrderBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            // Use built-in scanner, it goes for dispatchKeyEvent
        } else {
            initialiseDetectorsAndSources()
        }
        initListeners()
        initUI()
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedScanMode =
                extras.getSerializable(IntentExtrasKeys.FULFILMENT_RETURN_ORDER_SCAN_MODE.name) as FulfilmentReturnScanMode
            selectedFulfilmentOrder =
                extras.getParcelable(IntentExtrasKeys.FULFILMENT_ORDER.name) as? FulfilmentOrder
            callGetReturnedItems()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )

        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
        }
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            if (e.characters != null && e.characters.isNotEmpty()) {
                handleDetectedBarcode(e.characters)
            }
            return super.dispatchKeyEvent(e)
        } else {
            if (e.keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                return true
            }
            val action = e.action
            val keyCode = e.keyCode
            val character = e.unicodeChar.toChar()

            if (action == KeyEvent.ACTION_DOWN &&
                keyCode != KeyEvent.KEYCODE_ENTER &&
                character != '\t' &&
                character != '\n' &&
                character != '\u0000'
            ) {
                val pressedKey = character
                scannedBarcode += pressedKey
            }
            if (action == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_ENTER || character == '\t' || character == '\n' || character == '\u0000')
            ) {
                if (!scannedItemsHashMap.containsKey(scannedBarcode)) {
                    scannedItemsHashMap[scannedBarcode] = scannedBarcode
                    executeBarcodeAction(scannedBarcode)
                }
                scannedBarcode = ""
            }
            return false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (Helper.isCameraPermissionNeeded(this)
                ) {
                    Helper.showAndRequestCameraDialog(this)
                } else {
                    cameraSource?.start(binding.surfaceView.holder)
                }
            } else {
                if (Helper.shouldShowCameraPermissionDialog(this)) {
                    Helper.showAndRequestCameraDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_camera_permission)
                    )
                }
            }
        }
    }

    private fun initialiseDetectorsAndSources() {

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.surfaceView.holder?.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (Helper.isCameraPermissionNeeded(this@FulfilmentReturnOrderBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@FulfilmentReturnOrderBarcodeScannerActivity)
                    } else {
                        cameraSource?.start(binding.surfaceView.holder)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }
        })
        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val scannedBarcode = barcodes.valueAt(0).displayValue
                    if (scannedBarcode != currentBarcodeRead) {
                        confirmCounter = 0
                        currentBarcodeRead = scannedBarcode
                    } else {
                        confirmCounter++
                        if (confirmCounter >= confirmTarget) {
                            currentBarcodeRead = null
                            confirmCounter = 0
                            handleDetectedBarcode(scannedBarcode)
                        }
                    }
                }
            }
        })
    }

    private fun openCamera(): String? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this@FulfilmentReturnOrderBarcodeScannerActivity.packageManager!!) != null) {
            var photoFile: File? = null
            photoFile = try {
                Helper.createImageFile(this as LogesTechsActivity)
            } catch (ex: IOException) {
                return ""
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this@FulfilmentReturnOrderBarcodeScannerActivity.applicationContext!!,
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
                            super.getContext(),
                            getString(R.string.error_image_capture_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        super.getContext(),
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
                            super.getContext(),
                            getString(R.string.error_image_loading),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        super.getContext(),
                        getString(R.string.error_image_loading),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            else -> {}
        }
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentReturnOrderBarcodeScannerActivity)
            adapter =
                ReturnedItemCellAdapter(
                    list = returnedItemsList,
                    listener = this@FulfilmentReturnOrderBarcodeScannerActivity,
                    loadedImagesList
                )
        }
    }

    private fun initListeners() {
        binding.buttonSwitchBinAndLocation.setOnClickListener(this)
        binding.buttonDone.setOnClickListener(this)
    }

    private fun initUI() {
        handleSelectedScanMode()
    }

    private fun handleSelectedScanMode() {
        scannedItemsHashMap.clear()
        binding.buttonSwitchBinAndLocation.visibility = View.GONE
        binding.containerSubTitle.visibility = View.GONE
        when (selectedScanMode) {
            FulfilmentReturnScanMode.BIN -> {
                hideScannedItemsContainer()
                if (isReject) {
                    binding.textTitle.text =
                        getString(R.string.please_scan_damaged_location_barcode)
                    scannedBin?.barcode = null
                } else {
                    isBinScan = true
                    binding.buttonSwitchBinAndLocation.visibility = View.VISIBLE
                    binding.containerSubTitle.visibility = View.VISIBLE
                    binding.textSubTitle.text = selectedFulfilmentOrder?.barcode
                    binding.textTitle.text = getString(R.string.please_scan_bin_barcode)
                }
            }

            FulfilmentReturnScanMode.ITEM_INTO_BIN -> {
                showScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_items)
            }

            null -> return
            else -> {}
        }
    }

    private fun handleDetectedBarcode(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
            toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            vibrate()
        }
    }

    private fun executeBarcodeAction(barcode: String) {
        if (!isBarcodeScanningAllowed) {
            return
        }
        when (selectedScanMode) {
            FulfilmentReturnScanMode.BIN -> {
                if (isBinScan) {
                    callGetBin(barcode)
                } else {
                    callGetWarehouseLocation(barcode)
                }
            }

            FulfilmentReturnScanMode.ITEM_INTO_BIN -> {
                callSortReturnedItem(barcode)
            }

            null -> return
            else -> {}
        }
    }

    private fun showScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.VISIBLE
        binding.buttonDone.visibility = View.VISIBLE
    }

    private fun hideScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.GONE
    }

    //APIs
    private fun callGetReturnedItems() {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getReturnedItems(selectedFulfilmentOrder?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            returnedItemsList = ArrayList(response.body()!!.data)
                            initRecycler()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
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
            this.runOnUiThread {
                hideWaitDialog()
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_check_internet_connection)
                )
            }
        }
    }

    private fun callGetBin(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getBin(
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = FulfilmentReturnScanMode.ITEM_INTO_BIN
                            scannedBin = response.body()
                            handleSelectedScanMode()
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
                    scannedItemsHashMap.remove(barcode)
                } catch (e: Exception) {
                    scannedItemsHashMap.remove(barcode)
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
            this.runOnUiThread {
                hideWaitDialog()
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_check_internet_connection)
                )
            }
        }
    }

    private fun callGetWarehouseLocation(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = if (!isReject) {
                        ApiAdapter.apiClient.getWarehouseLocation(
                            barcode
                        )
                    } else {
                        ApiAdapter.apiClient.getWarehouseDamagedLocationForReturn(
                            barcode,
                            selectedFulfilmentOrder?.customerId
                        )
                    }
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = FulfilmentReturnScanMode.ITEM_INTO_BIN
                            scannedWarehouseLocation = response.body()
                            handleSelectedScanMode()
                            if (isReject) {
                                loadedImagesList.clear()
                                rejectItemDialog = RejectItemDialog(
                                    super.getContext(),
                                    this@FulfilmentReturnOrderBarcodeScannerActivity,
                                    loadedImagesList,
                                    productBarcode
                                )
                                rejectItemDialog?.showDialog()
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
                    scannedItemsHashMap.remove(barcode)
                } catch (e: Exception) {
                    scannedItemsHashMap.remove(barcode)
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
            this.runOnUiThread {
                hideWaitDialog()
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_check_internet_connection)
                )
            }
        }
    }

    private fun callSortReturnedItem(barcode: String) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.sortReturnedItemIntoBin(
                        barcode,
                        selectedFulfilmentOrder?.customerId!!,
                        scannedBin?.id,
                        scannedWarehouseLocation?.id,
                        selectedFulfilmentOrder?.id
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            (binding.rvScannedBarcodes.adapter as ReturnedItemCellAdapter).removeItemByBarcode(
                                barcode
                            )
                            if ((binding.rvScannedBarcodes.adapter as ReturnedItemCellAdapter)
                                    .getItemCount() == 0
                            ) {
                                onBackPressed()
                            }
                        }
                    } else {
                        scannedItemsHashMap.remove(barcode)
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
                    scannedItemsHashMap.remove(barcode)
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
            this.runOnUiThread {
                hideWaitDialog()
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_check_internet_connection)
                )
            }
        }
    }

    private fun callRejectItem(rejectItemRequestBody: RejectItemRequestBody?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            rejectItemRequestBody?.locationId = scannedWarehouseLocation?.id
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectReturnedItem(
                        scannedWarehouseLocation?.id,
                        selectedFulfilmentOrder?.id,
                        rejectItemRequestBody
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
                            (binding.rvScannedBarcodes.adapter as ReturnedItemCellAdapter).removeItemByBarcode(
                                rejectItemRequestBody?.barcode
                            )
                            if ((binding.rvScannedBarcodes.adapter as ReturnedItemCellAdapter)
                                    .getItemCount() == 0
                            ) {
                                onBackPressed()
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
            this.runOnUiThread {
                hideWaitDialog()
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_check_internet_connection)
                )
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

                    val response = ApiAdapter.apiClient.uploadPodImageForRejectedItem(
                        rejectItemDialog?.barcode!!,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                            loadedImagesList[loadedImagesList.size - 1].imageUrl =
                                response.body()?.fileUrl
                            (rejectItemDialog?.binding?.rvThumbnails?.adapter as ThumbnailsAdapter).updateItem(
                                loadedImagesList.size - 1
                            )
                            rejectItemDialog?.binding?.containerThumbnails?.visibility =
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_insert_barcode -> {
                InsertBarcodeDialog(this, this).showDialog()
            }

            R.id.button_switch_bin_and_location -> {
                isBinScan = !isBinScan
                if (isBinScan) {
                    binding.textTitle.text = getString(R.string.please_scan_bin_barcode)
                } else {
                    binding.textTitle.text = getString(R.string.please_scan_location_barcode)
                }
            }

            R.id.button_done -> {
                onBackPressed()
            }
        }

    }

    override fun onBarcodeInserted(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
        }
    }

    override fun onItemRejected(rejectItemRequestBody: RejectItemRequestBody) {
        callRejectItem(rejectItemRequestBody)
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
    }

    override fun onShowRejectItemDialog(barcode: String) {
        isBinScan = false
        isReject = true
        selectedScanMode = FulfilmentReturnScanMode.BIN
        productBarcode = barcode
        handleSelectedScanMode()
    }
}