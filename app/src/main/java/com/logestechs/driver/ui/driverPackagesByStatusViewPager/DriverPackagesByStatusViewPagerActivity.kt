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
import com.logestechs.driver.databinding.ActivityDriverPackagesByStatusViewPagerBinding
import com.logestechs.driver.utils.LogesTechsActivity


class DriverPackagesByStatusViewPagerActivity : LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityDriverPackagesByStatusViewPagerBinding
    private lateinit var packagesByStatusViewPagerAdapter: PackagesByStatusViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverPackagesByStatusViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        initViewPager()
    }

    private fun initViewPager() {
        packagesByStatusViewPagerAdapter =
            PackagesByStatusViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = packagesByStatusViewPagerAdapter
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
                    );
                    makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                }
                1 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_accepted_packages_tab_item
                        )
                    );
                    makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                }
                2 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_in_car_packages_tab_item
                        )
                    );
                    makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                }
                3 -> {
                    imageViewTabIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_delivered_packages_tab_item
                        )
                    );
                    makeTabUnselected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
                }
            }
            tab.customView = binding
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as LinearLayout
                val imageViewTabIcon = customView.findViewById<ImageView>(R.id.image_view_tab_icon)
                val imageViewTriangleIcon =
                    customView.findViewById<ImageView>(R.id.image_view_triangle)
                val textViewCount = customView.findViewById<TextView>(R.id.text_view_count)
                makeTabSelected(imageViewTabIcon, textViewCount, imageViewTriangleIcon)
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
            }

        })

        this.binding.tabLayout.getTabAt(1)?.select()
    }

    private fun initListeners() {
        binding.toolbarMain.buttonBack.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_back -> {
                onBackPressed()
            }
        }
    }
}