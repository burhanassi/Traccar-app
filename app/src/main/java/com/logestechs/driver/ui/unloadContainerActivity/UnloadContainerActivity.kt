package com.logestechs.driver.ui.unloadContainerActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.responses.GetVerfiyDriverResponse
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivitySortOnShelveBinding
import com.logestechs.driver.databinding.ActivityUnloadContainerBinding
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.ui.sortOnShelveActivity.ShelfScanMode
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.DriverPackagesCellAdapter
import com.logestechs.driver.utils.adapters.DriverRoutePackagesCellAdapter
import com.logestechs.driver.utils.adapters.InCarGroupedMassCodReportAdapter
import com.logestechs.driver.utils.adapters.MassCodReportCellAdapter
import com.logestechs.driver.utils.adapters.ScannedPackagesOnShelfCellAdapter
import com.logestechs.driver.utils.dialogs.InsertBarcodeDialog
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener
import com.logestechs.driver.utils.interfaces.SearchPackagesDialogListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

enum class UnloadScanMode {
    DRIVER,
    PACKAGE
}

class UnloadContainerActivity : LogesTechsActivity(), View.OnClickListener,
    InsertBarcodeDialogListener,
    SearchPackagesDialogListener {
    private lateinit var binding: ActivityUnloadContainerBinding

    private var cameraSource: CameraSource? = null
    private var barcodeDetector: BarcodeDetector? = null

    private var currentBarcodeRead: String? = null
    private var confirmCounter = 0
    private val confirmTarget = 3

    private var scannedItemsHashMap: HashMap<String, String> = HashMap()
    private var toneGen1: ToneGenerator? = null

    private var flashMode: Boolean = false

    private var selectedScanMode: UnloadScanMode? = UnloadScanMode.DRIVER

    private var scannedBarcode = ""

    private var driverId: Long? = null

    private var currentPageIndex = 1
    private var totalRecordsNo = 0
    private var isLoading = false
    private var isLastPage = false

    private var searchWord: String? = null

    private var scannedPackages: Int = 0

    private var driverBarcode: String = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnloadContainerBinding.inflate(layoutInflater)
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        setContentView(binding.root)

        initListeners()
        initRecycler()
        initUi()
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            // Use built-in scanner, it goes for dispatchKeyEvent
        } else {
            initialiseDetectorsAndSources()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQUEST_SCAN_BUNDLE && resultCode == RESULT_OK) {

            val extras = data?.extras
            if (extras != null) {
                val pkg: Package? = extras.getParcelable(IntentExtrasKeys.BUNDLE.name)
                (binding.rvScannedBarcodes.adapter as ScannedPackagesOnShelfCellAdapter).insertItem(
                    pkg?.getPickupScannedItem()
                )
                Helper.showSuccessMessage(
                    super.getContext(),
                    getString(R.string.success_operation_completed)
                )
            }
        }
        if (requestCode == AppConstants.REQUEST_SCAN_BARCODE) {
            if (resultCode == RESULT_OK && data != null) {
                searchWord = data.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name)
                SearchPackagesDialog(super.getContext(), this, searchWord).showDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            searchWord = null
            clearList()
            callVerifyDriver(driverBarcode!!)
        }
        binding.buttonDone.setOnClickListener(this)
        binding.buttonTorch.setOnClickListener(this)
        binding.buttonInsertBarcode.setOnClickListener(this)
        binding.buttonSearch.setOnClickListener(this)
        binding.buttonScanPackages.setOnClickListener(this)
    }

    private fun initUi() {
        handleSelectedScanMode()
    }

    private fun initRecycler() {
        binding.rvScannedBarcodes.apply {
            layoutManager = LinearLayoutManager(this@UnloadContainerActivity)
            adapter = ScannedPackagesOnShelfCellAdapter(ArrayList(), null)
        }

        binding.rvDriverPackages.apply {
            layoutManager = LinearLayoutManager(this@UnloadContainerActivity)
            adapter = DriverPackagesCellAdapter(ArrayList())
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun handleSelectedScanMode() {
        when (selectedScanMode) {
            UnloadScanMode.DRIVER -> {
                binding.textTitle.text = getText(R.string.please_verify_driver)
            }

            UnloadScanMode.PACKAGE -> {
                binding.containerTitle.visibility = View.GONE
                binding.containerDriverDetails.visibility = View.GONE
                binding.containerBarcodeScanner.visibility = View.VISIBLE
                binding.containerInsertBarcode.visibility = View.VISIBLE
                binding.containerScannedItem.visibility = View.VISIBLE
            }

            null -> return
        }
    }

    private fun handleDriverVerified(data: GetVerfiyDriverResponse, search: String? = null) {
        binding.containerBarcodeScanner.visibility = View.GONE
        binding.containerDriverDetails.visibility = View.VISIBLE

        binding.textDriverName.text = data.firstName + " " + data.lastName
        binding.textDriverHub.text = data.city
        binding.itemDriverCar.textItem.text = data.vehicle?.brand
        binding.tvPackagesNumber.text =
            getString(R.string.title_packages) + ": " + data.vehicle?.noOfPkgs.toString()
        if (search != null) {
            (binding.rvDriverPackages.adapter as DriverPackagesCellAdapter).clearList()
        }
        (binding.rvDriverPackages.adapter as DriverPackagesCellAdapter).update(
            data.driverPkgs!!
        )
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
                    if (Helper.isCameraPermissionNeeded(this@UnloadContainerActivity)) {
                        Helper.showAndRequestCameraDialog(this@UnloadContainerActivity)
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
        when (selectedScanMode) {
            UnloadScanMode.DRIVER -> {
                driverBarcode = barcode!!
                callVerifyDriver(barcode)
            }

            UnloadScanMode.PACKAGE -> {
                callUnloadPackageFromContainer(barcode)
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
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
            binding.refreshLayoutCustomers.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }

    private fun clearList() {
        currentPageIndex = 1
        (binding.rvDriverPackages.adapter as DriverPackagesCellAdapter).clearList()

    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvDriverPackages.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvDriverPackages.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvDriverPackages.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        callVerifyDriver(driverBarcode!!)
                    }
                }
            }
        }

    //APIs
    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    private fun callVerifyDriver(barcode: String?, search: String? = null) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.verifyDriver(
                        barcode,
                        page = currentPageIndex,
                        search = search
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful!! && response?.body()!! != null) {
                        withContext(Dispatchers.Main) {
                            driverId = response.body()!!.id
                            totalRecordsNo = response.body()!!.vehicle?.noOfPkgs!!

                            val totalRound: Int =
                                (totalRecordsNo) / (AppConstants.DEFAULT_PAGE_SIZE * currentPageIndex)
                            if (totalRound == 0) {
                                currentPageIndex = 1
                                isLastPage = true
                            } else {
                                currentPageIndex++
                                isLastPage = false
                            }
                            withContext(Dispatchers.Main) {
                                handleDriverVerified(response.body()!!, search)
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
                    isLoading = false
                } catch (e: Exception) {
                    isLoading = false
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

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    private fun callUnloadPackageFromContainer(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.unloadPackageFromContainerToHub(
                        barcode,
                        driverId
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        scannedPackages++
                        withContext(Dispatchers.Main) {
                            selectedScanMode = UnloadScanMode.PACKAGE
                            handleSelectedScanMode()
                            binding.titleScannedPackages.text =
                                getString(R.string.title_scanned_packages) + " ($scannedPackages)"
                            (binding.rvScannedBarcodes.adapter as ScannedPackagesOnShelfCellAdapter).insertItem(
                                response.body()?.getPickupScannedItem()
                            )
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
                        scannedItemsHashMap.remove(barcode)
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

            R.id.button_torch -> {
                openFlashLight()
            }

            R.id.button_search -> {
                SearchPackagesDialog(super.getContext(), this, searchWord).showDialog()
            }

            R.id.button_scan_packages -> {
                selectedScanMode = UnloadScanMode.PACKAGE
                handleSelectedScanMode()
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
            Helper.showErrorMessage(getContext(), e.localizedMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBarcodeInserted(barcode: String) {
        callUnloadPackageFromContainer(barcode)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPackageSearch(keyword: String?) {
        searchWord = keyword
        currentPageIndex = 1
        callVerifyDriver(driverBarcode, searchWord!!)
    }

    override fun onStartBarcodeScan() {
        val scanBarcode = Intent(super.getContext(), SingleScanBarcodeScanner::class.java)
        this.startActivityForResult(
            scanBarcode,
            AppConstants.REQUEST_SCAN_BARCODE
        )
    }
}