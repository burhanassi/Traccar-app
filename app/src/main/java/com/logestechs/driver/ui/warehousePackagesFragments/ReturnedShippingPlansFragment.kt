package com.logestechs.driver.ui.warehousePackagesFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.databinding.FragmentReturnedShippingPlansBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.DriverShippingPlanCellAdapter
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class ReturnedShippingPlansFragment : LogesTechsFragment() {

    private var _binding: FragmentReturnedShippingPlansBinding? = null
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
        val v: FragmentReturnedShippingPlansBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_returned_shipping_plans,
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
        binding.textTitle.text = getString(R.string.returned_shipping_plans)
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
                        status = ShippingPlanStatus.REJECTED_ITEMS.name
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
}
