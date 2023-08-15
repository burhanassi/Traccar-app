package com.logestechs.driver.ui.driverRouteActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.AddNoteRequestBody
import com.logestechs.driver.api.requests.ChangePackageTypeRequestBody
import com.logestechs.driver.api.requests.CodChangeRequestBody
import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.api.requests.ReturnPackageRequestBody
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ActivityDriverRouteBinding
import com.logestechs.driver.databinding.ActivityReturnedPackagesBinding
import com.logestechs.driver.ui.packageDeliveryScreens.returnedPackageDelivery.ReturnedPackageDeliveryActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.adapters.InCarPackageCellAdapter
import com.logestechs.driver.utils.adapters.ReturnedPackageCustomerCellAdapter
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DriverRouteActivity : LogesTechsActivity(),
    View.OnClickListener,
    InCarPackagesCardListener {
    private lateinit var binding: ActivityDriverRouteBinding

    private var doesUpdateData = true
    private var enableUpdateData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()
        initListeners()
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
        binding.rvPackages.adapter = InCarPackageCellAdapter(
            ArrayList(),
            super.getContext(),
            this,
            null,
            isGrouped = false
        )
        binding.rvPackages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)

        binding.refreshLayoutPackages.setOnRefreshListener {
            if (binding.rvPackages.adapter !is InCarPackageCellAdapter) {
                val layoutManager = LinearLayoutManager(
                    super.getContext()
                )
                binding.rvPackages.adapter = InCarPackageCellAdapter(
                    ArrayList(),
                    super.getContext(),
                    this,
                    null,
                    isGrouped = false
                )
                binding.rvPackages.layoutManager = layoutManager
            }
            callGetInCarPackagesUngrouped()
        }
    }

    override fun hideWaitDialog() {
        super.hideWaitDialog()
        try {
            binding.refreshLayoutPackages.isRefreshing = false
        } catch (e: java.lang.Exception) {
            Helper.logException(e, Throwable().stackTraceToString())
        }
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
                            (binding.rvPackages.adapter as InCarPackageCellAdapter).update(
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

    override fun onPackageReturned(body: ReturnPackageRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onShowReturnPackageDialog(pkg: Package?) {
        TODO("Not yet implemented")
    }

    override fun onShowAttachmentsDialog(pkg: Package?) {
        TODO("Not yet implemented")
    }

    override fun onFailDelivery(body: FailDeliveryRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onPackagePostponed(body: PostponePackageRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onPackageTypeChanged(body: ChangePackageTypeRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onPackageNoteAdded(body: AddNoteRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onShowPackageNoteDialog(pkg: Package?) {
        TODO("Not yet implemented")
    }

    override fun onCodChanged(body: CodChangeRequestBody?) {
        TODO("Not yet implemented")
    }

    override fun onDeliverPackage(pkg: Package?) {
        TODO("Not yet implemented")
    }

    override fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onSendSmsMessage(pkg: Package?) {
        TODO("Not yet implemented")
    }

    override fun onCallReceiver(pkg: Package?, receiverPhone: String?) {
        TODO("Not yet implemented")
    }
}