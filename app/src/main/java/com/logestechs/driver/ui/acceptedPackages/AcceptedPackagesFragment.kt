package com.logestechs.driver.ui.acceptedPackages

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tscdll.TSCActivity
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Village
import com.logestechs.driver.databinding.FragmentAcceptedPackagesBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.EnhancedTSCPrinterActivity
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.RefreshViewModel
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.AcceptedPackageVillageCellAdapter
import com.logestechs.driver.utils.bottomSheets.AcceptedPackagesBottomSheet
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AcceptedPackagesFragment(
    tscPrinterActivity: EnhancedTSCPrinterActivity
) : LogesTechsFragment(), AcceptedPackagesCardListener {

    private var _binding: FragmentAcceptedPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null


    private lateinit var parentActivity: AppCompatActivity
    private lateinit var viewModel: RefreshViewModel

    private val tscDll = TSCActivity()
    private var fileName: String? = null

    val executorService = Executors.newCachedThreadPool()

    private var url: URL? = null

    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    var isSprint: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentAcceptedPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accepted_packages,
            container,
            false
        )
        _binding = v
        return v.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppCompatActivity) {
            parentActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (loginResponse?.user?.companyID == 240.toLong() || loginResponse?.user?.companyID == 313.toLong()) {
            isSprint = true
        }
        viewModel = ViewModelProvider(requireActivity()).get(RefreshViewModel::class.java)
        viewModel.dataRefresh.observe(viewLifecycleOwner, Observer { refresh ->
            if (refresh) {
                initRecycler()
                initListeners()
                callGetAcceptedPackages()
                activityDelegate = activity as ViewPagerCountValuesDelegate
                if (isSprint) {
                    binding.textTitle.text =
                        getString(R.string.packages_view_pager_accepted_packages_sprint)
                } else {
                    binding.textTitle.text =
                        getString(R.string.packages_view_pager_accepted_packages)
                }
            }
        })
        initRecycler()
        initListeners()
        callGetAcceptedPackages()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        if (isSprint) {
            binding.textTitle.text =
                getString(R.string.packages_view_pager_accepted_packages_sprint)
        } else {
            binding.textTitle.text = getString(R.string.packages_view_pager_accepted_packages)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            callGetAcceptedPackages()
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvVillages.adapter = AcceptedPackageVillageCellAdapter(
            ArrayList(),
            super.getContext(),
            requireFragmentManager(),
            this,
            isSprint
        )
        binding.rvVillages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            callGetAcceptedPackages()
        }
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvVillages.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvVillages.visibility = View.GONE
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

    //APIs
    private fun callGetAcceptedPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getAcceptedPackages()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvVillages.adapter as AcceptedPackageVillageCellAdapter).update(
                                body?.villages as ArrayList<Village?>
                            )
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body.villages?.size ?: 0)
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

    private fun callGetAcceptedPackagesByCustomer(customer: Customer?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getAcceptedPackagesByCustomer(customerId = customer?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            val bottomSheet = AcceptedPackagesBottomSheet()
                            val bundle = Bundle()
                            val packageList = ArrayList<Parcelable>(response.body() ?: emptyList())
                            bundle.putParcelableArrayList(
                                BundleKeys.PACKAGES_KEY.toString(),
                                packageList
                            )

                            bundle.putInt(
                                BundleKeys.PACKAGES_COUNT.toString(),
                                response.body()!!.size
                            )
                            bottomSheet.arguments = bundle
                            bottomSheet.show(requireFragmentManager(), "exampleBottomSheet")
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

    private fun callPrintAwb(customer: Customer?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val timezone = TimeZone.getDefault().id.toString()

                    val selectedDevice = chooseBluetoothDevice()

                    if (selectedDevice != null) {
                        val response = ApiAdapter.apiClient.printPackageAwb(
                            customer?.id!!,
                            timezone,
                            true
                        )
                        hideWaitDialog()

                        if (response?.isSuccessful == true && response.body() != null) {
                            val imageUrl = response?.body()!!.url

                            connectAndPrintOnTSC(imageUrl!!, selectedDevice)
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
                    } else {
                        hideWaitDialog()
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

    @SuppressLint("MissingPermission")
    private suspend fun chooseBluetoothDevice(): BluetoothDevice? {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter?.bondedDevices

        return withContext(Dispatchers.Main) {
            val deviceList = pairedDevices?.map { it.name }?.toTypedArray()
            val selectedDevice = async(Dispatchers.Main) {
                val result = suspendCoroutine<BluetoothDevice?> { continuation ->
                    AlertDialog.Builder(super.getContext())
                        .setTitle("Choose Bluetooth Device")
                        .setItems(deviceList) { dialog, which ->
                            val selectedDevice = pairedDevices?.elementAt(which)
                            continuation.resume(selectedDevice)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            continuation.resume(null)
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                result
            }
            selectedDevice.await()
        }
    }
    private fun connectAndPrintOnTSC(imageUrl: String, selectedDevice: BluetoothDevice) {
        val REQUEST_BLUETOOTH_PERMISSION = 1

        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            tscDll.openport(selectedDevice.address)

            // Use Glide to load the image from the URL
            Glide.with(requireContext())
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        // Bitmap is ready, perform your resizing and printing logic
                        val drawable = BitmapDrawable(resources, resource)

                        val originalWidth = drawable.intrinsicWidth
                        val originalHeight = drawable.intrinsicHeight

                        val width = ((originalWidth * 72).toDouble() / 25.4).toInt()
                        val height = ((originalHeight * 72).toDouble() / 25.4).toInt()

                        val resize_width = 800 // Change this to your desired width
                        val resize_height = (resize_width.toDouble() / width) * height

                        val resizedBitmap = Bitmap.createBitmap(
                            resize_width,
                            resize_height.toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(resizedBitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)

                        var desiredPercentageHeight = 0.041
                        var desiredPercentageTime = 0.9

                        var newHeight = originalHeight * desiredPercentageHeight
                        var newTimeOut = originalHeight * desiredPercentageTime
                        if (resizedBitmap != null) {
                            tscDll.setup(100, newHeight.toInt(), 4, 4, 0, 0, 0)
                            tscDll.clearbuffer()
                            tscDll.sendbitmap(0, 0, resizedBitmap)
                            tscDll.sendcommand("\r\nPRINT 1\r\n")
                            tscDll.closeport(newTimeOut.toInt())
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle case where the Bitmap couldn't be loaded
                    }
                })
        } else {
            requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSION)
        }
    }


    override fun scanForPickup(customer: Customer?) {
        val mIntent = Intent(super.getContext(), BarcodeScannerActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_FOR_PICKUP.name, customer)
        startActivity(mIntent)
    }

    override fun getAcceptedPackages(customer: Customer?) {
        callGetAcceptedPackagesByCustomer(customer)
    }

    override fun printAwb(customer: Customer?) {
        callPrintAwb(customer)
    }
}