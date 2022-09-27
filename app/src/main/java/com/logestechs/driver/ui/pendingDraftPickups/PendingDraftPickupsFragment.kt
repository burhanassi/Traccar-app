package com.logestechs.driver.ui.pendingDraftPickups

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
import com.logestechs.driver.data.model.DraftPickup
import com.logestechs.driver.databinding.FragmentPendingDraftPickupsBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.PendingDraftPickupCellAdapter
import com.logestechs.driver.utils.interfaces.DriverDraftPickupsByStatusViewPagerActivityDelegate
import com.logestechs.driver.utils.interfaces.PendingDraftPickupCardListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PendingDraftPickupsFragment : LogesTechsFragment(), PendingDraftPickupCardListener {
    private var _binding: FragmentPendingDraftPickupsBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: DriverDraftPickupsByStatusViewPagerActivityDelegate? = null

    private var draftPickupsList: ArrayList<DraftPickup?> = ArrayList()

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentPendingDraftPickupsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pending_draft_pickups,
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
        activityDelegate = activity as DriverDraftPickupsByStatusViewPagerActivityDelegate
        binding.textTitle.text = getString(R.string.packages_view_pager_pending_packages)
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            currentPageIndex = 1
            (binding.rvDraftPickups.adapter as PendingDraftPickupCellAdapter).clearList()
            callGetPendingDraftPickups()
            activityDelegate?.updateCountValues()
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            context
        )
        binding.rvDraftPickups.adapter = PendingDraftPickupCellAdapter(
            draftPickupsList, super.getContext(), listener = this
        )
        binding.rvDraftPickups.layoutManager = layoutManager
        binding.rvDraftPickups.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            currentPageIndex = 1
            (binding.rvDraftPickups.adapter as PendingDraftPickupCellAdapter).clearList()
            callGetPendingDraftPickups()
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
                val visibleItemCount: Int = binding.rvDraftPickups.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvDraftPickups.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvDraftPickups.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        callGetPendingDraftPickups()
                    }
                }
            }
        }

    private fun handlePaging(totalRecordsNumber: Int) {
        val totalRound: Int =
            totalRecordsNumber / (AppConstants.DEFAULT_PAGE_SIZE * currentPageIndex)
        if (totalRound == 0) {
            currentPageIndex = 1
            isLastPage = true
        } else {
            currentPageIndex++
            isLastPage = false
        }
    }

    //apis
    @SuppressLint("NotifyDataSetChanged")
    private fun callGetPendingDraftPickups() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDriverDraftPickups(
                        DraftPickupStatus.ASSIGNED_TO_DRIVER.name,
                        page = currentPageIndex
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }

                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        handlePaging(body?.totalRecordsNo ?: 0)
                        withContext(Dispatchers.Main) {
                            draftPickupsList.addAll(body?.data ?: ArrayList())
                            binding.rvDraftPickups.adapter?.notifyDataSetChanged()
                            handleNoPackagesLabelVisibility(body?.data?.isEmpty() ?: true && draftPickupsList.isEmpty())
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


    private fun callAcceptDraftPickup(index: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.acceptDraftPickup(draftPickupsList[index]?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            currentPageIndex = 1
                            (binding.rvDraftPickups.adapter as PendingDraftPickupCellAdapter).clearList()
                            callGetPendingDraftPickups()
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

    private fun callRejectDraftPickup(index: Int) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.rejectDraftPickup(
                            draftPickupsList[index]?.id,
                            ""
                        )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                super.getContext(),
                                getString(R.string.success_operation_completed)
                            )
                            currentPageIndex = 1
                            (binding.rvDraftPickups.adapter as PendingDraftPickupCellAdapter).clearList()
                            callGetPendingDraftPickups()
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

    override fun onAcceptDraftPickup(index: Int) {
        callAcceptDraftPickup(index)
    }

    override fun onRejectDraftPickup(index: Int) {
        callRejectDraftPickup(index)
    }
}