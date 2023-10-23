package com.logestechs.driver.ui.barcodeScanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityBarcodeScannerBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.ScannedBarcodeCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import com.logestechs.driver.utils.interfaces.ScannedBarcodeCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException


class BarcodeScannerActivity : LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener, ScannedBarcodeCardListener {
    private lateinit var binding: ActivityBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private val REQUEST_CAMERA_PERMISSION = 201

    private var scanType: BarcodeScanType = BarcodeScanType.PACKAGE_PICKUP
    var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null

    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var scannedBarcode = ""

    private var flashmode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        handleOutOfDelivery()
        initialiseDetectorsAndSources()
        initRecycler()
        handleScannedItemsCount()
        initListeners()
        getExtras()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_SCAN_BUNDLE && resultCode == RESULT_OK) {

            val extras = data?.extras
            if (extras != null) {
                val pkg: Package? = extras.getParcelable(IntentExtrasKeys.BUNDLE.name)
                (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).insertItem(
                    pkg?.getPickupScannedItem()
                )
                handleScannedItemsCount()
                Helper.showSuccessMessage(
                    super.getContext(),
                    getString(R.string.success_operation_completed)
                )
            }
        }
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            customer = extras.getParcelable(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_FOR_PICKUP.name)
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonTorch.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@BarcodeScannerActivity)
            adapter = ScannedBarcodeCellAdapter(ArrayList(), this@BarcodeScannerActivity)
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
                callPickupPackage(scannedBarcode)
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
                    if (Helper.isCameraPermissionNeeded(this@BarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@BarcodeScannerActivity)
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

            override fun receiveDetections(detections: Detections<Barcode>) {
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
            callPickupPackage(barcode)
            toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            vibrate()
        }
    }

    private fun handleScannedItemsCount() {
        binding.textScannedItemsCount.text =
            getString(R.string.count) + binding.rvScannedBarcodes.adapter?.itemCount
    }

    private fun handleOutOfDelivery() {
        if (Helper.getCompanyCurrency() != AppCurrency.NIS.value &&
            Helper.getCompanyCurrency() != AppCurrency.JOD.value
        ) {
            binding.tvOutOfDelivery.visibility = View.VISIBLE
            binding.switchOutOfDelivery.visibility = View.VISIBLE
        }
    }

    //APIs
    private fun callPickupPackage(barcode: String) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.pickupPackage(
                        barcode,
                        driverCompanyConfigurations?.isBundlePodEnabled,
                        binding.switchOutOfDelivery.isChecked
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (response.body()?.status?.name == AdminPackageStatus.RETURNED_BY_RECIPIENT.name) {
                                Helper.run {
                                    showErrorMessage(
                                        super.getContext(),
                                        getString(R.string.error_package_is_returned)
                                    )
                                }
                            } else if (response.body()?.status?.name == AdminPackageStatus.FAILED.name) {
                                Helper.run {
                                    showErrorMessage(
                                        super.getContext(),
                                        getString(R.string.error_package_is_failed)
                                    )
                                }
                            }

                            if (response.body()?.isBundle == true && driverCompanyConfigurations?.isBundlePodEnabled == true) {
                                Helper.run {
                                    val mIntent = Intent(
                                        this@BarcodeScannerActivity,
                                        SubBundlesBarcodeScannerActivity::class.java
                                    )
                                    mIntent.putExtra(IntentExtrasKeys.BUNDLE.name, response.body())
                                    startActivityForResult(
                                        mIntent,
                                        AppConstants.REQUEST_SCAN_BUNDLE
                                    )
                                }

                            } else if (driverCompanyConfigurations?.isPrintAwbCopiesAsPackageQuantity == true && (response.body()?.quantity
                                    ?: 0) > 1
                            ) {
                                (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).insertSubPackage(
                                    response.body()?.getPickupScannedItem(),
                                    !barcode.contains(":")
                                )
                            } else {
                                (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).insertItem(
                                    response.body()?.getPickupScannedItem()
                                )
                            }

                            handleScannedItemsCount()
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
                    hideWaitDialog()
                    scannedItemsHashMap.remove(barcode)
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
            scannedItemsHashMap.remove(barcode)
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callCancelPickup(position: Int, pkg: Package?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.cancelPickup(
                        pkg?.id
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
                            (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).deleteItem(
                                position
                            )
                            scannedItemsHashMap.remove(pkg?.barcode)
                            handleScannedItemsCount()
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

    private fun callCancelShippingPlanPickup(position: Int, pkg: Package?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.cancelShippingPlanPickup(
                        pkg?.barcode
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
                            (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).deleteItem(
                                position
                            )
                            scannedItemsHashMap.remove(pkg?.barcode)
                            handleScannedItemsCount()
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
            R.id.button_done -> {
                onBackPressed()
            }

            R.id.button_insert_barcode -> {
                InsertBarcodeDialog(this, this).showDialog()
            }

            R.id.button_torch -> {
                openFlashLight()
            }
        }

    }

    private fun openFlashLight() {
        val camera = Helper.getCameraFromCameraSource(cameraSource)
        try {
            val param = camera?.parameters
            if (!flashmode) {
                binding.buttonTorch.setImageDrawable(
                    ContextCompat.getDrawable(
                        super.getContext(),
                        R.drawable.ic_torch_on
                    )
                )
                param?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            } else {
                binding.buttonTorch.setImageDrawable(
                    ContextCompat.getDrawable(
                        super.getContext(),
                        R.drawable.ic_torch_off
                    )
                )
                param?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
            camera?.parameters = param
            flashmode = !flashmode
        } catch (e: java.lang.Exception) {
            Helper.showErrorMessage(super.getContext(), e.localizedMessage)
        }
    }
    override fun onBarcodeInserted(barcode: String) {
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            callPickupPackage(barcode)
        }
    }

    override fun onCancelPickup(position: Int, pkg: Package) {
        if (pkg.isShippingPlan == true) {
            callCancelShippingPlanPickup(position, pkg)
        } else {
            callCancelPickup(position, pkg)
        }
    }
}