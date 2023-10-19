package com.logestechs.driver.utils.bottomSheets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.BottomSheetPackageTrackBinding
import com.logestechs.driver.utils.AppFonts
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper

interface BottomSheetListener {
    fun onBottomSheetDismissed()
}

class PackageTrackBottomSheet(
) : BottomSheetDialogFragment(), View.OnClickListener {
    private var _binding: BottomSheetPackageTrackBinding? = null
    private val binding get() = _binding!!
    private var pkg: Package? = null

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var listener: BottomSheetListener? = null
    var isScan: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: BottomSheetPackageTrackBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_package_track,
            container,
            false
        )
        _binding = v
        binding.itemDetailComment.textItem.maxLines = 20

        pkg = arguments?.getParcelable(BundleKeys.PKG_KEY.toString())
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isScan) {
            binding.containerTitle.visibility = View.VISIBLE
            binding.textTitle.text = getString(R.string.swipe_down_to_scan_another_package)
        }

        binding.locationBubble.textBubbleLocation.text = pkg?.destinationAddress?.toStringAddress()

        binding.itemDetailShipmentId.textItem.typeface =
            Helper.getFontStyle(requireContext(), AppFonts.ROBOTO_MEDIUM)
        binding.itemDetailShipmentId.textItem.text = pkg?.barcode
        if (binding.itemDetailShipmentId.textItem.length() >= 15) {
            binding.itemDetailShipmentId.textItem.textSize = 12F
        }

        if (pkg?.senderMiddleName != null) {
            binding.itemDetailSenderName.textItem.text =
                "${pkg?.senderFirstName} ${pkg?.senderMiddleName} ${pkg?.senderLastName}"
        } else {
            binding.itemDetailSenderName.textItem.text =
                "${pkg?.senderFirstName} ${pkg?.senderLastName}"
        }

        if (pkg?.receiverMiddleName != null) {
            binding.itemDetailReceiverName.textItem.text =
                "${pkg?.receiverFirstName} ${pkg?.receiverMiddleName} ${pkg?.receiverLastName}"
        } else {
            binding.itemDetailReceiverName.textItem.text =
                "${pkg?.receiverFirstName} ${pkg?.receiverLastName}"
        }

        binding.itemDetailReceiverPhone.textItem.text = pkg?.receiverPhone
        binding.itemDetailReceiverPhone.buttonCopy.setOnClickListener {
            Helper.copyTextToClipboard(requireActivity(), pkg?.receiverPhone!!)
        }

        binding.itemDetailCreatedDate.textItem.text = pkg?.createdDate

        binding.itemDetailCreatedDate.textItem.typeface =
            Helper.getFontStyle(requireContext(), AppFonts.ROBOTO_MEDIUM)
        binding.itemDetailCreatedDate.textItem.text =
            Helper.formatServerDate(pkg?.createdDate, DateFormats.DEFAULT_FORMAT)

        if (pkg?.postponedDeliveryDate?.isNotEmpty() == true) {
            binding.itemDetailPostponedDate.root.visibility = View.VISIBLE
            binding.itemDetailPostponedDate.textItem.typeface =
                Helper.getFontStyle(requireContext(), AppFonts.ROBOTO_MEDIUM)
            binding.itemDetailPostponedDate.textItem.text =
                Helper.formatServerDate(pkg?.postponedDeliveryDate, DateFormats.DEFAULT_FORMAT)
        } else {
            binding.itemDetailPostponedDate.root.visibility = View.GONE
        }

        binding.itemDetailMoney.textItem.typeface =
            Helper.getFontStyle(requireContext(), AppFonts.ROBOTO_MEDIUM)
        binding.itemDetailMoney.textItem.text =
            "${pkg?.cost}"

        binding.itemDetailCod.textItem.typeface =
            Helper.getFontStyle(requireContext(), AppFonts.ROBOTO_MEDIUM)
        binding.itemDetailCod.textItem.text = "${pkg?.cod}"

        if (pkg?.notes != null && pkg?.notes!!.isNotEmpty()) {
            binding.itemDetailComment.textItem.text = pkg?.notes
        } else {
            binding.itemDetailComment.root.visibility = View.GONE
        }

        if (pkg?.status != null && pkg?.status!!.isNotEmpty()) {
            binding.itemPackageStatus.textItem.text = pkg?.status
        } else {
            binding.itemPackageStatus.root.visibility = View.GONE
        }

        binding.itemDetailShipmentId.buttonCopy.setOnClickListener {
            Helper.copyTextToClipboard(requireActivity(), pkg?.barcode!!)
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onBottomSheetDismissed()
    }

    fun setListener(listener: BottomSheetListener) {
        this.listener = listener
        isScan = true
    }

    //    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener { dialog ->
//            val dialog = dialog as BottomSheetDialog
//            val bottomSheet =
//                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            BottomSheetBehavior.from(bottomSheet).state =
//                BottomSheetBehavior.STATE_EXPANDED
//            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
//            BottomSheetBehavior.from(bottomSheet).isHideable = true
//        }
//        return bottomSheetDialog
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {

    }
}