package com.logestechs.driver.ui.acceptedDraftPickups

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.DraftPickup
import com.logestechs.driver.databinding.FragmentAcceptedDraftPickupsBinding
import com.logestechs.driver.ui.barcodeScanner.DraftPickupsBarcodeScanner
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.AcceptedDraftPickupCellAdapter
import com.logestechs.driver.utils.interfaces.AcceptedDraftPickupCardListener
import com.logestechs.driver.utils.interfaces.DriverDraftPickupsByStatusViewPagerActivityDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AcceptedDraftPickupsFragment : LogesTechsFragment(), AcceptedDraftPickupCardListener {

    private var _binding: FragmentAcceptedDraftPickupsBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: DriverDraftPickupsByStatusViewPagerActivityDelegate? = null

    private var draftPickupsList: ArrayList<DraftPickup?> = ArrayList()

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    private var doesUpdateData = true
    private var enableUpdateData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentAcceptedDraftPickupsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accepted_draft_pickups,
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
        binding.textTitle.text = getString(R.string.packages_view_pager_accepted_packages)
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
            currentPageIndex = 1
            (binding.rvAcceptedDraftPickups.adapter as AcceptedDraftPickupCellAdapter).clearList()
            callGetAcceptedDraftPickups()
        } else {
            doesUpdateData = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (enableUpdateData) {
            doesUpdateData = true
            enableUpdateData = false
        } else {
            doesUpdateData = false
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvAcceptedDraftPickups.adapter = AcceptedDraftPickupCellAdapter(
            draftPickupsList,
            super.getContext(),
            this
        )
        binding.rvAcceptedDraftPickups.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            currentPageIndex = 1
            (binding.rvAcceptedDraftPickups.adapter as AcceptedDraftPickupCellAdapter).clearList()
            callGetAcceptedDraftPickups()
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
    private fun callGetAcceptedDraftPickups() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDriverDraftPickups(
                        DraftPickupStatus.ACCEPTED_BY_DRIVER.name,
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
                            binding.rvAcceptedDraftPickups.adapter?.notifyDataSetChanged()
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

    override fun onScanPackagesForDraftPickup(index: Int) {
        enableUpdateData = true
        val mIntent = Intent(super.getContext(), DraftPickupsBarcodeScanner::class.java)
        mIntent.putExtra(IntentExtrasKeys.DRAFT_PICKUP.name, draftPickupsList[index])
        startActivity(mIntent)
    }
}