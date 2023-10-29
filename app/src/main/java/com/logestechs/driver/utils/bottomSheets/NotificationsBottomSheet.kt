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
import com.logestechs.driver.data.model.Notification
import com.logestechs.driver.databinding.BottomSheetNotificationsBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.BundleKeys
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsBottomSheetFragment
import com.logestechs.driver.utils.adapters.NotificationsListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class NotificationsBottomSheet(
) : LogesTechsBottomSheetFragment(), NotificationsListAdapter.OnItemClickListener {

    private var _binding: BottomSheetNotificationsBinding? = null
    private val binding get() = _binding!!
    private var notificationsList: ArrayList<Notification> = ArrayList()
    private var unreadNotificationsCount = 0

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: BottomSheetNotificationsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_notifications,
            container,
            false
        )

        notificationsList =
            arguments?.getParcelableArrayList(BundleKeys.NOTIFICATIONS_KEY.toString())
                ?: ArrayList()
        unreadNotificationsCount =
            arguments?.getInt(BundleKeys.UNREAD_NOTIFICATIONS_COUNT.toString()) ?: 0

        _binding = v
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = NotificationsListAdapter(notificationsList, this@NotificationsBottomSheet)
            addOnScrollListener(recyclerViewOnScrollListener)
        }

        binding.textUnreadNotificationsCount.text = unreadNotificationsCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = binding.rvNotifications.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvNotifications.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvNotifications.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        getNotifications()
                    }
                }
            }

            fun getNotifications() {
                showWaitDialog()
                if (Helper.isInternetAvailable(requireContext())) {
                    isLoading = true
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val response = ApiAdapter.apiClient.getNotifications(
                                AppConstants.DEFAULT_PAGE_SIZE,
                                currentPageIndex
                            )
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
                                    (binding.rvNotifications.adapter as NotificationsListAdapter).update(
                                        data.notificationsList
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

    override fun onItemClick(packageId: Long) {
        (requireActivity() as LogesTechsActivity).trackShipmentNotification(packageId)
    }
}