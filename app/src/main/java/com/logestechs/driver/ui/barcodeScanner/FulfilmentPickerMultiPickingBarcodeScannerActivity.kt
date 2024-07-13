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
import com.logestechs.driver.api.responses.SortItemIntoToteResponse
import com.logestechs.driver.data.model.*
import com.logestechs.driver.databinding.ActivityFulfilmentPickerBarcodeScannerBinding
import com.logestechs.driver.databinding.ActivityFulfilmentPickerMultiPickBarcodeScannerBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.FulfilmentOrderCellAdapter
import com.logestechs.driver.utils.adapters.FulfilmentOrderItemCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.FulfilmentOrderCardListener
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class FulfilmentPickerMultiPickingBarcodeScannerActivity :
    LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener, FulfilmentOrderCardListener {
    private lateinit var binding: ActivityFulfilmentPickerMultiPickBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var scannedBarcode = ""

    private var selectedScanMode: FulfilmentPickerScanMode? = null

    private var scannedTote: Bin? = null
    private var selectedFulfilmentOrder: FulfilmentOrder? = null
    private var selectedFulfilmentOrders: ArrayList<FulfilmentOrder?>? = null

    private var fulfilmentOrder: FulfilmentOrder? = null

    private var scanCount = 0
    private var scannedTotes: ArrayList<Bin?>? = null

    private var selectedOrderBarcode: String? = null
    private var orderIds: List<Long?>? = null
    private var fulfilmentOrderItemsToShow: ArrayList<FulfilmentOrder?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentPickerMultiPickBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        fulfilmentOrder = intent.getParcelableExtra(IntentExtrasKeys.FULFILMENT_ORDER.name)
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            // Use built-in scanner, it goes for dispatchKeyEvent
        } else {
            initialiseDetectorsAndSources()
        }
        initRecycler()
        initListeners()
        initUI()
    }

    private fun initUI() {
        handleSelectedScanMode()
    }

    private fun handleSelectedScanMode() {
        scannedItemsHashMap.clear()
        when (selectedScanMode) {
            FulfilmentPickerScanMode.TOTE -> {

            }

            FulfilmentPickerScanMode.ORDER_INTO_TOTE -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_tote)
            }

            FulfilmentPickerScanMode.ITEM_INTO_TOTE -> {
                showScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_items)
            }

            null -> showScannedOrdersContainer()
        }
    }

    private fun showScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.VISIBLE
        binding.containerDoneButton.visibility = View.VISIBLE
        binding.containerSurfaceView.visibility = View.VISIBLE
        binding.containerTitle.visibility = View.GONE
        binding.rvScannedItems.visibility = View.VISIBLE
        binding.rvScannedOrders.visibility = View.GONE
    }

    private fun hideScannedItemsContainer() {
        binding.containerScannedItems.visibility = View.GONE
        binding.containerDoneButton.visibility = View.GONE
        binding.containerSurfaceView.visibility = View.VISIBLE
    }

    private fun showScannedOrdersContainer() {
        binding.containerSurfaceView.visibility = View.GONE
        binding.containerDoneButton.visibility = View.GONE
        binding.buttonInsertBarcode.visibility = View.GONE
        binding.rvScannedItems.visibility = View.GONE
        binding.rvScannedOrders.visibility = View.VISIBLE
        binding.containerScannedItems.visibility = View.VISIBLE
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedFulfilmentOrders =
                extras.getSerializable(IntentExtrasKeys.FULFILMENT_ORDERS.name) as? ArrayList<FulfilmentOrder?>?
            orderIds = selectedFulfilmentOrders?.map { it?.id }
            fulfilmentOrderItemsToShow = selectedFulfilmentOrders
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initRecycler() {
        binding.rvScannedOrders.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentPickerMultiPickingBarcodeScannerActivity)
            adapter =
                FulfilmentOrderCellAdapter(
                    (selectedFulfilmentOrders?: ArrayList()),
                    this@FulfilmentPickerMultiPickingBarcodeScannerActivity,
                    this@FulfilmentPickerMultiPickingBarcodeScannerActivity
                )
        }

        binding.rvScannedItems.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentPickerMultiPickingBarcodeScannerActivity)

            val allProductItems = selectedFulfilmentOrders
                ?.flatMap { it?.items.orEmpty() }
                ?: emptyList()

            adapter = FulfilmentOrderItemCellAdapter(
                allProductItems as ArrayList<ProductItem?>,
                this@FulfilmentPickerMultiPickingBarcodeScannerActivity
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
            val vibrator = getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
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
                    if (Helper.isCameraPermissionNeeded(this@FulfilmentPickerMultiPickingBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@FulfilmentPickerMultiPickingBarcodeScannerActivity)
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
            FulfilmentPickerScanMode.TOTE -> {

            }

            FulfilmentPickerScanMode.ORDER_INTO_TOTE -> {
                callScanOrderIntoTote(barcode)
            }

            FulfilmentPickerScanMode.ITEM_INTO_TOTE -> {
                callScanItemIntoTote(barcode)
            }

            null -> return
        }
    }

    //APIs
    private fun callScanItemIntoTote(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanItemsIntoTote(
                        orderIds!!,
                        null,
                        null,
                        BarcodeRequestBody(barcode)
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val body = response.body()
                            val scrollPosition =
                                (binding.rvScannedItems.adapter as FulfilmentOrderItemCellAdapter).scanItem(
                                    body?.sku
                                )
                            binding.rvScannedItems.smoothScrollToPosition(scrollPosition)
                            (binding.rvScannedItems.adapter as FulfilmentOrderItemCellAdapter).highlightItem(
                                scrollPosition
                            )
                            if ((binding.rvScannedItems.adapter as FulfilmentOrderItemCellAdapter)
                                    .getCount() == 0
                            ) {
                                onBackPressed()
                            }
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

    private fun callScanOrderIntoTote(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanOrderIntoTote(
                        selectedFulfilmentOrders?.get(scanCount)?.id,
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (selectedFulfilmentOrders?.size == 1) {
                                selectedScanMode = FulfilmentPickerScanMode.ITEM_INTO_TOTE
                                scanCount = 0
                            } else {
                                selectedScanMode = null
                            }
                            (binding.rvScannedOrders.adapter as FulfilmentOrderCellAdapter).removeOrder(
                                selectedFulfilmentOrder?.barcode!!
                            )
                            handleSelectedScanMode()
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
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done -> {
                onBackPressed()
            }

            R.id.button_insert_barcode -> {
                InsertBarcodeDialog(this, this).showDialog()
            }
        }

    }

    override fun onBarcodeInserted(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
        }
    }

    override fun onScanToteForOrder(fulfilmentOrder: FulfilmentOrder) {
        selectedScanMode = FulfilmentPickerScanMode.ORDER_INTO_TOTE
        selectedFulfilmentOrder = fulfilmentOrder
        handleSelectedScanMode()
    }
}