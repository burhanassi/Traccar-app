package com.logestechs.driver.ui.barcodeScanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.BarcodeRequestBody
import com.logestechs.driver.api.responses.SortItemIntoToteResponse
import com.logestechs.driver.data.model.Bin
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ActivityFulfilmentPackerBarcodeScannerBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.adapters.FulfilmentOrderItemCellAdapter
import com.logestechs.driver.utils.adapters.FulfilmentOrderItemToPackCellAdapter
import com.logestechs.driver.utils.adapters.ShippingPlanCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

enum class FulfilmentPackerScanMode {
    TOTE,
    ITEM_INTO_TOTE
}

class FulfilmentPackerBarcodeScannerActivity :
    LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener {
    private lateinit var binding: ActivityFulfilmentPackerBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var scannedBarcode = ""

    private var scannedTote: Bin? = null
    private var selectedFulfilmentOrder: FulfilmentOrder? = null

    private var selectedScanMode: FulfilmentPackerScanMode? = FulfilmentPackerScanMode.TOTE

    private var fulfilmentOrder: FulfilmentOrder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentPackerBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        fulfilmentOrder = intent.getParcelableExtra(IntentExtrasKeys.FULFILMENT_ORDER.name)
        initialiseDetectorsAndSources()
        initRecycler()
        initListeners()
        initUi()
    }

    private fun handleSelectedScanMode() {
        scannedItemsHashMap.clear()
        binding.containerDetails.visibility = View.GONE
        when (selectedScanMode) {
            FulfilmentPackerScanMode.TOTE -> {
                hideScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_tote)
            }

            FulfilmentPackerScanMode.ITEM_INTO_TOTE -> {
                showScannedItemsContainer()
                binding.textTitle.text = getString(R.string.please_scan_item_barcode)
                binding.textItemsNumber.text = getString(R.string.number_of_items) +
                        "${(binding.rvScannedBarcodes.adapter as FulfilmentOrderItemToPackCellAdapter).getItemCount()} " + getString(
                    R.string.number_of_somthing
                ) + fulfilmentOrder?.totalQuantity
            }

            null -> return
            else -> {}
        }
    }

    private fun showScannedItemsContainer() {
        binding.containerDetails.visibility = View.VISIBLE
    }

    private fun hideScannedItemsContainer() {
        binding.containerDetails.visibility = View.GONE
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedFulfilmentOrder =
                extras.getParcelable(IntentExtrasKeys.FULFILMENT_ORDER.name) as? FulfilmentOrder
        }
    }

    private fun initUi() {
        handleSelectedScanMode()
    }

    private fun initListeners() {
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@FulfilmentPackerBarcodeScannerActivity)
            adapter =
                FulfilmentOrderItemToPackCellAdapter(
                    ArrayList(),
                    this@FulfilmentPackerBarcodeScannerActivity
                )
        }
        binding.textScannedOrder.text =
            getString(R.string.order_barcode) + "${selectedFulfilmentOrder?.barcode}"
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
                    if (Helper.isCameraPermissionNeeded(this@FulfilmentPackerBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@FulfilmentPackerBarcodeScannerActivity)
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
            FulfilmentPackerScanMode.TOTE -> {
                callGetToteToPackItems(barcode)
            }

            FulfilmentPackerScanMode.ITEM_INTO_TOTE -> {
                callPackFulfilmentOrderByItem(barcode)
            }

            null -> return
            else -> {}
        }
    }

    //APIs

    private fun callPackFulfilmentOrderByItem(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var response = ApiAdapter.apiClient.packFulfilmentOrderByItem(
                        fulfilmentOrder?.id!!,
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val body = response.body()

                            (binding.rvScannedBarcodes.adapter as FulfilmentOrderItemToPackCellAdapter)
                                .insertItem(body)
                            binding.rvScannedBarcodes.smoothScrollToPosition(0)
                            binding.textItemsNumber.text =
                                "${(binding.rvScannedBarcodes.adapter as FulfilmentOrderItemToPackCellAdapter).getItemCount()} of ${fulfilmentOrder?.totalQuantity}"
                            if ((binding.rvScannedBarcodes.adapter as FulfilmentOrderItemToPackCellAdapter)
                                    .getItemCount() == fulfilmentOrder?.totalQuantity
                            ) {
                                callPackFulfilmentOrder()
                            }
                            Helper.showSuccessMessage(
                                super.getContext(), getString(R.string.success_operation_completed)
                            )
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

    private fun callPackFulfilmentOrder() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.packFulfilmentOrder(fulfilmentOrder?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(), getString(R.string.success_operation_completed)
                            )
//                            currentPageIndex = 1
//                            (binding.rvFulfilmentOrders.adapter as PickedFulfilmentOrderCellAdapter).clearList()
//                            callGetFulfilmentOrders(FulfilmentOrderStatus.PICKED.name)
                            onBackPressed()
                        }

                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), getString(R.string.error_general)
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

    private fun callGetToteToPackItems(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanToteToPack(fulfilmentOrder?.id, barcode)

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = FulfilmentPackerScanMode.ITEM_INTO_TOTE
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