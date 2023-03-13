package com.logestechs.driver.ui.deliveredPackages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.databinding.FragmentDeliveredPackagesBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.DeliveredPackageCellAdapter
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DeliveredPackagesFragment : LogesTechsFragment() {
    private var _binding: FragmentDeliveredPackagesBinding? = null
    private val binding get() = _binding!!

    private var activityDelegate: ViewPagerCountValuesDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: FragmentDeliveredPackagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_delivered_packages,
            container,
            false
        )
        _binding = v
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListeners()
        activityDelegate = activity as ViewPagerCountValuesDelegate
        binding.textTitle.text = getString(R.string.title_delivered_packages)
    }

    override fun onResume() {
        super.onResume()
        if (!LogesTechsApp.isInBackground) {
            callGetDeliveredPackages()
        }
    }

    private fun initListeners() {
        binding.refreshLayoutPackages.setOnRefreshListener {
            callGetDeliveredPackages()
        }
    }

    private fun initRecycler() {
        val layoutManager = LinearLayoutManager(
            super.getContext()
        )
        binding.rvPackages.adapter = DeliveredPackageCellAdapter(
            ArrayList(),
            super.getContext(),
        )
        binding.rvPackages.layoutManager = layoutManager
    }

    private fun handleNoPackagesLabelVisibility(count: Int) {
        if (count > 0) {
            binding.textNoPackagesFound.visibility = View.GONE
            binding.rvPackages.visibility = View.VISIBLE
        } else {
            binding.textNoPackagesFound.visibility = View.VISIBLE
            binding.rvPackages.visibility = View.GONE
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
    private fun callGetDeliveredPackages() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getDeliveredPackages()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            (binding.rvPackages.adapter as DeliveredPackageCellAdapter).update(
                                body?.pkgs ?: ArrayList()
                            )
                            activityDelegate?.updateCountValues()
                            handleNoPackagesLabelVisibility(body?.pkgs?.size ?: 0)
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

}