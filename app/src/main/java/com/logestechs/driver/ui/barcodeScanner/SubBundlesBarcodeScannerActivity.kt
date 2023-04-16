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
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.PickupBundleRequestBody
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ActivitySubBundlesBarcodeScannerBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.ScannedBarcodeCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class SubBundlesBarcodeScannerActivity : LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener {
    private lateinit var binding: ActivitySubBundlesBarcodeScannerBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private val REQUEST_CAMERA_PERMISSION = 201

    private var pkg: Package? = null

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
        getExtras()
        binding = ActivitySubBundlesBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        initialiseDetectorsAndSources()
        initRecycler()
        initListeners()
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            pkg = extras.getParcelable(IntentExtrasKeys.BUNDLE.name)
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonTorch.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initRecycler() {
        val subBundlesList: ArrayList<ScannedItem?> = ArrayList()
        if (pkg != null && pkg?.subBundles != null) {
            for (pkg in pkg?.subBundles!!) {
                subBundlesList.add(pkg?.getPickupScannedItem())
            }
        }

        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@SubBundlesBarcodeScannerActivity)
            adapter = ScannedBarcodeCellAdapter(
                subBundlesList,
                null,
                true
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
                if (!(binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).makePackageSelected(
                        scannedBarcode
                    )
                ) {
                    Helper.showErrorMessage(this, getString(R.string.error_package_not_found))
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        scannedItemsHashMap.remove(scannedBarcode)
                    }, 2, TimeUnit.SECONDS)
                }
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
                    if (Helper.isCameraPermissionNeeded(this@SubBundlesBarcodeScannerActivity)) {
                        Helper.showAndRequestCameraDialog(this@SubBundlesBarcodeScannerActivity)
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
            this.runOnUiThread {
                if (!(binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).makePackageSelected(
                        barcode
                    )
                ) {
                    Helper.showErrorMessage(this, getString(R.string.error_package_not_found))
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        scannedItemsHashMap.remove(barcode)
                    }, 2, TimeUnit.SECONDS)
                }
            }
            toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            vibrate()
        }
    }

    private fun handleScannedItemsCount() {
        binding.textScannedItemsCount.text =
            getString(R.string.count) + binding.rvScannedBarcodes.adapter?.itemCount
    }

    //APIs
    private fun callPickupBundle(scannedPackagesIds: List<Long?>) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.pickupBundle(
                        pkg?.id,
                        PickupBundleRequestBody(scannedPackagesIds)
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val returnIntent = Intent()
                            returnIntent.putExtra(IntentExtrasKeys.BUNDLE.name, pkg)
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
                val barcodes =
                    (binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).getSelectedPackagesIds()
                if (barcodes.isNotEmpty()) {
                    callPickupBundle(barcodes)
                } else {
                    Helper.showErrorMessage(
                        this,
                        getString(R.string.title_please_scan_bundle_items)
                    )
                }
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
            if (!(binding.rvScannedBarcodes.adapter as ScannedBarcodeCellAdapter).makePackageSelected(
                    barcode
                )
            ) {
                Helper.showErrorMessage(this, getString(R.string.error_package_not_found))
                Executors.newSingleThreadScheduledExecutor().schedule({
                    scannedItemsHashMap.remove(barcode)
                }, 2, TimeUnit.SECONDS)
            }
        }
    }
}