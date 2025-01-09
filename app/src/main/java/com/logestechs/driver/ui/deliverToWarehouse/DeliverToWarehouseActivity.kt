package com.logestechs.driver.ui.deliverToWarehouse

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ActivityDeliverToWarhouseBinding
import com.logestechs.driver.ui.barcodeScanner.FulfilmentReturnOrderBarcodeScannerActivity
import com.logestechs.driver.ui.barcodeScanner.FulfilmentReturnScanMode
import com.logestechs.driver.ui.deliverToWarehouse.deliverOrderToWarehouseScreen.OrderDeliverToWarehouseActivity
import com.logestechs.driver.utils.AdminPackageStatus
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.FulfillmentItemStatus
import com.logestechs.driver.utils.FulfilmentOrderStatus
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.DeliverToWarehouseCellAdapter
import com.logestechs.driver.utils.interfaces.DeliverToWarehouseCardListener
import com.logestechs.driver.utils.interfaces.ReturnedFulfilmentOrderCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DeliverToWarehouseActivity : LogesTechsActivity(), View.OnClickListener,
    ReturnedFulfilmentOrderCardListener, DeliverToWarehouseCardListener {
    private lateinit var binding: ActivityDeliverToWarhouseBinding

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    private var fulfilmentOrdersList: ArrayList<FulfilmentOrder?> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliverToWarhouseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        currentPageIndex = 1
        (binding.rvFulfilmentOrders.adapter as DeliverToWarehouseCellAdapter).clearList()
        binding.rvFulfilmentOrders.adapter?.notifyDataSetChanged()
        callGetFulfilmentOrders()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvFulfilmentOrders.adapter = DeliverToWarehouseCellAdapter(
            fulfilmentOrdersList, super.getContext(), listener = this
        )
        binding.rvFulfilmentOrders.layoutManager = layoutManager
        binding.rvFulfilmentOrders.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            currentPageIndex = 1
            (binding.rvFulfilmentOrders.adapter as DeliverToWarehouseCellAdapter).clearList()
            callGetFulfilmentOrders()
        }

        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.toolbarMain.buttonBack.setOnClickListener(this)

        if (SharedPreferenceWrapper.getNotificationsCount() == "0") {
            binding.toolbarMain.notificationCount.visibility = View.GONE
        }
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
                        callGetFulfilmentOrders()
                    }
                }
            }
        }

    //APIs
    @SuppressLint("NotifyDataSetChanged")
    private fun callGetFulfilmentOrders() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getFulfilmentOrders(
                        status = FulfilmentOrderStatus.PACKED.name,
                        pkgStatus = AdminPackageStatus.SCANNED_BY_HANDLER_AND_UNLOADED.name
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        val totalRound: Int =
                            (body?.totalRecordsNo
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
                                    super.getContext(),
                                    getString(R.string.error_general)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }
        }
    }

    override fun onReturnFulfilmentOrder(index: Int) {
        val mIntent = Intent(this, FulfilmentReturnOrderBarcodeScannerActivity::class.java)
        mIntent.putExtra(
            IntentExtrasKeys.FULFILMENT_RETURN_ORDER_SCAN_MODE.name,
            FulfilmentReturnScanMode.BIN
        )
        mIntent.putExtra(
            IntentExtrasKeys.FULFILMENT_ORDER.name,
            fulfilmentOrdersList[index]
        )
        startActivity(mIntent)
    }

    override fun onDeliverToWarehouse(index: Int) {
        val mIntent = Intent(this, OrderDeliverToWarehouseActivity::class.java)
        mIntent.putExtra(
            IntentExtrasKeys.FULFILMENT_ORDER.name,
            fulfilmentOrdersList[index]
        )
        startActivity(mIntent)
    }
}