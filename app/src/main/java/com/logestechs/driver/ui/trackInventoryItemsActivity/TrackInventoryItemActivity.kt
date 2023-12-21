package com.logestechs.driver.ui.trackInventoryItemsActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.responses.InventoryItemResponse
import com.logestechs.driver.databinding.ActivityTrackInventoryItemBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.FulfillmentItemStatus
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.PreviousStatusesCellAdapter
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class TrackInventoryItemActivity : LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityTrackInventoryItemBinding

    private var cameraSource: CameraSource? = null
    private var barcodeDetector: BarcodeDetector? = null

    private var currentBarcodeRead: String? = null
    private var confirmCounter = 0
    private val confirmTarget = 3

    private var scannedItemsHashMap: HashMap<String, String> = HashMap()
    private var toneGen1: ToneGenerator? = null

    private val gradientDrawable = GradientDrawable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackInventoryItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
        if (SharedPreferenceWrapper.getScanWay() == "built-in") {
            // Use built-in scanner, it goes for dispatchKeyEvent
        } else {
            initialiseDetectorsAndSources()
        }
    }

    private fun initUi() {
        binding.textTitle.text = getText(R.string.please_scan_item_barcode)
        binding.titleItemDetails.text = getString(R.string.item_details)
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
                    if (Helper.isCameraPermissionNeeded(this@TrackInventoryItemActivity)) {
                        Helper.showAndRequestCameraDialog(this@TrackInventoryItemActivity)
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
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.characters != null && event.characters.isNotEmpty()) {
            handleDetectedBarcode(event.characters)
        }
        return super.dispatchKeyEvent(event)
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
        callSearchForInventoryItem(barcode)
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

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor", "ResourceType")
    private fun handleDetailsToDisplay(itemDetails: InventoryItemResponse) {
        binding.containerItemDetails.visibility = View.VISIBLE
        binding.itemBarcode.textItem.text = itemDetails.barcode
        binding.itemName.textItem.text = itemDetails.productName
        binding.itemSku.textItem.text = itemDetails.sku
        binding.warehouseName.textItem.text = itemDetails.warehouseName
        binding.customerName.textItem.text = itemDetails.customerName

        if (itemDetails.itemTrackingStatus.isNotEmpty()) {
            val layoutManager = PeekingLinearLayoutManager(
                binding.rvPreviousStatuses
                    .context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            layoutManager.initialPrefetchItemCount = itemDetails.itemTrackingStatus.size

            val childItemAdapter = PreviousStatusesCellAdapter(
                itemDetails.itemTrackingStatus,
                getContext()
            )
            binding.rvPreviousStatuses.layoutManager = layoutManager
            binding.rvPreviousStatuses.adapter = childItemAdapter
        } else {
            binding.previousStatusesCard.visibility = View.GONE
        }

        if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
            binding.itemStatus.text = itemDetails.getStatusText(AppLanguages.ARABIC)
        } else {
            binding.itemStatus.text = itemDetails.getStatusText(AppLanguages.ENGLISH)
        }
        gradientDrawable.cornerRadius = resources.getDimension(R.dimen.corner_radius)
        when (itemDetails.status) {
            FulfillmentItemStatus.UNSORTED -> {
                binding.unreceivedCard.visibility = View.VISIBLE
                binding.rejectedCard.visibility = View.GONE
                binding.sortedCard.visibility = View.GONE
                binding.pickedCard.visibility = View.GONE
                binding.packedCard.visibility = View.GONE
                binding.returnedCard.visibility = View.GONE

                gradientDrawable.setColor(resources.getColor(R.color.yellow))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAsnBarcodeUnreceived.text = itemDetails.shippingPlanBarcode ?: "-------"
            }

            FulfillmentItemStatus.REJECTED -> {
                binding.unreceivedCard.visibility = View.GONE
                binding.rejectedCard.visibility = View.VISIBLE
                binding.sortedCard.visibility = View.GONE
                binding.pickedCard.visibility = View.GONE
                binding.packedCard.visibility = View.GONE
                binding.returnedCard.visibility = View.GONE
                gradientDrawable.setColor(resources.getColor(R.color.red))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAddedMethodRejected.text =
                    itemDetails.shippingPlanBarcode ?: getString(R.string.title_manually)
                binding.itemRejectedDateRejected.visibility = View.GONE
                binding.itemLocationBarcodeRejected.text = itemDetails.locationBarcode ?: "-------"
                binding.itemRejectedReasonRejected.text = itemDetails.rejectReason ?: "-------"
                binding.itemExpiryDateRejected.text = Helper.formatServerDateLocalized(
                    itemDetails.expiryDate,
                    DateFormats.DEFAULT_FORMAT
                )
            }

            FulfillmentItemStatus.SORTED -> {
                binding.unreceivedCard.visibility = View.GONE
                binding.rejectedCard.visibility = View.GONE
                binding.sortedCard.visibility = View.VISIBLE
                binding.pickedCard.visibility = View.GONE
                binding.packedCard.visibility = View.GONE
                binding.returnedCard.visibility = View.GONE
                gradientDrawable.setColor(resources.getColor(R.color.green))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAddedMethodSorted.text =
                    itemDetails.shippingPlanBarcode ?: getString(R.string.title_manually)
                binding.itemReceivedDateSorted.text =
                    SimpleDateFormat(DateFormats.DATE_FILTER_FORMAT.value, Locale.US)
                        .format(
                            SimpleDateFormat(DateFormats.SERVER_FORMAT.value, Locale.US).parse(
                                itemDetails.createdDate!!
                            )!!
                        )
                binding.itemLocationBarcodeSorted.text = itemDetails.locationBarcode ?: "-------"
                binding.itemBinBarcodeSorted.text = itemDetails.binBarcode ?: "-------"
                binding.itemExpiryDateSorted.text = Helper.formatServerDateLocalized(
                    itemDetails.expiryDate,
                    DateFormats.DEFAULT_FORMAT
                )
            }

            FulfillmentItemStatus.PICKED -> {
                binding.unreceivedCard.visibility = View.GONE
                binding.rejectedCard.visibility = View.GONE
                binding.sortedCard.visibility = View.GONE
                binding.pickedCard.visibility = View.VISIBLE
                binding.packedCard.visibility = View.GONE
                binding.returnedCard.visibility = View.GONE
                gradientDrawable.setColor(resources.getColor(R.color.blue))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAddedMethodPicked.text =
                    itemDetails.shippingPlanBarcode ?: getString(R.string.title_manually)
                binding.itemReceivedDatePicked.text = Helper.formatServerDateLocalized(
                    itemDetails.createdDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPreviousLocationPicked.text =
                    itemDetails.previousLocationBarcode ?: "-------"
                binding.itemPreviousBinPicked.text = itemDetails.previousBinBarcode
                binding.itemExpiryDatePicked.text = Helper.formatServerDateLocalized(
                    itemDetails.expiryDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPickedByPicked.text = itemDetails.pickedUser ?: "-------"
                binding.itemFulfillmentOrderBarcodePicked.text =
                    itemDetails.orderBarcode ?: "-------"
                binding.itemToteNumberPicked.text = itemDetails.toteBarcode ?: "-------"
            }

            FulfillmentItemStatus.PACKED -> {
                binding.unreceivedCard.visibility = View.GONE
                binding.rejectedCard.visibility = View.GONE
                binding.sortedCard.visibility = View.GONE
                binding.pickedCard.visibility = View.GONE
                binding.packedCard.visibility = View.VISIBLE
                binding.returnedCard.visibility = View.GONE
                gradientDrawable.setColor(resources.getColor(R.color.purple))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAddedMethodPacked.text =
                    itemDetails.shippingPlanBarcode ?: getString(R.string.title_manually)
                binding.itemReceivedDatePacked.text = Helper.formatServerDateLocalized(
                    itemDetails.createdDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPreviousLocationPacked.text =
                    itemDetails.previousLocationBarcode ?: "-------"
                binding.itemPreviousBinPacked.text = itemDetails.previousBinBarcode ?: "-------"
                binding.itemExpiryDatePacked.text = Helper.formatServerDateLocalized(
                    itemDetails.expiryDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPickedByPacked.text = itemDetails.pickedUser ?: "-------"
                binding.itemFulfillmentOrderBarcodePacked.text =
                    itemDetails.orderBarcode ?: "-------"
                binding.itemToteNumberPacked.text = itemDetails.toteBarcode ?: "-------"
                binding.itemPackedByPacked.text = itemDetails.packedUser ?: "-------"
                binding.itemPackageNumberBarcodePacked.text =
                    itemDetails.packageBarcode ?: "-------"
            }

            FulfillmentItemStatus.RETURNED -> {
                binding.unreceivedCard.visibility = View.GONE
                binding.rejectedCard.visibility = View.GONE
                binding.sortedCard.visibility = View.GONE
                binding.pickedCard.visibility = View.GONE
                binding.packedCard.visibility = View.GONE
                binding.returnedCard.visibility = View.VISIBLE
                gradientDrawable.setColor(resources.getColor(R.color.orange))
                binding.containerItemStatus.background = gradientDrawable
                binding.itemAddedMethodReturned.text =
                    itemDetails.shippingPlanBarcode ?: getString(R.string.title_manually)
                binding.itemReceivedDateReturned.text = Helper.formatServerDateLocalized(
                    itemDetails.createdDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPreviousLocationReturned.text =
                    itemDetails.previousLocationBarcode ?: "-------"
                binding.itemPreviousBinReturned.text = itemDetails.previousBinBarcode ?: "-------"
                binding.itemExpiryDateReturned.text = Helper.formatServerDateLocalized(
                    itemDetails.expiryDate,
                    DateFormats.DEFAULT_FORMAT
                )
                binding.itemPickedByReturned.text = itemDetails.pickedUser ?: "-------"
                binding.itemFulfillmentOrderBarcodeReturned.text =
                    itemDetails.orderBarcode ?: "-------"
                binding.itemPackedByReturned.text = itemDetails.packedUser ?: "-------"
                binding.itemPackageNumberBarcodeReturned.text =
                    itemDetails.packageBarcode ?: "-------"
                binding.itemReturnedDateReturned.visibility = View.GONE
                binding.itemReturnReasonReturned.visibility = View.GONE
                binding.itemCurrentLocationReturned.text = itemDetails.locationBarcode ?: "-------"
            }

            else -> {}
        }
    }

    //APIs
    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    private fun callSearchForInventoryItem(barcode: String?) {
        this.runOnUiThread {
            showWaitDialog()
        }
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.searchForInventoryItem(
                        barcode
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            handleDetailsToDisplay(response.body()!!)
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

    }
}