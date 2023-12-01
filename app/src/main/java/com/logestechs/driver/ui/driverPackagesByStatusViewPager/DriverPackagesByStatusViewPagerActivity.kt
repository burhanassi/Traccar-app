package com.logestechs.driver.ui.driverPackagesByStatusViewPager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.databinding.ActivityDriverPackagesByStatusViewPagerBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.interfaces.ViewPagerCountValuesDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DriverPackagesByStatusViewPagerActivity : LogesTechsActivity(), View.OnClickListener,
    ViewPagerCountValuesDelegate {
    private lateinit var binding: ActivityDriverPackagesByStatusViewPagerBinding
    private lateinit var packagesByStatusViewPagerAdapter: PackagesByStatusViewPagerAdapter

    private var selectedTabIndex = 0
    private var selectedInCarStatus: String? = InCarPackageStatus.TO_DELIVER.name
    private var loginResponse = SharedPreferenceWrapper.getLoginResponse()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverPackagesByStatusViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        initListeners()
        initViewPager()
    }

    override fun onResume() {
        super.onResume()
        callGetDashboardInfo()
    }

    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            selectedTabIndex = extras.getInt(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name)
            selectedInCarStatus = extras.getString(IntentExtrasKeys.IN_CAR_PACKAGE_STATUS.name)
        }
    }

    private fun initViewPager() {
        binding.viewPager.isUserInputEnabled = false
        packagesByStatusViewPagerAdapter =
            PackagesByStatusViewPagerAdapter(
                supportFragmentManager,
                lifecycle,
                selectedInCarStatus ?: InCarPackageStatus.TO_DELIVER.name
            )
        binding.viewPager.adapter = packagesByStatusViewPagerAdapter
        binding.viewPager.setCurrentItem(selectedTabIndex, false)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val inflater = LayoutInflater.from(tab.parent?.context)
            val binding = inflater.inflate(R.layout.tab_item_indicator, tab.view, false)
            val imageViewTabIcon = binding.findViewById<ImageView>(R.id.image_view_tab_icon)
            val imageViewTriangleIcon = binding.findViewById<ImageView>(R.id.image_view_triangle)
            val textViewCount = binding.findViewById<TextView>(R.id.text_view_count)

            when (position) {
                0 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_pending_packages_tab_item
                        )
                    )
                    if (position == selectedTabIndex) {
                        makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    } else {
                        makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    }
                }
                1 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_accepted_packages_tab_item
                        )
                    )
                    if (position == selectedTabIndex) {
                        makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    } else {
                        makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    }
                }
                2 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_in_car_packages_tab_item
                        )
                    )
                    if (position == selectedTabIndex) {
                        makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    } else {
                        makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    }
                }
                3 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_delivered_packages_tab_item
                        )
                    )
                    if (position == selectedTabIndex) {
                        makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    } else {
                        makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                    }
                }
            }
            tab.customView = binding
            updateTitle(selectedTabIndex)
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as LinearLayout
                val imageViewTabIcon = customView.findViewById<ImageView>(R.id.image_view_tab_icon)
                val imageViewTriangleIcon =
                    customView.findViewById<ImageView>(R.id.image_view_triangle)
                val textViewCount = customView.findViewById<TextView>(R.id.text_view_count)
                makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                selectedTabIndex = tab.position
                updateTitle(selectedTabIndex)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as LinearLayout
                val imageViewTabIcon = customView.findViewById<ImageView>(R.id.image_view_tab_icon)
                val imageViewTriangleIcon =
                    customView.findViewById<ImageView>(R.id.image_view_triangle)
                val textViewCount = customView.findViewById<TextView>(R.id.text_view_count)
                makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as LinearLayout
                val imageViewTabIcon = customView.findViewById<ImageView>(R.id.image_view_tab_icon)
                val imageViewTriangleIcon =
                    customView.findViewById<ImageView>(R.id.image_view_triangle)
                val textViewCount = customView.findViewById<TextView>(R.id.text_view_count)
                makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                selectedTabIndex = tab.position
                updateTitle(selectedTabIndex)
            }

        })
    }

    private fun initListeners() {
        binding.toolbarMain.buttonBack.setOnClickListener(this)
        binding.toolbarMain.buttonNotifications.setOnClickListener(this)

        binding.toolbarMain.notificationCount.text = SharedPreferenceWrapper.getNotificationsCount()
    }

    private fun makeTabSelected(
        tabIcon: ImageView,
        countTextView: TextView,
        triangleIcon: ImageView
    ) {
        val unwrappedDrawable =
            tabIcon.drawable
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.white))
        triangleIcon.visibility = View.VISIBLE

        countTextView.setBackgroundResource(R.drawable.background_dashboard_item_count)
    }

    private fun makeTabUnselected(
        tabIcon: ImageView,
        countTextView: TextView,
        triangleIcon: ImageView
    ) {
        val unwrappedDrawable =
            tabIcon.drawable
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.navajo_white))
        triangleIcon.visibility = View.GONE
        countTextView.setBackgroundResource(R.drawable.background_dashboard_item_count_unselected)
    }

    private fun updateTitle(index: Int) {
//        when (index) {
//            0 -> {
//            }
//
//            1 -> {
//            }
//
//            2 -> {
//            }
//
//            3 -> {
//                binding.textTitle.text = getString(R.string.dashboard_delivered)
//            }
//        }
    }

    private fun updateCountValues(data: GetDashboardInfoResponse?) {
        binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.text_view_count)?.text =
            data?.pendingPackagesCount.toString()
        binding.tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.text_view_count)?.text =
            data?.acceptedPackagesCount.toString()
        binding.tabLayout.getTabAt(2)?.customView?.findViewById<TextView>(R.id.text_view_count)?.text =
            data?.inCarPackagesCount.toString()
        binding.tabLayout.getTabAt(3)?.customView?.findViewById<TextView>(R.id.text_view_count)?.text =
            data?.deliveredPackagesCount.toString()
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

    private fun callGetDashboardInfo() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDashboardInfo(loginResponse?.device?.id)
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            updateCountValues(data)
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
            Helper.showErrorMessage(
                getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    override fun updateCountValues() {
        callGetDashboardInfo()
    }
}