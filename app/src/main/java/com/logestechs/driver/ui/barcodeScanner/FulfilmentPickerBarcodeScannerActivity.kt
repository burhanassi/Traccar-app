package com.logestechs.driver.ui.barcodeScanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
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
import com.logestechs.driver.data.model.*
import com.logestechs.driver.databinding.ActivityFulfilmentPickerBarcodeScannerBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.adapters.FulfilmentOrderItemCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

enum class FulfilmentPickerScanMode {
    TOTE,
    ITEM_INTO_TOTE
}

class FulfilmentPickerBarcodeScannerActivity :
    LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener {
    private lateinit var binding: ActivityFulfilmentPickerBarcodeScannerBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentPickerBarcodeScannerBinding.inflate(layoutInflater)
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
        when (selectedScanMode) {
            FulfilmentPickerScanMode.TOTE -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_tote)
            }
            FulfilmentPickerScanMode.ITEM_INTO_TOTE -> {
                showScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_items)
            }
            null -> return
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

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedScanMode =
                extras.getSerializable(IntentExtrasKeys.FULFILMENT_PICKER_SCAN_MODE.name) as FulfilmentPickerScanMode
            selectedFulfilmentOrder =
                extras.getParcelable(IntentExtrasKeys.FULFILMENT_ORDER.name) as? FulfilmentOrder
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentPickerBarcodeScannerActivity)
            adapter =
                FulfilmentOrderItemCellAdapter(
                    (selectedFulfilmentOrder?.items ?: ArrayList()) as ArrayList<ProductItem?>,
                    this@FulfilmentPickerBarcodeScannerActivity
                )
        }
        binding.textScannedOrder.text = "Order Barcode: ${selectedFulfilmentOrder?.barcode}"
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
                    if (Helper.isCameraPermissionNeeded(this@FulfilmentPickerBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@FulfilmentPickerBarcodeScannerActivity)
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
                callGetTote(barcode)
            }
            FulfilmentPickerScanMode.ITEM_INTO_TOTE -> {
                callScanItemIntoTote(barcode)
            }
            null -> return
        }
    }

    //APIs
    private fun callGetTote(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getTote(
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = FulfilmentPickerScanMode.ITEM_INTO_TOTE
                            scannedTote = response.body()
                            handleSelectedScanMode()
                            binding.textScannedTote.text = "Tote Barcode: ${scannedTote?.barcode}"
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

    private fun callScanItemIntoTote(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanItemIntoTote(
                        scannedTote?.id,
                        selectedFulfilmentOrder?.id,
                        BarcodeRequestBody(barcode)
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val body = response.body()
                            val scrollPosition =
                                (binding.rvScannedBarcodes.adapter as FulfilmentOrderItemCellAdapter).scanItem(
                                    body?.sku
                                )
                            binding.rvScannedBarcodes.smoothScrollToPosition(scrollPosition)
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
}