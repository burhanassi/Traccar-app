package com.logestechs.driver.utils.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.DeficitBalanceHistory
import com.logestechs.driver.databinding.BottomSheetDeficitsHistoryBinding
import com.logestechs.driver.ui.dashboard.deficitId
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsBottomSheetFragment
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.DeficitHistoryListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DeficitHistoryBottomSheet(
) : LogesTechsBottomSheetFragment(){

    private var _binding: BottomSheetDeficitsHistoryBinding? = null
    private val binding get() = _binding!!
    private var notificationsList: ArrayList<DeficitBalanceHistory> = ArrayList()
    private var loginResponse = SharedPreferenceWrapper.getLoginResponse()

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: BottomSheetDeficitsHistoryBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_deficits_history,
            container,
            false
        )

        _binding = v
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHistory()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = DeficitHistoryListAdapter(notificationsList, requireContext())
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvHistory.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvHistory.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvHistory.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        getHistory()
                    }
                }
            }
        }

    fun getHistory() {
        showWaitDialog()
        if (Helper.isInternetAvailable(requireContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDeficitBalanceHistory(deficitId)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    // Check if response was successful.
                    if (response!!.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        val totalRound: Int =
                            data.totalRecordsNo / (AppConstants.DEFAULT_PAGE_SIZE * currentPageIndex)
                        if (totalRound == 0) {
                            currentPageIndex = 1
                            isLastPage = true
                        } else {
                            currentPageIndex++
                            isLastPage = false
                        }

                        withContext(Dispatchers.Main) {
                            binding.textUnreadNotificationsCount.text = data.totalRecordsNo.toString()
                            (binding.rvHistory.adapter as DeficitHistoryListAdapter).update(
                                data.data
                            )
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    requireContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    requireContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                    isLoading = false
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(requireContext(), e.message)
                        } else {
                            Helper.showErrorMessage(
                                requireContext(),
                                e.stackTraceToString()
                            )
                        }
                    }
                    isLoading = false
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                requireContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }
}