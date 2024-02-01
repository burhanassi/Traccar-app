package com.logestechs.driver.ui.pickedFulfilmentOrdersActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.PrintPickListRequestBody
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ActivityPickedFulfilmentOrdersBinding
import com.logestechs.driver.ui.barcodeScanner.FulfilmentPackerBarcodeScannerActivity
import com.logestechs.driver.ui.barcodeScanner.FulfilmentPickerBarcodeScannerActivity
import com.logestechs.driver.ui.barcodeScanner.FulfilmentPickerScanMode
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.FulfilmentOrderStatus
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.NewFulfilmentOrderCellAdapter
import com.logestechs.driver.utils.adapters.PickedFulfilmentOrderCellAdapter
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.PickedFulfilmentOrderCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.TimeZone

class PickedFulfilmentOrdersActivity : LogesTechsActivity(), PickedFulfilmentOrderCardListener,
    View.OnClickListener {
    private lateinit var binding: ActivityPickedFulfilmentOrdersBinding

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    private var fulfilmentOrdersList: ArrayList<FulfilmentOrder?> = ArrayList()
    private var status: String? = FulfilmentOrderStatus.PICKED.name

    private var isOnCreateCalled = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickedFulfilmentOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tabLayout: TabLayout = findViewById(R.id.tab_layout_fulfillment)

        tabLayout.addTab(tabLayout.newTab().setText(R.string.picked_fulfillment))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.partially_picked_fulfillment))

        tabLayout.tabTextColors = getColorStateList(R.color.tab_text_color_selector)

        initRecycler()
        initListeners()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                 status = when (tab.position) {
                    0 ->{
                        FulfilmentOrderStatus.PICKED.name
                    }
                    1 ->{
                        FulfilmentOrderStatus.PARTIALLY_PICKED.name
                    }
                    else -> null
                }

                currentPageIndex = 1
                fulfilmentOrdersList.clear()
                if (status == FulfilmentOrderStatus.PICKED.name) {
                    binding.textTitle.text = getText(R.string.picked_fulfillment)
                } else {
                    binding.textTitle.text = getText(R.string.partially_picked_fulfillment)
                    binding.buttonScanTote.visibility = View.GONE
                }
                callGetFulfilmentOrders(status)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselection here if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselection here if needed
            }

        })


        tabLayout.getTabAt(0)?.select()
        callGetFulfilmentOrders(FulfilmentOrderStatus.PICKED.name)
        isOnCreateCalled = true
    }
    override fun onResume() {
        super.onResume()

        if (!isOnCreateCalled) {
            currentPageIndex = 1
            (binding.rvFulfilmentOrders.adapter as PickedFulfilmentOrderCellAdapter).clearList()
            callGetFulfilmentOrders(status)
        }
        isOnCreateCalled = false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_SCAN_BARCODE -> {
                callGetOrderByToteBarcode(data?.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name)!!)
            }

            else -> {}
        }
    }
    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvFulfilmentOrders.adapter = PickedFulfilmentOrderCellAdapter(
            fulfilmentOrdersList, super.getContext(), listener = this
        )
        binding.rvFulfilmentOrders.layoutManager = layoutManager
        binding.rvFulfilmentOrders.addOnScrollListener(recyclerViewOnScrollListener)
        (binding.rvFulfilmentOrders.adapter as PickedFulfilmentOrderCellAdapter).clearList()

    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            currentPageIndex = 1
            (binding.rvFulfilmentOrders.adapter as PickedFulfilmentOrderCellAdapter).clearList()
            callGetFulfilmentOrders(status)
        }

        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.toolbarMain.buttonBack.setOnClickListener(this)

        if (SharedPreferenceWrapper.getNotificationsCount() == "0") {
            binding.toolbarMain.notificationCount.visibility = View.GONE
        }
        binding.buttonScanTote.setOnClickListener(this)
    }

    private fun handleNoPackagesLabelVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.textNoPackagesFound.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.GONE
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


    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvFulfilmentOrders.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvFulfilmentOrders.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvFulfilmentOrders.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        callGetFulfilmentOrders(status)
                    }
                }
            }
        }

    //apis
    @SuppressLint("NotifyDataSetChanged")
    private fun callGetFulfilmentOrders(status: String?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                        val response = ApiAdapter.apiClient.getFulfilmentOrders(
                            page = currentPageIndex,
                            status = status
                        )
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                        }
                        if (response?.isSuccessful == true && response.body() != null) {
                            val body = response.body()
                            val totalRound: Int = (body?.totalRecordsNo
                                ?: 0) / (AppConstants.DEFAULT_PAGE_SIZE * currentPageIndex)
                            if (totalRound == 0) {
                                currentPageIndex = 1
                                isLastPage = true
                            } else {
                                currentPageIndex++
                                isLastPage = false
                            }
                            withContext(Dispatchers.Main) {
                                fulfilmentOrdersList.addAll(body?.data ?: ArrayList())
                                binding.rvFulfilmentOrders.adapter?.notifyDataSetChanged()
                                handleNoPackagesLabelVisibility(body?.data?.isEmpty() ?: true && fulfilmentOrdersList.isEmpty())
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
                                        super.getContext(), getString(R.string.error_general)
                                    )
                                }
                            }
                        }
                        isLoading = false
                } catch (e: Exception) {
                    isLoading = false
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

    private fun callPackFulfilmentOrder(index: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.packFulfilmentOrder(fulfilmentOrdersList[index]?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(), getString(R.string.success_operation_completed)
                            )
                            currentPageIndex = 1
                            (binding.rvFulfilmentOrders.adapter as PickedFulfilmentOrderCellAdapter).clearList()
                            callGetFulfilmentOrders(FulfilmentOrderStatus.PICKED.name)
                        }

                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), getString(R.string.error_general)
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

    private fun callPrintPickList(index: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.printPickList(
                            PrintPickListRequestBody(listOf(fulfilmentOrdersList[index]?.id ?: 0))
                        )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()!!
                        withContext(Dispatchers.Main) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data.url))
                            startActivity(browserIntent)
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), getString(R.string.error_general)
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

    private fun callGetOrderByToteBarcode(toteBarcode: String) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getOrderFromTote(toteBarcode)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            onPackFulfilmentOrder(response.body()!!)
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(), getString(R.string.error_general)
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
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_scan_tote -> {
                val scanBarcode = Intent(this, SingleScanBarcodeScanner::class.java)
                this.startActivityForResult(
                    scanBarcode,
                    AppConstants.REQUEST_SCAN_BARCODE
                )
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }
        }
    }

    override fun onPackFulfilmentOrder(fulfilmentOrder: FulfilmentOrder?) {
        if (fulfilmentOrder != null) {
            val intent = Intent(this, FulfilmentPackerBarcodeScannerActivity::class.java)

            intent.putExtra(IntentExtrasKeys.FULFILMENT_ORDER.name, fulfilmentOrder)
            startActivity(intent)
        }
    }

    override fun onContinuePickingClicked(fulfilmentOrder: FulfilmentOrder?) {
        if (fulfilmentOrder != null) {
            val intent = Intent(this, FulfilmentPickerBarcodeScannerActivity::class.java)

            intent.putExtra(IntentExtrasKeys.FULFILMENT_ORDER.name, fulfilmentOrder)
            intent.putExtra(
                IntentExtrasKeys.FULFILMENT_PICKER_SCAN_MODE.name,
                FulfilmentPickerScanMode.ITEM_INTO_TOTE
            )
            startActivity(intent)
        }
    }

    override fun onDirectPackFulfilmentOrder(index: Int) {
        callPackFulfilmentOrder(index)
    }

    override fun onPrintPickList(index: Int) {
        callPrintPickList(index)
    }
}