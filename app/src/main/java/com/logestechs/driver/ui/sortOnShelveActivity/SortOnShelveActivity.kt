@file:Suppress("DEPRECATION")

package com.logestechs.driver.ui.sortOnShelveActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.databinding.ActivitySortOnShelveBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

enum class ShelfScanMode {
    SHELF,
    PACKAGE_INTO_SHELF
}

@Suppress("DEPRECATION")
class SortOnShelveActivity : LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener {
    private lateinit var binding: ActivitySortOnShelveBinding

    private var cameraSource: CameraSource? = null
    private var barcodeDetector: BarcodeDetector? = null

    private var currentBarcodeRead: String? = null
    private var confirmCounter = 0
    private val confirmTarget = 3

    private var scannedItemsHashMap: HashMap<String, String> = HashMap()
    private var toneGen1: ToneGenerator? = null

    private var flashMode: Boolean = false

    private var selectedScanMode: ShelfScanMode? = ShelfScanMode.SHELF

    private var scannedBarcode = ""

    private var shelfId: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySortOnShelveBinding.inflate(layoutInflater)
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        setContentView(binding.root)

        initListeners()
        initUi()
        initialiseDetectorsAndSources()
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonTorch.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
    }

    private fun initUi() {
        handleSelectedScanMode()
    }

    private fun handleSelectedScanMode() {
        scannedItemsHashMap.clear()
        when (selectedScanMode) {
            ShelfScanMode.SHELF -> {
                binding.textTitle.text = getText(R.string.please_scan_shelf_barcode)
            }

            ShelfScanMode.PACKAGE_INTO_SHELF -> {
                binding.containerTitle.visibility = View.GONE
                binding.containerInsertBarcode.visibility = View.VISIBLE
                binding.containerScannedItem.visibility = View.VISIBLE
            }

            null -> return
        }
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
                    if (Helper.isCameraPermissionNeeded(this@SortOnShelveActivity)) {
                        Helper.showAndRequestCameraDialog(this@SortOnShelveActivity)
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
        if (!scannedItemsHashMap.containsKey(barcode)) {
            scannedItemsHashMap[barcode] = barcode
            executeBarcodeAction(barcode)
            toneGen1?.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
            vibrate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun executeBarcodeAction(barcode: String?) {
        scannedItemsHashMap.clear()
        when (selectedScanMode) {
            ShelfScanMode.SHELF -> {
                callScanShelf(barcode)
            }

            ShelfScanMode.PACKAGE_INTO_SHELF -> {
                callScanPackageOnShelf(barcode)
            }

            null -> return
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

    @RequiresApi(Build.VERSION_CODES.M)
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
            scannedBarcode += character
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

    //APIs
    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    private fun callScanShelf(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanShelfByBarcode(
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = ShelfScanMode.PACKAGE_INTO_SHELF
                            handleSelectedScanMode()
                            shelfId = response.body()!!.id
//                            binding.titleShelf.text = response.body()!!.destinationCity + " " + getString(R.string.title_shelf)
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

    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    private fun callScanPackageOnShelf(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.scanPackagesOnShelf(
                        shelfId,
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            selectedScanMode = ShelfScanMode.PACKAGE_INTO_SHELF
                            handleSelectedScanMode()
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBarcodeInserted(barcode: String) {
        callScanShelf(barcode)
    }
}