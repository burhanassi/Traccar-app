package com.logestechs.driver.ui.packageDeliveryScreens.packageDelivery

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.location.Location
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.AddNoteRequestBody
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.requests.DeliverPackageRequestBody
import com.logestechs.driver.api.requests.PayMultiWayRequestBody
import com.logestechs.driver.data.model.CodCollectionMethod
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.PackageItemsToDeliver
import com.logestechs.driver.data.model.PaymentTypeModel
import com.logestechs.driver.data.model.Status
import com.logestechs.driver.databinding.ActivityPackageDeliveryBinding
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.ui.verifyPackageDelivery.VerifyPackageDeliveryActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.ConfirmationDialogAction
import com.logestechs.driver.utils.CountriesCode
import com.logestechs.driver.utils.DeliveryType
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.Helper.Companion.isAppInstalled
import com.logestechs.driver.utils.IntegrationSource
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.PaymentGatewayType
import com.logestechs.driver.utils.PaymentType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.VerificationStatus
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.dialogs.DeliveryCodeVerificationDialog
import com.logestechs.driver.utils.dialogs.PaymentTypeValueDialog
import com.logestechs.driver.utils.interfaces.ConfirmationDialogActionListener
import com.logestechs.driver.utils.interfaces.PaymentTypeValueDialogListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
import com.logestechs.driver.utils.interfaces.VerificationCodeDialogListener
import io.nearpay.sdk.Environments
import io.nearpay.sdk.NearPay
import io.nearpay.sdk.utils.PaymentText
import io.nearpay.sdk.utils.enums.AuthenticationData
import io.nearpay.sdk.utils.enums.NetworkConfiguration
import io.nearpay.sdk.utils.enums.PurchaseFailure
import io.nearpay.sdk.utils.enums.TransactionData
import io.nearpay.sdk.utils.enums.UIPosition
import io.nearpay.sdk.utils.listeners.PurchaseListener
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.util.TypedValue

class PackageDeliveryActivity : LogesTechsActivity(), View.OnClickListener, ThumbnailsListListener,
    ConfirmationDialogActionListener,
    VerificationCodeDialogListener,
    PaymentTypeValueDialogListener {
    private lateinit var binding: ActivityPackageDeliveryBinding

    private var path: String? = null
    private var file: File? = null
    private var bitmap: Bitmap? = null
    private var gestureTouch = false
    private var pkg: Package? = null

    private var paymentTypeButtonsList: ArrayList<StatusSelector> = ArrayList()
    private var selectedPaymentType: StatusSelector? = null

    private var selectedDeliveryType: DeliveryType? = null


    private var selectedPodImageUri: Uri? = null
    private var mCurrentPhotoPath: String? = null

    private var selectedVideoUri: Uri? = null
    private var mCurrentVideoPath: String? = null

    private var loadedImagesList: ArrayList<LoadedImage> = ArrayList()

    private var isCameraAction = false

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    var items: List<PackageItemsToDeliver?>? = null

    private var isClickPayVerified = false

    private var paymentTypeId: Long? = null
    private var packageCodToPay: Double = 0.0
    private var packageValueToPay: Double = 0.0

    private var notes: String? = null

    private var sum: Double? = 0.0
    private var paymentDataList = mutableListOf<PayMultiWayRequestBody>()

    private var scannedSubpackagesBarcodes: List<String> = emptyList()
    private var videoUrl: String = ""

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        initializeUi()
        initListeners()
        initData()
        initPaymentMethodsControls()
        fillButtonsList()
    }

    private fun initializeUi() {
        path =
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/signature.png"
        file = File(path ?: "")
        file?.delete()

        binding.gestureViewSignature.isHapticFeedbackEnabled = false
        binding.gestureViewSignature.cancelLongPress()
        binding.gestureViewSignature.cancelClearAnimation()

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(super.getContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@PackageDeliveryActivity)
        }

        if (companyConfigurations?.isPartialDeliveryEnabled == true) {
            binding.containerPartialDeliveryControls.visibility = View.VISIBLE
        }

        if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true) {
            binding.containerSignature.visibility = View.GONE
        }

        if (companyConfigurations?.isShowPaymentTypesWhenDriverDeliver == false) {
            binding.containerPaymentType.visibility = View.GONE
        }

        if (pkg?.shipmentType == PackageType.REGULAR.name) {
            binding.containerPaymentType.visibility = View.GONE
        }

        if (Helper.getCountryCode() == CountriesCode.SAR.value && companyConfigurations?.isAddingPaymentTypesEnabled == false) {
            binding.containerPaymentGateways.visibility = View.VISIBLE
            if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes != true) {
                binding.textFieldClickPay.visibility = View.GONE
                binding.textFieldInterPay.visibility = View.GONE
                binding.textFieldNearPay.visibility =View.GONE
            }
        }
        binding.textPaymentAmount.visibility = View.GONE

        if (pkg?.shipmentType == PackageType.REGULAR.name) {
            binding.containerPaymentType.visibility = View.GONE
        }

        handleWarningText()
    }

    private fun handleWarningText() {
        var hasWarnings = false
        val warningText = StringBuilder()
        if (pkg?.shipmentType == PackageType.SWAP.name) {
            if (warningText.isNotEmpty()) {
                warningText.append("\n")
                warningText.append("\n")
            }
            warningText.append("*")
            warningText.append(getString(R.string.warning_pickup_returned_packages))
            hasWarnings = true
        }

        binding.textWarningMessage.text = warningText.toString()

        if (hasWarnings) {
            binding.containerWarningMessage.visibility = View.VISIBLE
        }
    }

    private fun isSignatureEntered(): Boolean {
        return binding.gestureViewSignature.gesture != null && binding.gestureViewSignature.gesture.length > 0
    }

    private fun initData() {
        items = pkg?.packageItemsToDeliverList
        binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
        binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()
        binding.itemPackageBarcode.textItem.text = pkg?.barcode
        binding.textCod.text = pkg?.cod?.format()

        if (pkg?.notes?.trim().isNullOrEmpty()) {
            binding.itemNotes.root.visibility = View.GONE
        } else {
            binding.itemNotes.root.visibility = View.VISIBLE
            binding.itemNotes.textItem.text = pkg?.notes
            notes = pkg?.notes
        }

        if (pkg?.supplierInvoice?.trim().isNullOrEmpty()) {
            binding.itemSupplierInvoiceNumber.root.visibility = View.GONE
        } else {
            binding.itemSupplierInvoiceNumber.root.visibility = View.VISIBLE
            binding.itemSupplierInvoiceNumber.textItem.text = pkg?.supplierInvoice
        }

        if (companyConfigurations?.isPartialDeliveryEnabled == true) {
            selectedDeliveryType = DeliveryType.FULL
        }

        binding.textPaymentAmount.text = sum?.format() + "/" + pkg?.cod?.format()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initListeners() {
        binding.gestureViewSignature.addOnGestureListener(object :
            GestureOverlayView.OnGestureListener {
            override fun onGesture(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
            }

            override fun onGestureCancelled(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
            }

            override fun onGestureEnded(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
                binding.containerScrollView.isEnableScrolling = true
            }

            override fun onGestureStarted(
                gestureOverlayView: GestureOverlayView,
                motionEvent: MotionEvent
            ) {
                binding.containerScrollView.isEnableScrolling = false
                gestureTouch = motionEvent.action != MotionEvent.ACTION_MOVE
            }
        })

        binding.radioGroupPartialDelivery.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_button_full_delivery) {
                binding.containerPartialDeliveryNote.visibility = View.GONE
                selectedDeliveryType = DeliveryType.FULL
            } else if (checkedId == R.id.radio_button_partial_delivery) {
                binding.containerPartialDeliveryNote.visibility = View.VISIBLE
                selectedDeliveryType = DeliveryType.PARTIAL
                if ((companyConfigurations?.isSupportDeliveringPackageItemsPartially!! && items != null) ||
                    (pkg?.integrationSource == IntegrationSource.FULFILLMENT)
                ) {
                    binding.tvNoteTitle.text = getString(R.string.title_partial_delivery_items_flow)
                    val checkBoxContainer = findViewById<LinearLayout>(R.id.check_box_container)
                    val itemPriceLabel = getString(R.string.item_price)
                    val isArabic = resources.configuration.locale.language == "ar"
                    var anyCheckBoxChecked = false
                    sumCod(anyCheckBoxChecked)
                    items?.let { itemList ->
                        for (item in itemList) {
                            val checkBox = CheckBox(this)
                            checkBox.text = Html.fromHtml(
                                "${item?.name}, <b>${itemPriceLabel}:</b> ${item?.cod ?: 0}",
                                Html.FROM_HTML_MODE_LEGACY
                            )
                            if (isArabic) {
                                checkBox.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            }
                            checkBox.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            checkBox.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    item?.status = Status.DELIVERED
                                    anyCheckBoxChecked = true
                                } else {
                                    item?.status = Status.RETURNED
                                    anyCheckBoxChecked =
                                        itemList.any { it?.status == Status.DELIVERED }
                                }
                                sumCod(anyCheckBoxChecked)
                            }
                            checkBoxContainer.addView(checkBox)
                            checkChosen()
                        }
                    }
                } else {
                    binding.containerItemsFlow.visibility = View.GONE
                    binding.tvNoteTitle.text = getString(R.string.title_partial_delivery_note)
                }
            }
        }

        binding.itemPackageBarcode.buttonCopy.setOnClickListener {
            Helper.copyTextToClipboard(this, pkg?.barcode)
        }

        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.buttonClearSignature.setOnClickListener(this)
        binding.buttonDeliverPackage.setOnClickListener(this)
        binding.selectorCash.setOnClickListener(this)
        binding.selectorDigitalWallet.setOnClickListener(this)
        binding.selectorCheque.setOnClickListener(this)
        binding.selectorPrepaid.setOnClickListener(this)
        binding.selectorCardPayment.setOnClickListener(this)
        binding.selectorInterPay.setOnClickListener(this)
        binding.selectorNearPay.setOnClickListener(this)
        binding.selectorClickPay.setOnClickListener(this)
        binding.selectorBankTransfer.setOnClickListener(this)
        binding.buttonCaptureImage.setOnClickListener(this)
        binding.buttonTakeVideo.setOnClickListener(this)
        binding.buttonLoadImage.setOnClickListener(this)
        binding.buttonContextMenu.setOnClickListener(this)

        if (SharedPreferenceWrapper.getNotificationsCount() == "0") {
            binding.toolbarMain.notificationCount.visibility = View.GONE
        }
    }

    private fun checkChosen() {
        for (item in items!!) {
            if (item?.status != Status.DELIVERED) {
                item?.status = Status.RETURNED
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sumCod(anyCheckBoxChecked: Boolean) {
        var sum: Double? = pkg?.cod ?: 0.0
        var temp: Double? = 0.0
        for (item in items ?: emptyList()) {
            if (item?.status != Status.DELIVERED) {
                temp = temp?.plus(item?.cod!!)
            }
        }
        sum = sum?.minus(temp!!)
        val boldText = "<b>${getString(R.string.title_partial_delivery_cod)}</b>"
        val codText = Html.fromHtml("$boldText $sum", Html.FROM_HTML_MODE_LEGACY)
        binding.tvCodSum.text = codText
        if (!anyCheckBoxChecked) {
            binding.tvCodSum.visibility = View.GONE
        } else {
            binding.tvCodSum.visibility = View.VISIBLE
        }
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            pkg = extras.getParcelable(IntentExtrasKeys.PACKAGE_TO_DELIVER.name)
            packageCodToPay = pkg?.cod!!
        }
    }

    private fun initPaymentMethodsControls() {
        if (companyConfigurations?.isAddingPaymentTypesEnabled == true) {
            callGetPaymentMethods()
        } else {
            binding.containerDynamicPaymentMethods.visibility = View.GONE
            binding.containerStaticPaymentMethods.visibility = View.VISIBLE

            binding.selectorCash.enumValue = PaymentType.CASH
            binding.selectorDigitalWallet.enumValue = PaymentType.DIGITAL_WALLET
            binding.selectorCheque.enumValue = PaymentType.CHEQUE
            binding.selectorPrepaid.enumValue = PaymentType.PREPAID
            binding.selectorCardPayment.enumValue = PaymentType.CARD
            binding.selectorInterPay.enumValue = PaymentType.INTER_PAY
            binding.selectorNearPay.enumValue = PaymentType.NEAR_PAY
            binding.selectorClickPay.enumValue = PaymentType.CLICK_PAY
            binding.selectorBankTransfer.enumValue = PaymentType.BANK_TRANSFER
            val bottomMarginInPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                resources.displayMetrics
            ).toInt()
            if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes == true) {
                binding.textPaymentAmount.visibility = View.VISIBLE
                binding.textFieldCash.visibility = View.VISIBLE
                binding.textFieldCardPayment.visibility = View.VISIBLE
                binding.textFieldInterPay.visibility = View.VISIBLE
                binding.textFieldNearPay.visibility = View.VISIBLE
                binding.textFieldClickPay.visibility = View.VISIBLE
                binding.textFieldBankTransfer.visibility = View.VISIBLE
                binding.textFieldDigitalWallet.visibility = View.VISIBLE
                binding.textFieldCheque.visibility = View.VISIBLE
                binding.textFieldPrepaid.visibility = View.VISIBLE
                setupPaymentMethodSelectors()
            } else {
                binding.textPaymentAmount.visibility = View.GONE
                binding.textFieldCash.visibility = View.GONE
                binding.textFieldCardPayment.visibility = View.GONE
                binding.textFieldInterPay.visibility = View.GONE
                binding.textFieldNearPay.visibility = View.GONE
                binding.textFieldClickPay.visibility = View.GONE
                binding.textFieldBankTransfer.visibility = View.GONE
                binding.textFieldDigitalWallet.visibility = View.GONE
                binding.textFieldCheque.visibility = View.GONE
                binding.textFieldPrepaid.visibility = View.GONE

                val statusParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(0, 0, 0, bottomMarginInPixels)
                    }
                binding.selectorCash.layoutParams = statusParams
                binding.selectorDigitalWallet.layoutParams = statusParams
                binding.selectorCheque.layoutParams = statusParams
                binding.selectorPrepaid.layoutParams = statusParams
                binding.selectorCardPayment.layoutParams = statusParams
                binding.selectorInterPay.layoutParams = statusParams
                binding.selectorNearPay.layoutParams = statusParams
                binding.selectorClickPay.layoutParams = statusParams
                binding.selectorBankTransfer.layoutParams = statusParams
            }
        }
    }

    private fun fillButtonsList() {
        paymentTypeButtonsList.add(binding.selectorCash)
        paymentTypeButtonsList.add(binding.selectorDigitalWallet)
        paymentTypeButtonsList.add(binding.selectorCheque)
        paymentTypeButtonsList.add(binding.selectorPrepaid)
        paymentTypeButtonsList.add(binding.selectorCardPayment)
        paymentTypeButtonsList.add(binding.selectorInterPay)
        paymentTypeButtonsList.add(binding.selectorNearPay)
        paymentTypeButtonsList.add(binding.selectorClickPay)
        paymentTypeButtonsList.add(binding.selectorBankTransfer)
    }

    private fun unselectAllPaymentMethods() {
        for (item in paymentTypeButtonsList) {
            item.makeUnselected()
        }
    }

    private fun handleSelectPaymentMethod(selector: StatusSelector) {
        unselectAllPaymentMethods()
        selector.makeSelected()
        selectedPaymentType = selector
    }

    private fun validateInput(): Boolean {
        if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled != true) {
            if (!isSignatureEntered()) {
                Helper.showErrorMessage(this, getString(R.string.error_enter_signature))
                return false
            }
        }

        if (selectedDeliveryType == DeliveryType.PARTIAL) {
            if (binding.etPartialDeliveryNote.text.toString().isEmpty()) {
                Helper.showErrorMessage(
                    super.getContext(),
                    getString(R.string.error_enter_partial_delivery_note)
                )
                return false
            }
        }

        if ((companyConfigurations?.isSupportDeliveringPackageItemsPartially == true && items != null) ||
            (pkg?.integrationSource == IntegrationSource.FULFILLMENT)
        ) {
            var deliveredItemFound = false
            for (item in items ?: emptyList()) {
                if (item?.status == Status.DELIVERED) {
                    deliveredItemFound = true
                    break
                }
            }
            if (selectedDeliveryType == DeliveryType.PARTIAL) {
                if (!deliveredItemFound) {
                    Helper.showErrorMessage(this, getString(R.string.error_no_delivered_items))
                    return false
                }
            }

        }

        if (companyConfigurations?.isForceDriversToAddAttachments == true) {
            if (loadedImagesList.isEmpty()) {
                Helper.showErrorMessage(
                    this,
                    getString(R.string.error_add_attachments)
                )
                return false
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getPodImagesUrls(): List<String?>? {
        return if (loadedImagesList.isNotEmpty() || videoUrl.isNotEmpty()) {
            val list: ArrayList<String?> = ArrayList()
            for (item in loadedImagesList) {
                list.add(item.imageUrl)
            }
            list.add(videoUrl)
            list
        } else {
            null
        }
    }

    //Media Picker methods
    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK)
        pickPhoto.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(
            pickPhoto,
            AppConstants.REQUEST_LOAD_PHOTO
        )
    }

    private fun openCamera(isVideo: Boolean = false): String? {
        val intent = if (isVideo) {
            // Video capture intent
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_DURATION_LIMIT, 14) // 15-second limit
                putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0) // 0 for low quality (~240p)
            }
        } else {
            // Photo capture intent
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        }

        if (intent.resolveActivity(this.packageManager) != null) {
            val mediaFile = try {
                if (isVideo) Helper.createVideoFile(this) else Helper.createImageFile(this)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return null
            }

            if (mediaFile != null) {
                val mediaUri = FileProvider.getUriForFile(
                    this.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    mediaFile
                )

                val currentMediaPath = "file:" + mediaFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)

                // Handle permissions for older Android versions
                if (SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    intent.clipData = ClipData.newRawUri("", mediaUri)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val requestCode = if (isVideo) AppConstants.REQUEST_TAKE_VIDEO else AppConstants.REQUEST_TAKE_PHOTO
                this.startActivityForResult(intent, requestCode)

                return currentMediaPath
            }
        }
        return null
    }

    private fun validateAndUploadVideo(videoUri: Uri?) {
        if (videoUri == null) {
            Toast.makeText(applicationContext, getString(R.string.error_image_capture_failed), Toast.LENGTH_LONG).show()
            return
        }

        showWaitDialog()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val videoPath = Helper.getRealPathFromURI(super.getContext(), videoUri) ?: ""
                val videoFile = File(videoPath)

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(super.getContext(), videoUri)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                retriever.release()

                if (duration > 15000) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                        Toast.makeText(
                            applicationContext,
                            "Video too long",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val compressedVideoFile = compressVideo(videoFile)

                uploadVideoFile(compressedVideoFile)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideWaitDialog()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_image_capture_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    private fun compressVideo(inputFile: File): File {
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: cacheDir
        val outputFile = File(outputDir, "compressed_${System.currentTimeMillis()}.mp4")

        try {
            val mediaCodec = MediaCodec.createEncoderByType("video/avc")
            val format = MediaFormat.createVideoFormat("video/avc", 426, 240).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, 500000) // 500 kbps
                setInteger(MediaFormat.KEY_FRAME_RATE, 24)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }

            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec.createInputSurface()
            mediaCodec.start()

            val muxer = MediaMuxer(outputFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // ... (actual encoding implementation would go here) ...

            mediaCodec.stop()
            mediaCodec.release()
            muxer.stop()
            muxer.release()

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to original file if compression fails
            return inputFile
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_TAKE_PHOTO -> if (resultCode == RESULT_OK) {
                selectedPodImageUri = Uri.parse(mCurrentPhotoPath)
                if (selectedPodImageUri != null) {
                    val compressedImage =
                        Helper.validateCompressedImage(
                            selectedPodImageUri!!,
                            true,
                            super.getContext()
                        )
                    if (compressedImage != null) {
                        loadedImagesList.add(compressedImage)
                        if (loadedImagesList.size > 0 && loadedImagesList[loadedImagesList.size - 1]
                                .imageUrl == null
                        ) {
                            callUploadPodImage(loadedImagesList[loadedImagesList.size - 1])
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_image_capture_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_image_capture_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_LOAD_PHOTO -> if (resultCode == RESULT_OK && data != null) {
                selectedPodImageUri = data.data
                if (selectedPodImageUri != null) {
                    val compressedImage =
                        Helper.validateCompressedImage(
                            selectedPodImageUri!!,
                            false,
                            super.getContext()
                        )
                    if (compressedImage != null) {
                        loadedImagesList.add(compressedImage)
                        if (loadedImagesList.size > 0 && loadedImagesList[loadedImagesList.size - 1].imageUrl == null
                        ) {
                            callUploadPodImage(loadedImagesList[loadedImagesList.size - 1])
                        }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_image_loading),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_image_loading),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            AppConstants.REQUEST_TAKE_VIDEO -> if (resultCode == RESULT_OK) {
                selectedVideoUri = data?.data ?: Uri.parse(mCurrentVideoPath)
                validateAndUploadVideo(selectedVideoUri)
            }

            AppConstants.REQUEST_SCAN_BARCODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    callAddPackageNote(
                        pkg?.id,
                        AddNoteRequestBody(
                            data.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name),
                            null,
                            packageId = pkg?.id
                        )
                    )
                }
            }

            AppConstants.REQUEST_VERIFY_PACKAGE -> {
                if (resultCode == RESULT_OK) {
                    val verificationStatus = data?.getBooleanExtra("verificationStatus", false)
                    val barcodes = data?.getStringArrayListExtra("subpackagesBarcodes") ?: emptyList()
                    scannedSubpackagesBarcodes = barcodes
                    if (verificationStatus == true) {
                        handlePackageDelivery()
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_camera_and_storage_permissions)
                        )
                    }
                }
            }

            AppConstants.OPEN_SOFTPOS_RESULT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data?.getBundleExtra("data")
                    if (bundle != null) {
                        val transactionId = bundle.getString("rrNumber", "")
                        val isFailed = bundle.getBoolean("isFailed")
                        val txtType = bundle.getString("txtType", "")
                        val responseCode = bundle.getString("responseCode", "")
                        val approvalCode = bundle.getString("approvalCode", "")

                        if (isFailed || (txtType != null && txtType.toLowerCase(Locale.ROOT) == "reversal")) {
                            Helper.showErrorMessage(this, getString(R.string.error_transaction_failed))
                        } else {
                            if (responseCode == "000" && approvalCode != null) {
                                callPaymentGateway(PaymentGatewayType.INTER_PAY, transactionId)
                            } else {
                                Helper.showErrorMessage(this, getString(R.string.error_transaction_failed_by_bank))
                            }
                        }
                    } else {
                        handleExceptionFromSoftpos("400", "Invalid response from SoftPOS", null)
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    val bundle = data?.getBundleExtra("data")
                    val errorCode = bundle?.getString("errorCode")
                    if (errorCode != null) {
                        handleExceptionFromSoftpos(
                            bundle.getString("errorCode", ""),
                            bundle.getString("errorMessage", ""),
                            bundle.getString("errorDetails", "")
                        )
                    } else {
                        handleExceptionFromSoftpos("400", "User has cancelled the payment.", null)
                    }
                } else {
                    handleExceptionFromSoftpos("404", "Unable to get data", null)
                }
            }

            else -> {}
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (Helper.isStorageAndCameraPermissionNeeded(this)
                ) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    if (isCameraAction) {
                        mCurrentPhotoPath = openCamera()
                        isCameraAction = false
                    } else {
                        openGallery()
                    }
                }
            } else {
                if (Helper.shouldShowCameraAndStoragePermissionDialog(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_camera_and_storage_permissions)
                    )
                }
            }
        } else if (requestCode == AppConstants.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                if (Helper.isStoragePermissionNeeded(this)
                ) {
                    Helper.showAndRequestStorageDialog(this)
                } else {
                    uploadPackageSignature()
                }
            } else {
                if (Helper.shouldShowStoragePermissionDialog(this)) {
                    Helper.showAndRequestStorageDialog(this)
                } else {
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_storage_permission)
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPaymentMethods(paymentTypes: List<CodCollectionMethod>) {
        binding.containerDynamicPaymentMethods.visibility = View.VISIBLE
        binding.containerStaticPaymentMethods.visibility = View.GONE
        if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes == true) {
            binding.textPaymentAmount.visibility = View.VISIBLE
        } else {
            binding.textPaymentAmount.visibility = View.GONE
        }

        val container = findViewById<LinearLayout>(R.id.container_dynamic_payment_methods)
        val textFieldIds = mutableListOf<Int>()

        for (paymentType in paymentTypes) {
            val statusSelector = StatusSelector(this)
            statusSelector.setTextStatus(paymentType.paymentTypeName)
            statusSelector.enumValue = paymentType.staticPaymentTypeName

            val horizontalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textField = EditText(this).apply {
                hint = getString(R.string.hint_value)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                inputType = InputType.TYPE_CLASS_NUMBER
                isEnabled = false

                id = View.generateViewId()
                textFieldIds.add(id)
            }

            val statusLayoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
            statusLayoutParams.setMargins(0, 12, 0, 0)
            statusSelector.layoutParams = statusLayoutParams
            val bottomMarginInPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6f,
                resources.displayMetrics
            ).toInt()
            val statusParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 0, bottomMarginInPixels)
            }

            if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes == true) {
                horizontalLayout.addView(statusSelector)
                horizontalLayout.addView(textField)
            } else {
                horizontalLayout.removeView(textField)
                statusSelector.layoutParams = statusParams
                horizontalLayout.addView(statusSelector)
            }
            container.addView(horizontalLayout)
            statusSelector.setOnClickListener {
                selectedPaymentType?.makeUnselected()
                selectedPaymentType?.parent?.let { parentLayout ->
                    if (parentLayout is LinearLayout) {
                        if (parentLayout.childCount > 1) {
                            val previousTextField = parentLayout.getChildAt(1) as? EditText
                            previousTextField?.isEnabled = false
                        }
                    }
                }
                statusSelector.makeSelected()
                selectedPaymentType = statusSelector
                paymentTypeId = paymentType.id.toLong()
                textField.isEnabled = true
                textField.requestFocus()
                showKeyboard(textField)
                textField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        var newSum = 0.0
                        for (id in textFieldIds) {
                            val field = findViewById<EditText>(id)
                            val value = field.text.toString().toDoubleOrNull() ?: 0.0
                            newSum += value
                        }

                        if (newSum > packageCodToPay) {
                            val currentValue = textField.text.toString().toDoubleOrNull() ?: 0.0
                            newSum -= currentValue
                            textField.removeTextChangedListener(this)
                            textField.setText("")
                            textField.addTextChangedListener(this)

                            Helper.showErrorMessage(this@PackageDeliveryActivity, getString(R.string.error_can_not_exceed_cod))
                        }

                        sum = newSum
                        binding.textPaymentAmount.text = "${sum?.format()}/${packageCodToPay.format()}"

                        // Remove item if text field is empty
                        val amount = textField.text.toString().toDoubleOrNull()
                        if (amount == null || amount == 0.0) {
                            paymentDataList.removeAll { it.paymentTypeId == paymentType.id.toLong() }
                        } else {
                            // Update or add the new amount for this paymentTypeId
                            val paymentData = PayMultiWayRequestBody(
                                paymentType.paymentTypeName,
                                paymentTypeId = paymentType.id.toLong(),
                                amount = amount
                            )
                            paymentDataList.removeAll { it.paymentTypeId == paymentType.id.toLong() }
                            paymentDataList.add(paymentData)
                        }

                        Log.d("paymentDataList", "${paymentDataList.toString()}")

                        Log.d("sum", "${sum?.format()}/${packageCodToPay.format()}")
                    }
                })

                textField.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val amount = textField.text.toString().toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val paymentData = PayMultiWayRequestBody(
                                paymentType = paymentType.paymentTypeName.takeIf { paymentTypeId == null },
                                paymentTypeId = paymentTypeId ?: paymentType.id.toLong(),
                                amount = amount
                            )
                            paymentDataList.add(paymentData)
                        }
                        if (paymentDataList.size > 4) {
                            paymentDataList.removeLast()
                            Helper.showErrorMessage(this@PackageDeliveryActivity, getString(R.string.error_max_payment_methods_selected))
                            textField.setText("")
                        }
                    }
                    Log.d("paymentDataList", "${paymentDataList.toString()}")
                    val lastPaymentDataList = paymentDataList
                        .groupBy { it.paymentTypeId }
                        .map { (_, entries) -> entries.last() }

                    paymentDataList = lastPaymentDataList.toMutableList()
                    Log.d("lastPaymentDataList", lastPaymentDataList.toString())
                    Log.d("paymentDataList", "${paymentDataList.toString()}")
                }
                Log.d("sum", "${sum?.format()}")
            }
            Log.d("paymentDataList", "${paymentDataList.toString()}")
        }
    }

    // Define a data class to hold the status selector and the associated EditText
    data class PaymentSelector(
        val selector: StatusSelector,
        val editText: EditText
    )

    private fun setupPaymentMethodSelectors() {
        val paymentSelectors = listOf(
            PaymentSelector(binding.selectorCash, binding.textFieldCash),
            PaymentSelector(binding.selectorDigitalWallet, binding.textFieldDigitalWallet),
            PaymentSelector(binding.selectorCheque, binding.textFieldCheque),
            PaymentSelector(binding.selectorPrepaid, binding.textFieldPrepaid),
            PaymentSelector(binding.selectorCardPayment, binding.textFieldCardPayment),
            PaymentSelector(binding.selectorBankTransfer, binding.textFieldBankTransfer),
            PaymentSelector(binding.selectorInterPay, binding.textFieldInterPay),
            PaymentSelector(binding.selectorNearPay, binding.textFieldNearPay),
            PaymentSelector(binding.selectorClickPay, binding.textFieldClickPay)
        )

        paymentSelectors.forEach { paymentSelector ->
            paymentSelector.selector.setOnClickListener {
                handlePaymentSelection(
                    paymentSelector.selector,
                    paymentSelector.editText,
                    paymentSelectors
                )
            }

            // Add TextWatcher to update the payment amount when text changes
            paymentSelector.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    var newSum = 0.0
                    paymentSelectors.forEach { selector ->
                        val fieldValue = selector.editText.text.toString().toDoubleOrNull() ?: 0.0
                        newSum += fieldValue
                    }

                    if (newSum > packageCodToPay) {
                        val currentFieldValue =
                            paymentSelector.editText.text.toString().toDoubleOrNull() ?: 0.0
                        newSum -= currentFieldValue
                        paymentSelector.editText.removeTextChangedListener(this)
                        paymentSelector.editText.setText("")
                        paymentSelector.editText.addTextChangedListener(this)

                        Helper.showErrorMessage(
                            this@PackageDeliveryActivity,
                            getString(R.string.error_can_not_exceed_cod)
                        )
                    } else {
                        sum = newSum
                    }

                    sum = newSum
                    binding.textPaymentAmount.text = "${sum?.format()}/${packageCodToPay.format()}"
                    val amount = paymentSelector.editText.text.toString().toDoubleOrNull()
                    if (amount == null || amount == 0.0) {
                        paymentDataList.removeAll { it.paymentType == paymentSelector.selector.enumValue }
                    } else {
                        // Update or add the new amount for this paymentTypeId
                        val paymentData = PayMultiWayRequestBody(
                            paymentSelector.selector.enumValue.toString(),
                            null,
                            amount = amount
                        )
                        paymentDataList.removeAll { it.paymentType == paymentSelector.selector.enumValue }
                        paymentDataList.add(paymentData)
                    }

                    Log.d("paymentDataList", "${paymentDataList.toString()}")

                    Log.d("sum", "${sum?.format()}/${packageCodToPay.format()}")
                }
            })

            paymentSelector.editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val amount = paymentSelector.editText.text.toString().toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val paymentData = PayMultiWayRequestBody(
                            paymentType = paymentSelector.selector.enumValue.toString()
                                .takeIf { paymentTypeId == null },
                            null,
                            amount = amount
                        )
                        paymentDataList.add(paymentData)
                    }
                }
                Log.d("paymentDataList", "${paymentDataList.toString()}")
                val lastPaymentDataList = paymentDataList
                    .groupBy { it.paymentType }
                    .map { (_, entries) -> entries.last() }

                paymentDataList = lastPaymentDataList.toMutableList()
                if (lastPaymentDataList.size > 3) {
                    lastPaymentDataList.dropLast(1)
                    Helper.showErrorMessage(this@PackageDeliveryActivity, getString(R.string.error_max_payment_methods_selected))
                    paymentSelector.editText.setText("")
                }
            }
        }
    }

    private fun handlePaymentSelection(selectedSelector: StatusSelector, selectedEditText: EditText, paymentSelectors: List<PaymentSelector>) {
        selectedPaymentType?.makeUnselected()

        selectedSelector.makeSelected()
        selectedPaymentType = selectedSelector

        selectedEditText.isEnabled = true
        selectedEditText.requestFocus()
        showKeyboard(selectedEditText)

        paymentSelectors.forEach { paymentSelector ->
            if (paymentSelector.editText != selectedEditText) {
                paymentSelector.editText.isEnabled = false
                paymentSelector.selector.makeUnselected()
            }
        }
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    //Apis
    private fun uploadPackageSignature() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    bitmap = Bitmap.createBitmap(
                        binding.gestureViewSignature.width,
                        binding.gestureViewSignature.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap!!)
                    binding.gestureViewSignature.draw(canvas)
                    file?.createNewFile()
                    val fos = FileOutputStream(file)
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()

                    // resize and compress to reasonable size
                    val bytes = ByteArrayOutputStream()
                    bitmap?.compress(
                        Bitmap.CompressFormat.JPEG,
                        AppConstants.IMAGE_FULL_QUALITY,
                        bytes
                    )

                    val reqFile: RequestBody =
                        bytes.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "file",
                        pkg?.id.toString() +
                                "__signature_image" +
                                "_" + System.currentTimeMillis() +
                                ".jpg", reqFile
                    )

                    val response = ApiAdapter.apiClient.uploadPackageSignature(
                        pkg?.id ?: -1,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            callDeliverPackage(response.body()?.fileUrl)
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

    private fun callUploadPodImage(loadedImage: LoadedImage?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val file: File = File(
                        Helper.getRealPathFromURI(
                            super.getContext(),
                            loadedImage?.imageUri!!
                        ) ?: ""
                    )
                    val bitmap: Bitmap = MediaStore.Images.Media
                        .getBitmap(super.getContext().contentResolver, Uri.fromFile(file))

                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        AppConstants.IMAGE_FULL_QUALITY,
                        bytes
                    )

                    val reqFile: RequestBody =
                        bytes.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())

                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("en")).format(Date())
                    val imageFileName = "JPEG_" + timeStamp + "_"

                    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "file",
                        "$imageFileName.jpeg", reqFile
                    )

                    val response = ApiAdapter.apiClient.uploadPodImage(
                        pkg?.id ?: -1,
                        true,
                        body
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                            loadedImagesList[loadedImagesList.size - 1].imageUrl =
                                response.body()?.fileUrl
                            (binding.rvThumbnails.adapter as ThumbnailsAdapter).updateItem(
                                loadedImagesList.size - 1
                            )
                            binding.containerThumbnails.visibility = View.VISIBLE
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

    private fun uploadVideoFile(videoFile: File) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val requestFile = videoFile.readBytes().toRequestBody("video/mp4".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData(
                    "file",
                    videoFile.name,
                    requestFile
                )

                val response = ApiAdapter.apiClient.uploadPodImage(
                    pkg?.id ?: -1,
                    true,
                    body
                )

                withContext(Dispatchers.Main) {
                    hideWaitDialog()
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_upload_video)
                            )
                            videoUrl = response.body()!!.fileUrl!!
                        }
                    } else {
                        // Handle error
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_video_capture_failed)
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideWaitDialog()
                    Helper.showErrorMessage(
                        super.getContext(),
                        getString(R.string.error_video_capture_failed)
                    )
                }
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    private fun callDeletePodImage(position: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.deletePodImage(DeleteImageRequestBody(loadedImagesList[position].imageUrl))
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            loadedImagesList.removeAt(position)
                            (binding.rvThumbnails.adapter as ThumbnailsAdapter).deleteItem(position)
                            if (loadedImagesList.isEmpty()) {
                                binding.containerThumbnails.visibility = View.GONE
                            }
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
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

    private fun callDeliverPackage(signatureUrl: String?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(super.getContext())
            var latitude: Double? = null
            var longitude: Double? = null
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    latitude = location?.latitude
                    longitude = location?.longitude
                }
            }

            val paymentType: String? = if (companyConfigurations?.isAddingPaymentTypesEnabled == true) {
                null
            } else {
                selectedPaymentType?.let {
                    (it.enumValue as PaymentType).name
                }
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    var note: String? = null
                    var subPackgesBarCode: List<String?>? = null
                    if (selectedDeliveryType == DeliveryType.PARTIAL) {
                        note = binding.etPartialDeliveryNote.text.toString()
                        subPackgesBarCode = scannedSubpackagesBarcodes.toList()
                    }
                    val response = ApiAdapter.apiClient.deliverPackage(
                        pkg?.barcode,
                        selectedDeliveryType?.name,
                        note,
                        body = DeliverPackageRequestBody(
                            pkg?.id,
                            longitude,
                            latitude,
                            0.0,
                            0.0,
                            signatureUrl,
                            getPodImagesUrls(),
                            null,
                            null,
                            paymentType,
                            paymentTypeId,
                            items,
                            subPackgesBarCode
                        )
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            val returnIntent = Intent()
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
                            Helper.showErrorMessage(
                                super.getContext(),
                                e.stackTraceToString()
                            )
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

    private fun callAddPackageNote(packageId: Long?, body: AddNoteRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.addPackageNote(
                        packageId,
                        body
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

    private fun requestPinCodeSms(isFirstPin: Boolean = true) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.requestPinCodeSms(
                        pkg?.id
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            pkg?.verificationStatus = VerificationStatus.SENT.toString()
                            if (isFirstPin) {
                                showDeliveryCodeVerificationDialog()
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

    private fun requestPaymentLinkClickPay() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.requestPaymentLinkClickPay(
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

    private fun callVerifyClickPay() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.verifyClickPay(
                        pkg?.id
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            isClickPayVerified = response.body()!!.isPaid!!
                            makePackageDelivery()
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

    private fun callPaymentGateway(type: PaymentGatewayType, transactionId: String) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.paymentGateway(
                        pkg?.id,
                        transactionId,
                        type
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes == true) {
                                submitValues()
                            } else {
                                makePackageDelivery()
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

    private fun callGetPaymentMethods() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getPaymentMethods(pkg?.customerId!!)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            setPaymentMethods(response.body()!!)
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

    private fun callPayMultiWay() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.payMultiWayNew(
                            pkg?.id,
                            paymentDataList
                        )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (response.body()!!.paymentStatus == "PAID") {
                                selectedPaymentType = null
                                paymentTypeId = null
                                makePackageDelivery()
                            } else {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_pay_all_cod)
                                )
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

    private fun showDeliveryCodeVerificationDialog() {
        DeliveryCodeVerificationDialog(super.getContext(), this, pkg).showDialog()
    }

    private fun needsPinVerification(): Boolean {
        if (companyConfigurations != null) {
            if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true || isSignatureEntered()) {
                if ((pkg?.shipmentType == PackageType.REGULAR.toString() || pkg?.cod == 0.0) && companyConfigurations?.isEnableDeliveryVerificationPinCodeForPkgs == true) {
                    return when (pkg?.verificationStatus) {
                        VerificationStatus.NOT_SENT.toString() -> {
                            requestPinCodeSms()
                            true
                        }

                        VerificationStatus.SENT.toString() -> {
                            showDeliveryCodeVerificationDialog()
                            true
                        }

                        VerificationStatus.VERIFIED.toString() -> {
                            false
                        }

                        else -> {
                            false
                        }
                    }
                } else if (companyConfigurations?.isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan!! > -1 && (pkg?.cod
                        ?: 0.0) >= companyConfigurations!!.isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan!!
                ) {
                    return when (pkg?.verificationStatus) {
                        VerificationStatus.NOT_SENT.toString() -> {
                            requestPinCodeSms()
                            true
                        }

                        VerificationStatus.SENT.toString() -> {
                            showDeliveryCodeVerificationDialog()
                            true
                        }

                        VerificationStatus.VERIFIED.toString() -> {
                            false
                        }

                        else -> {
                            false
                        }
                    }
                }
            } else {
                return false
            }
        } else {
            return false
        }
        return false
    }

    private fun handlePackageDelivery() {
        if (selectedPaymentType?.textView?.text == PaymentType.CLICK_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.CLICK_PAY.name) {
            if (isClickPayVerified) {
                if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true) {
                    callDeliverPackage(null)
                } else {
                    if (Helper.isStoragePermissionNeeded(this)) {
                        Helper.showAndRequestStorageDialog(this)
                    } else {
                        uploadPackageSignature()
                    }
                }
            } else {
                (this as LogesTechsActivity).showConfirmationDialog(
                    getString(R.string.click_pay_confirmation),
                    pkg,
                    ConfirmationDialogAction.CLICKPAY_RESULT,
                    this
                )
            }
        } else {
            if (companyConfigurations?.isSignatureOnPackageDeliveryDisabled == true) {
                callDeliverPackage(null)
            } else {
                if (Helper.isStoragePermissionNeeded(this)) {
                    Helper.showAndRequestStorageDialog(this)
                } else {
                    uploadPackageSignature()
                }
            }
        }
    }

    private fun makePackageDelivery() {
        if (!needsPinVerification()) {
            if (companyConfigurations?.isDriverProveDeliveryByScanBarcode!! && companyConfigurations?.proofOfDeliveryShipmentTypes?.contains(pkg?.shipmentType)!!) {
                val mIntent = Intent(
                    super.getContext(),
                    VerifyPackageDeliveryActivity::class.java
                )
                mIntent.putExtra("barcode", pkg?.barcode)
                mIntent.putExtra("invoice", pkg?.invoiceNumber)
                SharedPreferenceWrapper.saveSubpackagesQuantity(pkg?.quantity ?: 0)
                if (binding.radioButtonFullDelivery.isChecked) {
                    SharedPreferenceWrapper.resetIsPartiallyDelivered()
                } else if (binding.radioButtonPartialDelivery.isChecked) {
                    SharedPreferenceWrapper.saveIsPartiallyDelivered(true)
                }
                startActivityForResult(mIntent, AppConstants.REQUEST_VERIFY_PACKAGE)
            } else {
                handlePackageDelivery()
            }
        }
    }

    private fun startSoftposApp(cod: String) {
        val codValue = cod.toDoubleOrNull()
        val decimalFormat = DecimalFormat("#.00", DecimalFormatSymbols(Locale.ENGLISH))
        val formattedCod = decimalFormat.format(codValue)

        val sendIntent = Intent()
        sendIntent.setClassName(AppConstants.SOFTPOS_PACKAGE_NAME, AppConstants.SOFTPOS_CLASS_NAME)

        val bundle = Bundle()
        bundle.putString("amount", formattedCod)
        bundle.putString("transaction_type", "Sale")
        sendIntent.putExtra("data", bundle)
        sendIntent.type = "text/plain"

        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(sendIntent, AppConstants.OPEN_SOFTPOS_RESULT_CODE)
        } else {
            Log.e("Error", "SoftPOS app not installed")
        }
    }

    private fun startNearPay(cod: Double) {
        val nearPay = NearPay.Builder()
            .context(this)
            .authenticationData(AuthenticationData.UserEnter)
            .environment(Environments.PRODUCTION)
            .locale(Locale.getDefault())
            .networkConfiguration(NetworkConfiguration.SIM_PREFERRED)
            .uiPosition(UIPosition.CENTER_BOTTOM)
            .paymentText(PaymentText("يرجى تمرير البطاقة", "please tap your card"))
            .loadingUi(true)
            .build()

        val amount: Long = cod.toLong() * 100
        val customerReferenceNumber = pkg?.barcode
        val enableReceiptUi = true
        val enableReversal = true
        val finishTimeOut: Long = 10
        val transactionId = UUID.randomUUID()
        val enableUiDismiss = true

        nearPay.purchase(
            amount,
            customerReferenceNumber,
            enableReceiptUi,
            enableReversal,
            finishTimeOut,
            transactionId,
            enableUiDismiss,
            object :
                PurchaseListener {
                override fun onPurchaseApproved(transactionData: TransactionData) {
                    val transactionId = transactionData.receipts?.get(0)?.receipt_id
                    callPaymentGateway(PaymentGatewayType.NEAR_PAY, transactionId!!)
                }

                override fun onPurchaseFailed(purchaseFailure: PurchaseFailure) {
                    when (purchaseFailure) {
                        is PurchaseFailure.PurchaseDeclined -> {
                        }

                        is PurchaseFailure.PurchaseRejected -> {
                        }

                        is PurchaseFailure.AuthenticationFailed -> {
                        }

                        is PurchaseFailure.InvalidStatus -> {
                        }

                        is PurchaseFailure.GeneralFailure -> {
                        }

                        is PurchaseFailure.UserCancelled -> {
                        }
                    }
                }
            })
    }

    private fun handleExceptionFromSoftpos(
        errorCode: String,
        errorMessage: String,
        errorDetails: String?
    ) {
        Log.e(
            "SoftPOS Exception",
            "ErrorCode: $errorCode, ErrorMessage: $errorMessage, ErrorDetails: $errorDetails"
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_clear_signature -> {
                binding.gestureViewSignature.invalidate()
                binding.gestureViewSignature.clear(true)
                binding.gestureViewSignature.clearAnimation()
                binding.gestureViewSignature.cancelClearAnimation()
                gestureTouch = false
            }

            R.id.button_deliver_package -> {
                if (validateInput()) {
                    if (companyConfigurations?.isPromptNoteForDriverInPackageDelivery == true && !notes.isNullOrEmpty()){
                        (this as LogesTechsActivity).showConfirmationDialog(
                            notes.toString(),
                            pkg,
                            ConfirmationDialogAction.PACKAGE_NOTE,
                            this
                        )
                    } else {
                        (this as LogesTechsActivity).showConfirmationDialog(
                            getString(R.string.warning_deliver_package),
                            pkg,
                            ConfirmationDialogAction.DELIVER_PACKAGE,
                            this
                        )
                    }
                }
            }

            R.id.button_capture_image -> {
                isCameraAction = true
                if (Helper.isStorageAndCameraPermissionNeeded(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    mCurrentPhotoPath = openCamera()
                }
            }

            R.id.button_take_video -> {
                isCameraAction = true
                if (Helper.isStorageAndCameraPermissionNeeded(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    mCurrentVideoPath = openCamera(isVideo = true)
                }
            }

            R.id.button_load_image -> {
                isCameraAction = false
                if (Helper.isStorageAndCameraPermissionNeeded(this)) {
                    Helper.showAndRequestCameraAndStorageDialog(this)
                } else {
                    openGallery()
                }
            }

            R.id.selector_cash -> {
                handleSelectPaymentMethod(binding.selectorCash)
            }

            R.id.selector_digital_wallet -> {
                handleSelectPaymentMethod(binding.selectorDigitalWallet)
            }

            R.id.selector_cheque -> {
                handleSelectPaymentMethod(binding.selectorCheque)
            }

            R.id.selector_prepaid -> {
                handleSelectPaymentMethod(binding.selectorPrepaid)
            }

            R.id.selector_card_payment -> {
                handleSelectPaymentMethod(binding.selectorCardPayment)
            }

            R.id.selector_inter_pay -> {
                handleSelectPaymentMethod(binding.selectorInterPay)
            }

            R.id.selector_near_pay -> {
                handleSelectPaymentMethod(binding.selectorNearPay)
            }

            R.id.selector_click_pay -> {
                handleSelectPaymentMethod(binding.selectorClickPay)
                (this as LogesTechsActivity).showConfirmationDialog(
                    getString(R.string.click_pay_confirmation),
                    pkg,
                    ConfirmationDialogAction.CLICKPAY_PAYMENT,
                    this
                )
            }

            R.id.selector_bank_transfer -> {
                handleSelectPaymentMethod(binding.selectorBankTransfer)
            }

            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }

            R.id.button_context_menu -> {
                binding.buttonContextMenu.setOnClickListener {
                    val popup = PopupMenu(super.getContext(), binding.buttonContextMenu)
                    popup.inflate(R.menu.package_delivery_context_menu)
                    popup.setOnMenuItemClickListener { item: MenuItem? ->
                        when (item?.itemId) {
                            R.id.action_scan_barcode -> {
                                val scanBarcode =
                                    Intent(super.getContext(), SingleScanBarcodeScanner::class.java)
                                this.startActivityForResult(
                                    scanBarcode,
                                    AppConstants.REQUEST_SCAN_BARCODE
                                )
                            }
                        }
                        true
                    }
                    popup.show()
                }
            }
        }
    }


    override fun onDeleteImage(position: Int) {
        callDeletePodImage(position)
    }

    override fun onPackageVerified() {
        handlePackageDelivery()
    }

    override fun onResendPinSms() {
        requestPinCodeSms(isFirstPin = false)
    }

    override fun confirmAction(data: Any?, action: ConfirmationDialogAction) {
        if (action == ConfirmationDialogAction.CLICKPAY_PAYMENT) {
            requestPaymentLinkClickPay()
        } else if (action == ConfirmationDialogAction.CLICKPAY_RESULT) {
            isClickPayVerified
            handlePackageDelivery()
        } else if (action == ConfirmationDialogAction.DELIVER_PACKAGE) {
            if (companyConfigurations?.isEnableDeliverByMultiPaymentTypes == true) {
                submitValues()
            } else {
                if (selectedPaymentType?.textView?.text == PaymentType.INTER_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.INTER_PAY.name) {
                    if (isAppInstalled(packageManager, AppConstants.SOFTPOS_PACKAGE_NAME)) {
                        startSoftposApp(pkg?.cod?.format()!!)
                        return
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(), getString(R.string.error_app_is_not_installed)
                        )
                    }
                } else if (selectedPaymentType?.textView?.text == PaymentType.CLICK_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.CLICK_PAY.name) {
                    callVerifyClickPay()
                } else if (selectedPaymentType?.textView?.text == PaymentType.NEAR_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.NEAR_PAY.name) {
                    startNearPay(pkg?.cod!!)
                } else {
                    makePackageDelivery()
                }
            }
        } else if (action == ConfirmationDialogAction.PACKAGE_NOTE) {
            showConfirmationDialog(
                getString(R.string.warning_deliver_package),
                pkg,
                ConfirmationDialogAction.DELIVER_PACKAGE,
                this
            )
        }
    }

    override fun onValueInserted(
        value: Double,
        selectedPaymentType: StatusSelector?,
        paymentTypeId: Long?
    ) {
        packageValueToPay = value
        this.selectedPaymentType = selectedPaymentType
        this.paymentTypeId = paymentTypeId

        if (selectedPaymentType?.textView?.text == PaymentType.INTER_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.INTER_PAY.name) {
            if (isAppInstalled(packageManager, AppConstants.SOFTPOS_PACKAGE_NAME)) {
                startSoftposApp(packageCodToPay.format())
                return
            } else {
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_app_is_not_installed)
                )
            }
        } else if (selectedPaymentType?.textView?.text == PaymentType.NEAR_PAY.englishLabel || selectedPaymentType?.enumValue == PaymentType.NEAR_PAY.name) {
            startNearPay(packageCodToPay)
        } else {
            callPayMultiWay()
        }
    }

    private fun submitValues() {
        for (paymentData in paymentDataList) {
            when (paymentData.paymentType) {
                PaymentType.INTER_PAY.englishLabel -> {
                    if (isAppInstalled(packageManager, AppConstants.SOFTPOS_PACKAGE_NAME)) {
                        paymentDataList = paymentDataList.filter {
                            it.paymentType != PaymentType.INTER_PAY.englishLabel
                        }.toMutableList()
                        startSoftposApp(packageCodToPay.format())
                        continue
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(),
                            getString(R.string.error_app_is_not_installed)
                        )
                    }
                }
                PaymentType.NEAR_PAY.englishLabel -> {
                    paymentDataList = paymentDataList.filter {
                        it.paymentType != PaymentType.NEAR_PAY.englishLabel
                    }.toMutableList()
                    startNearPay(packageCodToPay)
                }
                else -> {}
            }
        }
        if (paymentDataList.isNotEmpty() && sum == packageCodToPay){
            callPayMultiWay()
        } else if (pkg?.shipmentType == PackageType.REGULAR.name) {
            makePackageDelivery()
        } else {
            Helper.showErrorMessage(
                super.getContext(),
                getString(R.string.error_pay_all_cod)
            )
        }
    }
}