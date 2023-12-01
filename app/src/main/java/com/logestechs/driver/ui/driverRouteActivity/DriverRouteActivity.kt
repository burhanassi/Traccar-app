package com.logestechs.driver.ui.driverRouteActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.DriverRouteRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityDriverRouteBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.ItemTouchHelperCallback
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.DriverRoutePackagesCellAdapter
import com.logestechs.driver.utils.interfaces.OnStartDragListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DriverRouteActivity : LogesTechsActivity(),
    View.OnClickListener,
    DriverRoutePackagesCellAdapter.ItemTouchHelperAdapter,
    OnStartDragListener {
    private lateinit var binding: ActivityDriverRouteBinding

    private var doesUpdateData = true
    private var enableUpdateData = false

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val arrangedPackagesList: ArrayList<Long?> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        initListeners()

        val adapter = binding.rvPackages.adapter as DriverRoutePackagesCellAdapter
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvPackages)
    }

    override fun onResume() {
        super.onResume()
        if (doesUpdateData) {
            callGetInCarPackagesUngrouped()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Helper.showSuccessMessage(
                super.getContext(),
                getString(R.string.success_operation_completed)
            )
            callGetInCarPackagesUngrouped()
        }
    }


    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvPackages.adapter = DriverRoutePackagesCellAdapter(
            ArrayList(),
            super.getContext(),
            null,
            isGrouped = false
        )
        binding.rvPackages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.buttonDone.setOnClickListener(this)
        binding.refreshLayoutPackages.setOnRefreshListener {
            if (binding.rvPackages.adapter !is DriverRoutePackagesCellAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvPackages.adapter = DriverRoutePackagesCellAdapter(
                    ArrayList(),
                    super.getContext(),
                    null,
                    isGrouped = false
                )
                binding.rvPackages.layoutManager = layoutManager
            }
            callGetInCarPackagesUngrouped()
        }
        binding.toolbarMain.notificationCount.text = SharedPreferenceWrapper.getNotificationsCount()
    }

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
            binding.refreshLayoutPackages.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
    }

    private fun handleDoneButtonClick() {
        val adapter = binding.rvPackages.adapter as DriverRoutePackagesCellAdapter
        if (adapter != null) {
            arrangedPackagesList?.clear()
            arrangedPackagesList?.addAll(adapter.getArrangedPackageIds())
        }
        callSendDriverRoute()
    }

    //apis
    private fun callGetInCarPackagesUngrouped() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getInCarPackagesUngrouped(
                            status = InCarPackageStatus.TO_DELIVER.value,
                            packageType = PackageType.ALL.name
                        )

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as DriverRoutePackagesCellAdapter).update(
                                body?.pkgs ?: ArrayList()
                            )
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

    private fun callSendDriverRoute() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.sendDriverRoute(
                            DriverRouteRequestBody(arrangedPackagesList)
                        )

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            onBackPressed()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }

            R.id.button_done -> {
                handleDoneButtonClick()
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        (binding.rvPackages.adapter as? DriverRoutePackagesCellAdapter)?.onItemMove(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }
}