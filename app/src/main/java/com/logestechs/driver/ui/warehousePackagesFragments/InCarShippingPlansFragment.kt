package com.logestechs.driver.ui.warehousePackagesFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.databinding.FragmentInCarShippingPlansBinding
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.DriverShippingPlanCellAdapter
import com.logestechs.driver.utils.dialogs.SearchPackagesDialog
import com.logestechs.driver.utils.interfaces.SearchPackagesDialogListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class InCarShippingPlansFragment : LogesTechsFragment(), View.OnClickListener,
    SearchPackagesDialogListener {

    private var _binding: FragmentInCarShippingPlansBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    private var shippingPlansList: ArrayList<ShippingPlan?> = ArrayList()

    private var searchWord: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentInCarShippingPlansBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_in_car_shipping_plans,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        binding.textTitle.text = getString(R.string.in_car_shipping_plans)
        binding.buttonSearch.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            currentPageIndex = 1
            (binding.rvShippingPlans.adapter as DriverShippingPlanCellAdapter).clearList()
            callGetShippingPlans()
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvShippingPlans.adapter = DriverShippingPlanCellAdapter(
            shippingPlansList,
            null
        )
        binding.rvShippingPlans.layoutManager = layoutManager
        binding.rvShippingPlans.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initListeners() {
        binding.refreshLayout.setOnRefreshListener {
            searchWord = null
            currentPageIndex = 1
            (binding.rvShippingPlans.adapter as DriverShippingPlanCellAdapter).clearList()
            callGetShippingPlans()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_SCAN_BARCODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    searchWord = data.getStringExtra(IntentExtrasKeys.SCANNED_BARCODE.name)
                } else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                    SearchPackagesDialog(requireContext(), this, searchWord).showDialog()
                }
            }
            else -> {}
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
            binding.refreshLayout.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }


    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvShippingPlans.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvShippingPlans.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvShippingPlans.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        callGetShippingPlans()
                    }
                }
            }
        }


    //APIs
    @SuppressLint("NotifyDataSetChanged")
    private fun callGetShippingPlans() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getShippingPlansForDriver(
                        page = currentPageIndex,
                        status = ShippingPlanStatus.PICKED_UP.name,
                        search = searchWord
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
                            shippingPlansList.addAll(body?.data ?: ArrayList())
                            binding.rvShippingPlans.adapter?.notifyDataSetChanged()
                            handleNoPackagesLabelVisibility(body?.data?.isEmpty() ?: true && shippingPlansList.isEmpty())
                            activityDelegate?.updateCountValues()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_search -> {
                SearchPackagesDialog(requireContext(), this, searchWord).showDialog()
            }
        }
    }

    override fun onPackageSearch(keyword: String?) {
        searchWord = keyword
        currentPageIndex = 1
        (binding.rvShippingPlans.adapter as DriverShippingPlanCellAdapter).clearList()
        callGetShippingPlans()
    }

    override fun onStartBarcodeScan() {
        val scanBarcode = Intent(context, SingleScanBarcodeScanner::class.java)
        this.startActivityForResult(
            scanBarcode,
            AppConstants.REQUEST_SCAN_BARCODE
        )
    }
}
