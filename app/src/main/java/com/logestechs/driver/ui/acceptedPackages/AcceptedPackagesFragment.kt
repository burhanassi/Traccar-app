package com.logestechs.driver.ui.acceptedPackages

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.logestechs.driver.utils.adapters.AcceptedPackageVillageCellAdapter
import com.logestechs.driver.utils.bottomSheets.AcceptedPackagesBottomSheet
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors

class AcceptedPackagesFragment(
    tscPrinterActivity: EnhancedTSCPrinterActivity
) : LogesTechsFragment(), AcceptedPackagesCardListener {

    private var _binding: FragmentAcceptedPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null


    private lateinit var parentActivity: AppCompatActivity
    private lateinit var viewModel: RefreshViewModel

    private val tscDll = tscPrinterActivity
    private var fileName: String? = null

    val executorService = Executors.newCachedThreadPool()

    private var url: URL? = null
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
        viewModel = ViewModelProvider(requireActivity()).get(RefreshViewModel::class.java)
        viewModel.dataRefresh.observe(viewLifecycleOwner, Observer { refresh ->
            if (refresh) {
                initRecycler()
                initListeners()
                callGetAcceptedPackages()
                activityDelegate = activity as ViewPagerCountValuesDelegate
                binding.textTitle.text = getString(R.string.packages_view_pager_accepted_packages)
            }
        })
        initRecycler()
        initListeners()
        callGetAcceptedPackages()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        binding.textTitle.text = getString(R.string.packages_view_pager_accepted_packages)
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
            this
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
                    val response = ApiAdapter.apiClient.getAcceptedPackagesByCustomer(customerId = customer?.id)
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

    private fun callPrintAwb() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
//                    val response = ApiAdapter.apiClient.printPackageAwb(packageId, isImage)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
//                    if (response?.isSuccessful == true && response.body() != null) {
//                        val pdfData = "https://elasticbeanstalk-eu-central-1-640066223797.s3-accelerate.amazonaws.com/1/Logestechs_1699456038684_report.pdf"
//
//                        downloadPdfFromUrl(pdfData)
//
//                        val request = DownloadManager.Request(Uri.parse(pdfData.toString() + ""))
//                        request.setTitle(fileName)
//                        request.setMimeType(Helper.Companion.PrinterConst.PDF_EXTENSION)
//                        request.allowScanningByMediaScanner()
//                        request.setAllowedOverMetered(true)
//                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                        request.setDestinationInExternalPublicDir(
//                            Environment.DIRECTORY_DOWNLOADS,
//                            fileName
//                        )
//                        val dm =
//                            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                        dm.enqueue(request)
//
//                        val file = File(
//                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                                .toString() + "/" + fileName
//                        )
//                        tscDll.printOnTscPrinter(context, file)
                    // Prepare the image (this is just a placeholder, you should provide the actual image)
                    val REQUEST_BLUETOOTH_PERMISSION = 1

                    val permissions = arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT// Add this permission
                    )

                    if (permissions.all {
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        // All permissions are granted, proceed with Bluetooth operations.
                        tscDll.openport(Helper.Companion.PrinterConst.PRINTER_BLUETOOTH_ADDRESS)
                        val imageBitmap: Bitmap = Bitmap.createBitmap(
                            700,
                            990,
                            Bitmap.Config.ARGB_8888
                        ) // Load your image here

                        val publicExternalStorageDir =
                            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        val tempImageFile = File(publicExternalStorageDir, "my_image.png")

                        try {
                            FileOutputStream(tempImageFile).use { outputStream ->
                                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        tscDll.sendfile("my_image.png")

                        val buffer = "test Test".toByteArray()
                        val PrintHeader = byteArrayOf(0xAA.toByte(), 0x55, 2, 0)
                        PrintHeader[3] = buffer.size.toByte()
                        tscDll.sendlargebyte(buffer)

                        tempImageFile.delete()
                    } else {
                        // Request permissions
                        requestPermissions(permissions, REQUEST_BLUETOOTH_PERMISSION)
                    }


//                    } else {
//                        try {
//                            val jObjError = JSONObject(response?.errorBody()!!.string())
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    jObjError.optString(AppConstants.ERROR_KEY)
//                                )
//                            }
//
//                        } catch (e: java.lang.Exception) {
//                            withContext(Dispatchers.Main) {
//                                Helper.showErrorMessage(
//                                    super.getContext(),
//                                    getString(R.string.error_general)
//                                )
//                            }
//                        }
//                    }
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

    private fun downloadPdfFromUrl(path: String) {
        executorService.execute {
            try {
                url = URL(path)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            fileName = url?.getPath()
            fileName = fileName?.substring(fileName!!.lastIndexOf('/') + 1)
            try {
                executeDownloadPdf()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun executeDownloadPdf() {
        val request = DownloadManager.Request(Uri.parse(url.toString() + ""))
        request.setTitle(fileName)
        request.setMimeType(Helper.Companion.PrinterConst.PDF_EXTENSION)
        request.allowScanningByMediaScanner()
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val dm = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/" + fileName
        )
        if (tscDll != null) tscDll.printOnTscPrinter(context, file)
    }

    override fun scanForPickup(customer: Customer?) {
        val mIntent = Intent(super.getContext(), BarcodeScannerActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_FOR_PICKUP.name, customer)
        startActivity(mIntent)
    }

    override fun getAcceptedPackages(customer: Customer?) {
        callGetAcceptedPackagesByCustomer(customer)
    }

    override fun printAwb() {
        callPrintAwb()
    }
}