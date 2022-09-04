package com.logestechs.driver.ui.packageDelivery

import android.content.Intent
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.DeliverPackageRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityPackageDeliveryBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.customViews.StatusSelector
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


class PackageDeliveryActivity : LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPackageDeliveryBinding

    private var path: String? = null
    private var file: File? = null
    private var bitmap: Bitmap? = null
    var gestureTouch = false
    private var pkg: Package? = null

    var paymentTypeButtonsList: ArrayList<StatusSelector> = ArrayList()
    var selectedPaymentType: StatusSelector? = null

    var selectedDeliveryType: DeliveryType? = null

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
        path = Environment.getExternalStorageDirectory().toString() + "/signature.png"
        file = File(path)
        file?.delete()

        binding.gestureViewSignature.isHapticFeedbackEnabled = false
        binding.gestureViewSignature.cancelLongPress()
        binding.gestureViewSignature.cancelClearAnimation()
    }

    private fun isSignatureEntered(): Boolean {
        return binding.gestureViewSignature.gesture != null && binding.gestureViewSignature.gesture.length > 0
    }

    private fun initData() {
        binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
        binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()
        binding.itemPackageBarcode.textItem.text = pkg?.barcode
        binding.textCod.text = pkg?.cod?.format()

        if (true) {
            selectedDeliveryType = DeliveryType.FULL
        }
    }

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
                binding.etPartialDeliveryNote.setText("")
                selectedDeliveryType = DeliveryType.FULL
            } else if (checkedId == R.id.radio_button_partial_delivery) {
                binding.containerPartialDeliveryNote.visibility = View.VISIBLE
                selectedDeliveryType = DeliveryType.PARTIAL
            }
        }

        binding.buttonClearSignature.setOnClickListener(this)
        binding.buttonDeliverPackage.setOnClickListener(this)
        binding.buttonAddPod.setOnClickListener(this)
        binding.selectorCash.setOnClickListener(this)
        binding.selectorDigitalWallet.setOnClickListener(this)
        binding.selectorCheque.setOnClickListener(this)
        binding.selectorPrepaid.setOnClickListener(this)
        binding.selectorCardPayment.setOnClickListener(this)
        binding.selectorBankTransfer.setOnClickListener(this)
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            pkg = extras.getParcelable(IntentExtrasKeys.PACKAGE_TO_DELIVER.name)
        }
    }

    private fun initPaymentMethodsControls() {
        binding.selectorCash.makeSelected()
        selectedPaymentType = binding.selectorCash
        binding.selectorCash.enumValue = PaymentType.CASH
        binding.selectorDigitalWallet.enumValue = PaymentType.DIGITAL_WALLET
        binding.selectorCheque.enumValue = PaymentType.CHEQUE
        binding.selectorPrepaid.enumValue = PaymentType.PREPAID
        binding.selectorCardPayment.enumValue = PaymentType.CARD_PAYMENT
        binding.selectorBankTransfer.enumValue = PaymentType.BANK_TRANSFER
    }

    private fun fillButtonsList() {
        paymentTypeButtonsList.add(binding.selectorCash)
        paymentTypeButtonsList.add(binding.selectorDigitalWallet)
        paymentTypeButtonsList.add(binding.selectorCheque)
        paymentTypeButtonsList.add(binding.selectorPrepaid)
        paymentTypeButtonsList.add(binding.selectorCardPayment)
        paymentTypeButtonsList.add(binding.selectorBankTransfer)
    }

    private fun unselectAllPaymentMethods() {
        for (item in paymentTypeButtonsList) {
            item.makeUnselected()
        }
    }

    private fun validateInput(): Boolean {
        if (selectedDeliveryType == DeliveryType.PARTIAL) {
            if (binding.etPartialDeliveryNote.text.toString().isEmpty()) {
                Helper.showErrorMessage(
                    super.getContext(),
                    getString(R.string.error_enter_partial_delivery_note)
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
                            callDeliverPackage(
                                DeliverPackageRequestBody(
                                    pkg?.id,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    response.body()?.fileUrl,
                                    null,
                                    null,
                                    null,
                                    (selectedPaymentType?.enumValue as PaymentType).name
                                )
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

    private fun callDeliverPackage(deliverPackageRequestBody: DeliverPackageRequestBody?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
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
                        body = deliverPackageRequestBody
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
            R.id.button_clear_signature -> {
                binding.gestureViewSignature.invalidate()
                binding.gestureViewSignature.clear(true)
                binding.gestureViewSignature.clearAnimation()
                binding.gestureViewSignature.cancelClearAnimation()
                gestureTouch = false
            }

            R.id.button_deliver_package -> {
                if (isSignatureEntered()) {
                    if (validateInput()) {
                        uploadPackageSignature()
                    }
                } else {
                    Helper.showErrorMessage(this, "signature not enetered")
                }
            }

            R.id.button_add_pod -> {
                Helper.showSuccessMessage(this, "added")
            }

            R.id.selector_cash -> {
                unselectAllPaymentMethods()
                binding.selectorCash.makeSelected()
                selectedPaymentType = binding.selectorCash
            }
            R.id.selector_digital_wallet -> {
                unselectAllPaymentMethods()
                binding.selectorDigitalWallet.makeSelected()
                selectedPaymentType = binding.selectorDigitalWallet
            }
            R.id.selector_cheque -> {
                unselectAllPaymentMethods()
                binding.selectorCheque.makeSelected()
                selectedPaymentType = binding.selectorCheque
            }
            R.id.selector_prepaid -> {
                unselectAllPaymentMethods()
                binding.selectorPrepaid.makeSelected()
                selectedPaymentType = binding.selectorPrepaid
            }
            R.id.selector_card_payment -> {
                unselectAllPaymentMethods()
                binding.selectorCardPayment.makeSelected()
                selectedPaymentType = binding.selectorCardPayment
            }
            R.id.selector_bank_transfer -> {
                unselectAllPaymentMethods()
                binding.selectorBankTransfer.makeSelected()
                selectedPaymentType = binding.selectorBankTransfer
            }
        }
    }
}