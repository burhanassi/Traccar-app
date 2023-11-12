package com.logestechs.driver.ui.acceptedPackages

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
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
                        Manifest.permission.BLUETOOTH_CONNECT // Add this permission
                    )

                    if (permissions.all {
                            ContextCompat.checkSelfPermission(
                                requireContext(),
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        // All permissions are granted, proceed with Bluetooth operations.
                        tscDll.openport(Helper.Companion.PrinterConst.PRINTER_BLUETOOTH_ADDRESS)
//                        val imageBitmap: Bitmap = Bitmap.createBitmap(
//                            700,
//                            990,
//                            Bitmap.Config.ARGB_8888
//                        ) // Load your image here
//
//                        val publicExternalStorageDir =
//                            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//                        val tempImageFile = File(publicExternalStorageDir, "my_image.png")
//
//                        try {
//                            FileOutputStream(tempImageFile).use { outputStream ->
//                                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                            }
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//
//                        tscDll.sendfile("my_image.png")
//
//                        val buffer = "test Test".toByteArray()
//                        val PrintHeader = byteArrayOf(0xAA.toByte(), 0x55, 2, 0)
//                        PrintHeader[3] = buffer.size.toByte()
//                        tscDll.sendlargebyte(buffer)
//
//                        tempImageFile.delete()
                        tscDll.sendcommand("SIZE 3,1\r\n");
                        tscDll.sendcommand("GAP 0,0\r\n");
                        tscDll.sendcommand("CLS\r\n");
                        tscDll.sendcommand("TEXT 100,100,\"3\",0,1,1,\"TEST TEST TEST!!!!\"\r\n");

                        val downloadsPath =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val imagePath = File(
                            downloadsPath,
                            "my_image.png"
                        ) // Replace with the actual path to your image

                        if (imagePath.exists()) {
//                        val options = BitmapFactory.Options().apply {
//                            inPreferredConfig = Bitmap.Config.ARGB_8888
//                        }

//                            val inputStream = FileInputStream(imagePath)
//
//                            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
//                            inputStream.close()
//                            val bitmap = Bitmap.createBitmap(300,400, Bitmap.Config.ARGB_8888)
//                            val canvas = Canvas(bitmap)
//                            canvas.drawColor(Color.BLACK)
//
//                            val paint = Paint().apply {
//                                color = Color.WHITE
//                                textSize = 30f
//                                textAlign = Paint.Align.CENTER
//                            }
//
//                            val x = 100 / 2f
//                            val y = 200 / 2f
//
//                            canvas.drawText("TEST", x, y, paint)

//                            val options = BitmapFactory.Options()
//                            options.inSampleSize = 2 // Adjust this value based on your needs
//                            val myLogo = BitmapFactory.decodeResource(resources, R.drawable.ic_logestechs_logo, options)
//
//
//                            val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.)
//                            val width = drawable?.intrinsicWidth ?: 0
//                            val height = drawable?.intrinsicHeight ?: 0
//
//                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                            val canvas = Canvas(bitmap)
//                            drawable?.setBounds(0, 0, canvas.width, canvas.height)
//                            drawable?.draw(canvas)

//                            if (bitmap != null) {
//                            // Perform operations with the loaded bitmap
//                            tscDll.setup(100, 150, 4, 4, 0, 0, 0)
//                            tscDll.clearbuffer()
//
//                            // Send the bitmap to the printer
//                            // Note: You might need to adapt this part based on the capabilities of your `tscDll` library.
//                            tscDll.sendbitmap(0, 20, bitmap)
//
//                            tscDll.sendcommand("\r\nPRINT 1\r\n")
//                            tscDll.closeport(5000)

                            fileName = "my_image.png"
                            val file = File(
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                ).toString() + "/" + fileName
                            )
                            val filePathString =
                                "Logestechs_1699456038684_report.pdf"
                            val pdfFile = File(filePathString)
                            val bitmaps = ArrayList<Bitmap>()

                            try {
                                println("Current working directory: ${System.getProperty("user.dir")}")

                                if (!file.exists()) {
                                    throw FileNotFoundException("File not found: $filePathString")
                                }

//                                val contentResolver = requireActivity().contentResolver
//                                val parcelFileDescriptor: ParcelFileDescriptor? =
//                                    contentResolver.openFileDescriptor(file.toUri(), "r")

                                val imageUrl =
                                    "https://www.google.com/url?sa=i&url=https%3A%2F%2Fpixabay.com%2Fvectors%2Flink-url-icon-chains-web-internet-1271843%2F&psig=AOvVaw3CCiV_wkxi1rjDB3lcKP3D&ust=1699881370622000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCJCj293FvoIDFQAAAAAdAAAAABAE" // Replace with your actual image URL

                                val client = OkHttpClient()
                                val request = Request.Builder().url(imageUrl).build()

                                val response = client.newCall(request).execute()

                                if (response.isSuccessful) {
                                    // Step 2: Convert the Downloaded Image to a Bitmap
                                    val downloadsDirectory =
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    val filePath =
                                        "my_image.png" // Replace with the actual file name
                                    val imagePath = File(downloadsDirectory, filePath)
                                    val drawable: Drawable? =
                                        ContextCompat.getDrawable(requireContext(), R.drawable.test)
                                    val originalWidth = drawable?.intrinsicWidth ?: 0
                                    val originalHeight = drawable?.intrinsicHeight ?: 0

                                    val width = ((originalWidth * 72).toDouble() / 25.4).toInt()
                                    val height = ((originalHeight * 72).toDouble() / 25.4).toInt()
                                    val resize_width = 800 // Change this to your desired width
                                    val resize_height = (resize_width.toDouble() / width) * height

                                    val bitmap = Bitmap.createBitmap(
                                        resize_width,
                                        resize_height.toInt(),
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = Canvas(bitmap)
                                    drawable?.setBounds(0, 0, canvas.width, canvas.height)
                                    drawable?.draw(canvas)

                                    // Step 3: Print the Bitmap using TSCDLL
                                    if (bitmap != null) {
                                        tscDll.setup(100, 150, 4, 4, 0, 0, 0)
                                        tscDll.clearbuffer()

                                        // Send the bitmap to the printer
                                        // Note: You might need to adapt this part based on the capabilities of your `tscDll` library.
                                        tscDll.sendbitmap(0, 0, bitmap)
//                                        tscDll.sendcommand("SIZE $resize_width dot, $resize_height dot\r\nCLS\r\n")
                                        tscDll.sendcommand("\r\nPRINT 1\r\n")
                                        tscDll.closeport(5000)
                                    }
                                }
                            } catch (var34: Exception) {
                                var34.printStackTrace()
                                bitmaps
                            }
                        }

//                        tscDll.sendcommand("BITMAP 100,200,0,\"$imagePath\"\r\n")

//                        tscDll.sendcommand("\r\nPRINT 1\r\n")
//                        tscDll.closeport(5000)
//                        }
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