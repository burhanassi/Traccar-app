package com.logestechs.driver.ui.acceptedPackages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Village
import com.logestechs.driver.databinding.FragmentAcceptedPackagesBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.AcceptedPackageVillageCellAdapter
import com.logestechs.driver.utils.bottomSheets.AcceptedPackagesBottomSheet
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AcceptedPackagesFragment : LogesTechsFragment(), AcceptedPackagesCardListener {

    private var _binding: FragmentAcceptedPackagesBinding? = null
    private val binding get() = _binding!!
    private var activityDelegate: ViewPagerCountValuesDelegate? = null


    private lateinit var parentActivity: AppCompatActivity
    private lateinit var viewModel: RefreshViewModel

    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    var isSprint: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: FragmentAcceptedPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accepted_packages,
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AppCompatActivity) {
            parentActivity = context
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (loginResponse?.user?.companyID == 240.toLong() || loginResponse?.user?.companyID == 313.toLong()) {
            isSprint = true
        }
        viewModel = ViewModelProvider(requireActivity()).get(RefreshViewModel::class.java)
        viewModel.dataRefresh.observe(viewLifecycleOwner, Observer { refresh ->
            if (refresh) {
                initRecycler()
                initListeners()
                callGetAcceptedPackages()
                activityDelegate = activity as ViewPagerCountValuesDelegate
                if (isSprint) {
                    binding.textTitle.text =
                        getString(R.string.packages_view_pager_accepted_packages_sprint)
                } else {
                    binding.textTitle.text =
                        getString(R.string.packages_view_pager_accepted_packages)
                }
            }
        })
        initRecycler()
        initListeners()
        callGetAcceptedPackages()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        if (isSprint) {
            binding.textTitle.text =
                getString(R.string.packages_view_pager_accepted_packages_sprint)
        } else {
            binding.textTitle.text = getString(R.string.packages_view_pager_accepted_packages)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            callGetAcceptedPackages()
        }
    }
    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvVillages.adapter = AcceptedPackageVillageCellAdapter(
            ArrayList(),
            super.getContext(),
            requireFragmentManager(),
            this,
            isSprint
        )
        binding.rvVillages.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.refreshLayoutCustomers.setOnRefreshListener {
            callGetAcceptedPackages()
        }
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvVillages.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvVillages.visibility = View.GONE
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

    //APIs
    private fun callGetAcceptedPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getAcceptedPackages()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvVillages.adapter as AcceptedPackageVillageCellAdapter).update(
                                body?.villages as ArrayList<Village?>
                            )
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body.villages?.size ?: 0)
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
    private fun callGetAcceptedPackagesByCustomer(customer: Customer?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getAcceptedPackagesByCustomer(customerId = customer?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            val bottomSheet = AcceptedPackagesBottomSheet()
                                val bundle = Bundle()
                            val packageList = ArrayList<Parcelable>(response.body() ?: emptyList())
                            bundle.putParcelableArrayList(
                                BundleKeys.PACKAGES_KEY.toString(),
                                packageList
                            )

                                bundle.putInt(
                                    BundleKeys.PACKAGES_COUNT.toString(),
                                    response.body()!!.size
                                )
                                bottomSheet.arguments = bundle
                                bottomSheet.show(requireFragmentManager(), "exampleBottomSheet")
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

    private fun callPrintAwb(packageId: Long, isImage: Boolean) {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.printPackageAwb(packageId, isImage)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {

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

    override fun scanForPickup(customer: Customer?) {
        val mIntent = Intent(super.getContext(), BarcodeScannerActivity::class.java)
        mIntent.putExtra(IntentExtrasKeys.CUSTOMER_WITH_PACKAGES_FOR_PICKUP.name, customer)
        startActivity(mIntent)
    }

    override fun getAcceptedPackages(customer: Customer?) {
        callGetAcceptedPackagesByCustomer(customer)
    }

    override fun printAwb(packageId: Int, isImage: Boolean) {
//        callPrintAwb(packageId, isImage)
    }
}