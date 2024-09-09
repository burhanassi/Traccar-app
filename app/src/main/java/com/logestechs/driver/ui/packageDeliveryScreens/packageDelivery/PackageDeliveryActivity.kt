package com.logestechs.driver.ui.packageDeliveryScreens.packageDelivery

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
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

        if (companyConfigurations?.isAddingPaymentTypesEnabled == false) {
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

            if (Helper.getCompanyCurrency() == AppCurrency.SAR.value) {
                binding.containerPaymentGateways.visibility = View.VISIBLE
            }
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

            binding.selectorCash.makeSelected()
            selectedPaymentType = binding.selectorCash
            binding.selectorCash.enumValue = PaymentType.CASH
            binding.selectorDigitalWallet.enumValue = PaymentType.DIGITAL_WALLET
            binding.selectorCheque.enumValue = PaymentType.CHEQUE
            binding.selectorPrepaid.enumValue = PaymentType.PREPAID
            binding.selectorCardPayment.enumValue = PaymentType.CARD
            binding.selectorInterPay.enumValue = PaymentType.INTER_PAY
            binding.selectorNearPay.enumValue = PaymentType.NEAR_PAY
            binding.selectorClickPay.enumValue = PaymentType.CLICK_PAY
            binding.selectorBankTransfer.enumValue = PaymentType.BANK_TRANSFER
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
        return if (loadedImagesList.isNotEmpty()) {
            val list: ArrayList<String?> = ArrayList()
            for (item in loadedImagesList) {
                list.add(item.imageUrl)
            }
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

    private fun openCamera(): String? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            var photoFile: File? = null
            photoFile = try {
                Helper.createImageFile(this)
            } catch (ex: IOException) {
                return ""
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    photoFile
                )
                val mCurrentPhotoPath = "file:" + photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                if (SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.clipData = ClipData.newRawUri("", photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                this.startActivityForResult(
                    takePictureIntent,
                    AppConstants.REQUEST_TAKE_PHOTO
                )
                return mCurrentPhotoPath
            }
            return ""
        }
        return ""
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
                        callPaymentGateway(PaymentGatewayType.INTER_PAY, transactionId)
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

    private fun setPaymentMethods(paymentTypes: List<PaymentTypeModel>) {
        binding.containerDynamicPaymentMethods.visibility = View.VISIBLE
        binding.containerStaticPaymentMethods.visibility = View.GONE

        val container = findViewById<LinearLayout>(R.id.container_dynamic_payment_methods)

        for (paymentType in paymentTypes) {
            val statusSelector = StatusSelector(this)
            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                statusSelector.setTextStatus(paymentType.arabicName)
            } else {
                statusSelector.setTextStatus(paymentType.name)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 12, 0, 0)
            statusSelector.layoutParams = layoutParams

            statusSelector.setOnClickListener {
                selectedPaymentType?.makeUnselected()
                statusSelector.makeSelected()
                selectedPaymentType = statusSelector
                paymentTypeId = paymentType.id
            }
            container.addView(statusSelector)
        }
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
                    if (selectedDeliveryType == DeliveryType.PARTIAL) {
                        note = binding.etPartialDeliveryNote.text.toString()
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
                            items
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
                                callPayMultiWay()
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
                        ApiAdapter.apiClient.getPaymentMethods()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            setPaymentMethods(response.body()!!.data!!)
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
                        ApiAdapter.apiClient.payMultiWay(
                            pkg?.id,
                            PayMultiWayRequestBody(
                                (selectedPaymentType?.enumValue as PaymentType).name,
                                paymentTypeId,
                                packageValueToPay
                            )
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
                                Helper.showSuccessMessage(
                                    super.getContext(),
                                    getString(R.string.success_operation_completed)
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
                } else if (companyConfigurations?.isEnableDeliveryVerificationPinCodeForPkgsWithCodGreaterThan != null && (pkg?.cod
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
        if (selectedPaymentType?.textView?.text == PaymentType.CLICK_PAY.englishLabel) {
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
            if (companyConfigurations?.isDriverProveDeliveryByScanBarcode!!) {
                val mIntent = Intent(
                    super.getContext(),
                    VerifyPackageDeliveryActivity::class.java
                )
                mIntent.putExtra("barcode", pkg?.barcode)
                mIntent.putExtra("invoice", pkg?.invoiceNumber)
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
            .paymentText(PaymentText("يرجى تمرير الطاقة", "please tap your card"))
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
                PaymentTypeValueDialog(super.getContext(), this, selectedPaymentType, paymentTypeId).showDialog()
            } else {
                if (selectedPaymentType?.textView?.text == PaymentType.INTER_PAY.englishLabel) {
                    if (isAppInstalled(packageManager, AppConstants.SOFTPOS_PACKAGE_NAME)) {
                        startSoftposApp(pkg?.cod?.format()!!)
                        return
                    } else {
                        Helper.showErrorMessage(
                            super.getContext(), getString(R.string.error_app_is_not_installed)
                        )
                    }
                } else if (selectedPaymentType?.textView?.text == PaymentType.CLICK_PAY.englishLabel) {
                    callVerifyClickPay()
                } else if (selectedPaymentType?.textView?.text == PaymentType.NEAR_PAY.englishLabel) {
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

        if (selectedPaymentType?.textView?.text == PaymentType.INTER_PAY.englishLabel) {
            if (isAppInstalled(packageManager, AppConstants.SOFTPOS_PACKAGE_NAME)) {
                startSoftposApp(packageCodToPay.format())
                return
            } else {
                Helper.showErrorMessage(
                    super.getContext(), getString(R.string.error_app_is_not_installed)
                )
            }
        } else if (selectedPaymentType?.textView?.text == PaymentType.NEAR_PAY.englishLabel) {
            startNearPay(packageCodToPay)
        } else {
            callPayMultiWay()
        }
    }
}