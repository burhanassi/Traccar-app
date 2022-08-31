package com.logestechs.driver.ui.packageDelivery

import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityPackageDeliveryBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PaymentType
import com.logestechs.driver.utils.customViews.StatusSelector
import java.io.File

class PackageDeliveryActivity : LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPackageDeliveryBinding

    private var path: String? = null
    private var file: File? = null
    private val bitmap: Bitmap? = null
    var gestureTouch = false
    private var pkg: Package? = null

    var paymentTypeButtonsList: ArrayList<StatusSelector> = ArrayList()
    var selectedPaymentType: StatusSelector? = null

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

//        binding.gestureViewSignature.isDrawingCacheEnabled = true
//
//        binding.gestureViewSignature.isAlwaysDrawnWithCacheEnabled = true
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
                    Helper.showSuccessMessage(
                        this,
                        (selectedPaymentType?.enumValue as PaymentType).arabicLabel
                    )
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