package com.logestechs.driver.ui.barcodeScanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.BarcodeRequestBody
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.api.requests.RejectedRequestBody
import com.logestechs.driver.api.responses.SortItemIntoBinResponse
import com.logestechs.driver.data.model.*
import com.logestechs.driver.databinding.ActivityFulfilmentSorterBarcodeScannerBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.ScannedShippingPlanItemCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.dialogs.ItemQuantityDialog
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import com.logestechs.driver.utils.interfaces.ItemQuantityDialogListener
import com.logestechs.driver.utils.interfaces.ScannedShippingPlanItemCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException


enum class FulfilmentSorterScanMode {
    LOCATION,
    BIN_INTO_LOCATION,
    BIN,
    ITEM_INTO_BIN,
    SHIPPING_PLAN
}

class FulfilmentSorterBarcodeScannerActivity :
    LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener, ScannedShippingPlanItemCardListener, SetTimeSpent.DataListener
    , ItemQuantityDialogListener {
    private lateinit var binding: ActivityFulfilmentSorterBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var scannedBarcode = ""

    private var selectedScanMode: FulfilmentSorterScanMode? = null

    private var scannedWarehouseLocation: WarehouseLocation? = null
    private var scannedShippingPlan: ShippingPlan? = null
    private var scannedBin: Bin? = null
    private var isBinScan = true
    private var hours: Double? = null
    private var rejectedItems: Int? = null
    private var isReject: Boolean = false
    private var flagLocation: Boolean = false

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentSorterBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        initialiseDetectorsAndSources()
        initRecycler()
        initListeners()
        initUI()
    }

    private fun initUI() {
        handleSelectedScanMode()
    }

    private fun handleSelectedScanMode() {
        scannedItemsHashMap.clear()
        binding.buttonSwitchBinAndLocation.visibility = View.GONE
        binding.containerSubTitle.visibility = View.GONE
        when (selectedScanMode) {
            FulfilmentSorterScanMode.LOCATION -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_location_barcode)
            }

            FulfilmentSorterScanMode.BIN_INTO_LOCATION -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_bin_barcode)
            }

            FulfilmentSorterScanMode.BIN -> {
                hideScannedItemsContainer()
                if (isReject) {
                    binding.textTitle.text =
                        getString(R.string.please_scan_damaged_location_barcode)
                    scannedBin?.barcode = null
                } else {
                    isBinScan = true
                    binding.buttonSwitchBinAndLocation.visibility = View.VISIBLE
                    binding.containerSubTitle.visibility = View.VISIBLE
                    binding.textSubTitle.text = scannedShippingPlan?.barcode
                    binding.textTitle.text = getString(R.string.please_scan_bin_barcode)
                }
            }

            FulfilmentSorterScanMode.ITEM_INTO_BIN -> {
                showScannedItemsContainer()
                updateShippingPlanCountValues(scannedShippingPlan?.shippingPlanDetails)
                binding.textScannedBin.text =
                    scannedBin?.barcode ?: scannedWarehouseLocation?.barcode
                binding.textTitle.text = getString(R.string.please_scan_items)
                if (isReject && flagLocation && !isBinScan) {
                    binding.buttonNewBin.text = getString(R.string.button_new_location)
                    binding.itemsCounts.visibility = View.GONE
                    binding.textRejectedItems.visibility = View.VISIBLE
                } else if (isReject || !isBinScan) {
                    binding.buttonNewBin.text = getString(R.string.button_new_location)
                }
            }

            FulfilmentSorterScanMode.SHIPPING_PLAN -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_shipping_plan_barcode)
            }

            null -> return
            else -> {}
        }
    }

    private fun showScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.VISIBLE
        binding.containerDoneButton.visibility = View.VISIBLE
    }

    private fun hideScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.GONE
        binding.containerDoneButton.visibility = View.GONE
    }

    private fun updateShippingPlanCountValues(shippingPlanDetails: ShippingPlanDetails?) {
        binding.textReceived.text = shippingPlanDetails?.sorted.toString()
        binding.textUnreceived.text = shippingPlanDetails?.unsorted.toString()
        binding.textRejected.text = shippingPlanDetails?.rejected.toString()
        rejectedItems = shippingPlanDetails?.rejected
        scannedShippingPlan?.shippingPlanDetails = shippingPlanDetails
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedScanMode =
                extras.getSerializable(IntentExtrasKeys.FULFILMENT_SORTER_SCAN_MODE.name) as FulfilmentSorterScanMode
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonNewBin.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
        binding.buttonSwitchBinAndLocation.setOnClickListener(this)
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentSorterBarcodeScannerActivity)
            adapter =
                ScannedShippingPlanItemCellAdapter(
                    list = ArrayList(),
                    listener = this@FulfilmentSorterBarcodeScannerActivity,
                    rejectItemDialogListener = null, // provide the appropriate value or null for 'rejectItemDialogListener'
                    productItem = null // provide the appropriate value or null for 'pkg'
                )
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
                    if (Helper.isCameraPermissionNeeded(this@FulfilmentSorterBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@FulfilmentSorterBarcodeScannerActivity)
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

    private fun handleDetectedBarcode(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
            toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            vibrate()
        }
    }

    private fun executeBarcodeAction(barcode: String?) {
        when (selectedScanMode) {
            FulfilmentSorterScanMode.LOCATION -> {
                callGetWarehouseLocation(barcode)
            }

            FulfilmentSorterScanMode.BIN_INTO_LOCATION -> {
                callSortBinIntoWarehouseLocation(barcode)
            }

            FulfilmentSorterScanMode.BIN -> {
                if (isBinScan) {
                    callGetBin(barcode)
                } else {
                    callGetWarehouseLocation(barcode)
                }
            }

            FulfilmentSorterScanMode.ITEM_INTO_BIN -> {
                if (!companyConfigurations?.isSortAndPickFulfillmentItemsByScanningProductBarcode!!) {
                    if (isBinScan) {
                        callSortItemIntoBin(barcode)
                    } else {

                        callSortItemIntoLocation(barcode)
                    }
                } else {
                    vibrate()
                    runOnUiThread {
                        ItemQuantityDialog(
                            this@FulfilmentSorterBarcodeScannerActivity,
                            this@FulfilmentSorterBarcodeScannerActivity,
                            barcode!!
                        ).showDialog()
                        scannedItemsHashMap.remove(barcode)
                    }
                }
            }

            FulfilmentSorterScanMode.SHIPPING_PLAN -> {
                callGetShippingPlan(barcode)
            }

            null -> return
            else -> {}
        }
    }

    //APIs
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
                        ApiAdapter.apiClient.getWarehouseDamagedLocation(
                            barcode,
                            scannedShippingPlan?.id
                        )
                    }
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (selectedScanMode == FulfilmentSorterScanMode.BIN) {
                                selectedScanMode = FulfilmentSorterScanMode.ITEM_INTO_BIN
                                scannedWarehouseLocation = response.body()
                                handleSelectedScanMode()
                            } else {
                                selectedScanMode = FulfilmentSorterScanMode.BIN_INTO_LOCATION
                                scannedWarehouseLocation = response.body()
                                handleSelectedScanMode()
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

    private fun callSortBinIntoWarehouseLocation(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.sortBinIntoLocation(
                        scannedWarehouseLocation?.id,
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.bin_location_assigned_successfully)
                            )
                            selectedScanMode = FulfilmentSorterScanMode.LOCATION
                            scannedWarehouseLocation = null
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

    private fun callGetShippingPlan(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getShippingPlan(
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = FulfilmentSorterScanMode.BIN
                            scannedShippingPlan = response.body()
                            scannedShippingPlan?.groupShippingPlanDetails()
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
                            selectedScanMode = FulfilmentSorterScanMode.ITEM_INTO_BIN
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

    private fun callSortItemIntoBin(barcode: String?, quantity: Int? = null) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.sortItemIntoBin(
                        scannedBin?.id,
                        scannedShippingPlan?.id,
                        quantity,
                        BarcodeRequestBody(barcode)
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val response = response.body()
                            (binding.rvScannedBarcodes.adapter as ScannedShippingPlanItemCellAdapter).insertItem(
                                response?.itemDetails
                            )
                            binding.rvScannedBarcodes.smoothScrollToPosition(0)
                            updateShippingPlanCountValues(response?.shippingPlanDetails)
                        }
                        scannedItemsHashMap.remove(barcode)
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

    private fun callSortItemIntoLocation(barcode: String?, quantity: Int? = null) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if(isReject){
                        var response = ApiAdapter.apiClient.sortRejectedItemIntoLocation(
                            scannedWarehouseLocation?.id,
                            barcode!!
                        )
                        if (response?.isSuccessful == true && response.body() != null) {
                            if (isReject) {
                                rejectedItems = rejectedItems?.minus(1)
                            }
                            scannedItemsHashMap.remove(barcode)
                            withContext(Dispatchers.Main) {
                                val response = response?.body()
                                (binding.rvScannedBarcodes.adapter as ScannedShippingPlanItemCellAdapter).insertItem(
                                    response?.itemDetails?.let { listOf(it) } ?: emptyList(),
                                    true
                                )
                                binding.rvScannedBarcodes.smoothScrollToPosition(0)
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
                    }else{
                        var response = ApiAdapter.apiClient.sortItemIntoLocation(
                            scannedWarehouseLocation?.id,
                            scannedShippingPlan?.id,
                            quantity,
                            BarcodeRequestBody(itemBarcode = barcode)
                        )
                        if (response?.isSuccessful == true && response.body() != null) {
                            if(isReject){
                                rejectedItems = rejectedItems?.minus(1)
                            }
                            withContext(Dispatchers.Main) {
                                val response = response.body()
                                (binding.rvScannedBarcodes.adapter as ScannedShippingPlanItemCellAdapter).insertItem(
                                    response?.itemDetails
                                )
                                binding.rvScannedBarcodes.smoothScrollToPosition(0)
                                updateShippingPlanCountValues(response?.shippingPlanDetails)
                                scannedItemsHashMap.remove(barcode)
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
                    }
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
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
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.rejectItem(
                        scannedBin?.id,
                        scannedWarehouseLocation?.id,
                        scannedShippingPlan?.id,
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
                            updateShippingPlanCountValues(response.body()?.shippingPlanDetails)
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
                    scannedItemsHashMap.remove(response?.body()!!.itemDetails?.barcode)
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

    private fun callSetTimeSpent(time: Double?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.setTimeSpent(
                        scannedShippingPlan?.id,
                        time
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Log.d("MyFragment", "OK!")
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done -> {
                if (rejectedItems!! > 0) {
                    scannedWarehouseLocation = null
                    selectedScanMode = FulfilmentSorterScanMode.BIN
                    isBinScan = false
                    isReject = true
                    flagLocation = true
                    (binding.rvScannedBarcodes.adapter as ScannedShippingPlanItemCellAdapter).clearList()
                    handleSelectedScanMode()
                } else {
                    val fragment = SetTimeSpent()
                    isReject = false
                    fragment.isCancelable = false
                    fragment.show(supportFragmentManager, "SetTimeSpentDialog")
                }
            }

            R.id.button_new_bin -> {
                scannedBin = null
                selectedScanMode = FulfilmentSorterScanMode.BIN
                (binding.rvScannedBarcodes.adapter as ScannedShippingPlanItemCellAdapter).clearList()
                handleSelectedScanMode()
            }

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
        }

    }

    override fun onBarcodeInserted(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
        }
    }

    override fun rejectItem(rejectItemRequestBody: RejectItemRequestBody) {
        callRejectItem(rejectItemRequestBody)
    }

    override fun onDataReceived(data: Double?) {
        hours = data
        callSetTimeSpent(hours)
    }

    override fun onQuantityInserted(quantity: Int, barcode: String) {
        if (isBinScan) {
            callSortItemIntoBin(barcode, quantity)
        } else {
            callSortItemIntoLocation(barcode, quantity)
        }
    }
}
