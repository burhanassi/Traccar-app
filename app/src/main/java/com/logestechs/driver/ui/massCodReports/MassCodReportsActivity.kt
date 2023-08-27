package com.logestechs.driver.ui.massCodReports

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.GroupedMassCodReports
import com.logestechs.driver.data.model.MassCodReport
import com.logestechs.driver.databinding.ActivityMassCodReportsBinding
import com.logestechs.driver.ui.packageDeliveryScreens.massCodReportDelivery.MassCodReportDeliveryActivity
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.InCarGroupedMassCodReportAdapter
import com.logestechs.driver.utils.adapters.MassCodReportCellAdapter
import com.logestechs.driver.utils.dialogs.MassCodReportViewModeDialog
import com.logestechs.driver.utils.interfaces.MassCodReportCardListener
import com.logestechs.driver.utils.interfaces.MassCodReportViewModeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MassCodReportsActivity : LogesTechsActivity(),
    MassCodReportCardListener,
    MassCodReportViewModeDialogListener,
    View.OnClickListener {
    private lateinit var binding: ActivityMassCodReportsBinding
    private var massReportsList: ArrayList<MassCodReport?> = ArrayList()
    private var selectedViewMode: MassCodReportsViewMode = MassCodReportsViewMode.BY_CUSTOMER

    //pagination fields
    private var isLoading = false
    private var isLastPage = false
    private var currentPageIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMassCodReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        initListeners()
        getMassCodReportsBySelectedMode()
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvMassCodReports.adapter = MassCodReportCellAdapter(
            massReportsList, super.getContext(), listener = this
        )
        binding.rvMassCodReports.layoutManager = layoutManager
        binding.rvMassCodReports.addOnScrollListener(recyclerViewOnScrollListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Helper.showSuccessMessage(
                super.getContext(),
                getString(R.string.success_operation_completed)
            )
            clearList()
            getMassCodReportsBySelectedMode()
        }
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            clearList()
            getMassCodReportsBySelectedMode()
        }

        binding.toolbarMain.buttonNotifications.setOnClickListener(this)
        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.buttonViewMode.setOnClickListener(this)
    }

    private fun getMassCodReportsBySelectedMode() {
        if (selectedViewMode == MassCodReportsViewMode.BY_CUSTOMER) {
            if (binding.rvMassCodReports.adapter !is InCarGroupedMassCodReportAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvMassCodReports.adapter = InCarGroupedMassCodReportAdapter(
                    java.util.ArrayList(),
                    super.getContext(),
                    this
                )
                binding.rvMassCodReports.layoutManager = layoutManager
            }
            callGetMassCodReportsByCustomer()
        } else {
            if (binding.rvMassCodReports.adapter !is MassCodReportCellAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvMassCodReports.adapter = MassCodReportCellAdapter(
                    massReportsList, super.getContext(), listener = this
                )
                binding.rvMassCodReports.layoutManager = layoutManager
            }
            callGetMassCodReports()
        }
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvMassCodReports.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvMassCodReports.visibility = View.GONE
        }
    }

    private fun clearList() {
        currentPageIndex = 1
        if (binding.rvMassCodReports.adapter is MassCodReportCellAdapter) {
            (binding.rvMassCodReports.adapter as MassCodReportCellAdapter).clearList()
        } else if (binding.rvMassCodReports.adapter is InCarGroupedMassCodReportAdapter) {
            (binding.rvMassCodReports.adapter as InCarGroupedMassCodReportAdapter).clearList()
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
                val visibleItemCount: Int = binding.rvMassCodReports.layoutManager!!.childCount
                val totalItemCount: Int = binding.rvMassCodReports.layoutManager!!.itemCount
                val firstVisibleItemPosition: Int =
                    (binding.rvMassCodReports.layoutManager!! as LinearLayoutManager).findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= AppConstants.DEFAULT_PAGE_SIZE) {
                        if (selectedViewMode == MassCodReportsViewMode.BY_REPORT) {
                            callGetMassCodReports()
                        }
                    }
                }
            }
        }


    //apis
    @SuppressLint("NotifyDataSetChanged")
    private fun callGetMassCodReports() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getMassCodReports(page = currentPageIndex)
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
                            massReportsList.addAll(body?.massCodPackages ?: ArrayList())
                            binding.rvMassCodReports.adapter?.notifyDataSetChanged()
                            handleNoPackagesLabelVisibility(body?.massCodPackages?.size ?: 0)

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
    private fun callGetMassCodReportsByCustomer() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            isLoading = true
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getMassCodReportsByCustomer()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvMassCodReports.adapter as InCarGroupedMassCodReportAdapter).update(
                                body?.massCodPackagesByCustomer as java.util.ArrayList<GroupedMassCodReports?>,
                                selectedViewMode
                            )
                            handleNoPackagesLabelVisibility(
                                body.massCodPackagesByCustomer.size ?: 0
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

    override fun onDeliverMassReport(index: Int, massCodReport: MassCodReport?) {
        val mIntent = Intent(this, MassCodReportDeliveryActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.MASS_COD_REPORT_TO_DELIVER.name, massCodReport)
        startActivityForResult(mIntent, 1)
    }
    override fun onDeliverGroupReport(index: Int, groupedPackage: GroupedMassCodReports?) {
        val mIntent = Intent(this, MassCodReportDeliveryActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.MASS_COD_REPORT_TO_DELIVER_ALL.name, groupedPackage)
        startActivityForResult(mIntent, 1)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }

            R.id.button_notifications -> {
                super.getNotifications()
            }

            R.id.button_view_mode -> {
                MassCodReportViewModeDialog(super.getContext(), this, selectedViewMode).showDialog()
            }
        }
    }

    override fun onViewModeChanged(selectedViewMode: MassCodReportsViewMode) {
        this.selectedViewMode = selectedViewMode
        clearList()
        getMassCodReportsBySelectedMode()
    }
}


