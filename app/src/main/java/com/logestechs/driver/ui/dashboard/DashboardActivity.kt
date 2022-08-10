package com.logestechs.driver.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.databinding.ActivityDashboardBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.ui.driverPackagesByStatusViewPager.DriverPackagesByStatusViewPagerActivity
import com.logestechs.driver.utils.*
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        callGetDashboardInfo()
    }

    private fun initData() {
        if (SharedPreferenceWrapper.getFailureReasons() == null) {
            getFailureReasons()
        }
    }

    private fun initOnClickListeners() {
        binding.buttonShowDashboardSubEntries.setOnClickListener(this)
        binding.imageViewDriverLogo.setOnClickListener(this)
        binding.dashEntryPendingPackages.root.setOnClickListener(this)
        binding.dashEntryAcceptedPackages.root.setOnClickListener(this)
        binding.dashEntryInCarPackages.root.setOnClickListener(this)
        binding.dashEntryScanPackages.root.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun bindDashboardData(data: GetDashboardInfoResponse?) {
        binding.dashEntryAcceptedPackages.textCount.text = data?.acceptedPackagesCount.toString()
        binding.dashEntryInCarPackages.textCount.text = data?.inCarPackagesCount.toString()
        binding.dashEntryPendingPackages.textCount.text = data?.pendingPackagesCount.toString()
        binding.dashEntryScanPackages.textCount.visibility = View.GONE

        binding.textInCarPackagesCount.text = data?.inCarPackagesCount.toString()
        binding.textDeliveredPackagesCount.text = data?.deliveredPackagesCount.toString()

        binding.textMassCodReportsSum.text =
            "${Helper.getCompanyCurrency()} ${data?.carriedMassReportsSum.toString()}"
        binding.textCodSum.text = "${Helper.getCompanyCurrency()} ${data?.carriedCodSum.toString()}"
    }


    //:- Action Handlers
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_show_dashboard_sub_entries -> {
                if (binding.containerDashboardSubEntries.visibility == View.VISIBLE) {
                    binding.containerDashboardSubEntries.visibility = View.GONE
                } else {
                    binding.containerDashboardSubEntries.visibility = View.VISIBLE
                }
            }

            R.id.image_view_driver_logo -> {
                if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                    Lingver.getInstance().setLocale(this, AppLanguages.ENGLISH.value)

                } else {
                    Lingver.getInstance().setLocale(this, AppLanguages.ARABIC.value)
                }

                val intent = Intent(
                    this,
                    DashboardActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            R.id.dash_entry_pending_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 0)
                startActivity(mIntent)
            }

            R.id.dash_entry_accepted_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 1)
                startActivity(mIntent)
            }

            R.id.dash_entry_in_car_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 2)
                startActivity(mIntent)
            }

            R.id.dash_entry_scan_packages -> {
                val mIntent = Intent(this, BarcodeScannerActivity::class.java)
                startActivity(mIntent)
            }
        }
    }

    //:-APIs 
    private fun callGetDashboardInfo() {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDashboardInfo()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            bindDashboardData(data)
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()?.string() ?: "")
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )

                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun getFailureReasons() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getFailureReasons()
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            SharedPreferenceWrapper.saveFailureReasons(data)
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                }
            }
        }
    }
}