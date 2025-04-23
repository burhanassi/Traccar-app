@file:Suppress("DEPRECATION")

package com.logestechs.driver.ui.verifyPackageDelivery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.databinding.ActivityVerifyPackageDeliveryBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@Suppress("DEPRECATION")
class VerifyPackageDeliveryActivity : LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityVerifyPackageDeliveryBinding

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    private var packageBarcode: String? = null
    private var invoiceBarcode: String? = null

    private val scannedSubpackagesBarcodes = hashSetOf<String>()
    private var quantity: Int = 0
    private var counter: Int = 0

    private var scannedItemsHashMap: HashMap<String, String> = HashMap()
    var customer: Customer? = null
    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var currentBarcodeRead: String? = null
    private val confirmTarget = 3
    private var confirmCounter = 0

    private var toneGen1: ToneGenerator? = null

    private var flashMode: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quantity = SharedPreferenceWrapper.getSubpackagesQuantity()
        getExtras()
        binding = ActivityVerifyPackageDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        initUi()
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            // Use built-in scanner, it goes for dispatchKeyEvent
        } else {
            initialiseDetectorsAndSources()
        }
        initListeners()
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            packageBarcode = extras.getString("barcode")
            invoiceBarcode = extras.getString("invoice")
        }
    }

    private fun initUi() {
        binding.textTitle.text = getText(R.string.please_scan_package_barcode)
    }

    private fun initListeners() {
        binding.buttonTorch.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.characters != null && event.characters.isNotEmpty()) {
            handleDetectedBarcode(event.characters)
        }
        return super.dispatchKeyEvent(event)
    }

    private fun initialiseDetectorsAndSources() {

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector!!)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.surfaceView.holder?.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (Helper.isCameraPermissionNeeded(this@VerifyPackageDeliveryActivity)) {
                        Helper.showAndRequestCameraDialog(this@VerifyPackageDeliveryActivity)
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

            @RequiresApi(Build.VERSION_CODES.M)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleDetectedBarcode(barcode: String) {
        if (scannedItemsHashMap.containsKey(barcode)) return

        scannedItemsHashMap[barcode] = barcode
        toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        vibrate()

        // Coroutine with 2-second delay (non-blocking)
        lifecycleScope.launch { // Uses Activity's lifecycleScope
            delay(2000) // 2 seconds delay
            executeBarcodeAction(barcode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun executeBarcodeAction(barcode: String?) {
        if (quantity != counter) {
            if (quantity <= 1 || driverCompanyConfigurations?.isScanAllPackageAwbCopiesByDriver == false) {
                if (barcode!!.contains(":")) {
                    val parentBarcode = barcode.split(":")[0]
                    if (parentBarcode == packageBarcode) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("verificationStatus", true)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        runOnUiThread {
                            Helper.showErrorMessage(
                                super.getContext(),
                                getString(R.string.error_wrong_package)
                            )
                        }
                    }
                } else if (barcode == packageBarcode || barcode == invoiceBarcode) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("verificationStatus", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    runOnUiThread {
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_wrong_package)
                        )
                    }
                }
            } else {
                if (barcode!!.contains(":")) {
                    val parentBarcode = barcode.split(":")[0]
                    if (parentBarcode == packageBarcode) {
                        if (scannedSubpackagesBarcodes.contains(barcode)) {
                            runOnUiThread {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_barcode_already_scanned)
                                )
                            }
                        } else {
                            scannedSubpackagesBarcodes.add(barcode)
                            counter++
                            runOnUiThread {
                                Helper.showSuccessMessage(
                                    super.getContext(),
                                    "${getString(R.string.confirmed)} $counter ${getString(R.string.of)} $quantity"
                                )
                            }
                        }
                    } else {
                        runOnUiThread {
                            Helper.showErrorMessage(
                                super.getContext(),
                                getString(R.string.error_wrong_package)
                            )
                        }
                    }
                } else {
                    if (barcode == packageBarcode) {
                        runOnUiThread {
                            Helper.showErrorMessage(
                                this@VerifyPackageDeliveryActivity,
                                getString(R.string.error_scan_all_items)
                            )
                        }
                    } else {
                        runOnUiThread {
                            Helper.showErrorMessage(
                                super.getContext(),
                                getString(R.string.error_wrong_package)
                            )
                        }
                    }
                }
            }
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra("verificationStatus", true)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        if (quantity == counter) {
            val resultIntent = Intent()
            resultIntent.putExtra("verificationStatus", true)
            setResult(RESULT_OK, resultIntent)
            finish()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_torch -> {
                openFlashLight()
            }
        }
    }

    private fun openFlashLight() {
        val camera = Helper.getCameraFromCameraSource(cameraSource)
        try {
            val param = camera?.parameters
            if (!flashMode) {
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
            flashMode = !flashMode
        } catch (e: java.lang.Exception) {
            Helper.showErrorMessage(super.getContext(), e.localizedMessage)
        }
    }
}