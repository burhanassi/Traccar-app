package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding4.widget.textChanges
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.DropdownItem
import com.logestechs.driver.databinding.DialogChooseLocationBinding
import com.logestechs.driver.utils.DropdownTag
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.adapters.DropdownListAdapter
import com.logestechs.driver.utils.interfaces.ChooseLocationDialogListener
import com.logestechs.driver.utils.interfaces.OnDropDownItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ChooseLocationDialog(
    var context: Context,
    var listener: ChooseLocationDialogListener,
    var customerId: Long,
    var productId: Long
): OnDropDownItemClickListener {
    lateinit var binding: DialogChooseLocationBinding
    lateinit var alertDialog: AlertDialog

    private var selectedLocation: String? = null
    private var customerLocations: HashMap<String, Long> = HashMap()
    private var search: String? = null
    private var villagesList: List<DropdownItem> = emptyList()
    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogChooseLocationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_choose_location, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.etLocations.editText.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribe({
                if (binding.etLocations.editText.hasFocus() && it.isNotEmpty()) {
                    binding.dropdownLocations.rvDropdownList.selectedItem = null
                        search = it.toString()
                        callGetCustomerLocations()
                }

            }, {
                Log.e("MainActivity", it.toString())
            })

        binding.dropdownLocations.rvDropdownList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                DropdownListAdapter(villagesList, this@ChooseLocationDialog, DropdownTag.LOCATIONS)
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
            listener.onDismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (selectedLocation == null) {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.title_choose_location)
                )
            } else {
                alertDialog.dismiss()
                val locationId = customerLocations[selectedLocation]
                if (locationId != null) {
                    listener.onChooseLocation(locationId)
                }
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun callGetCustomerLocations() {
        if (Helper.isInternetAvailable(context)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getCustomerLocations(
                        customerId,
                        search,
                        productId
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val warehouseLocations = response.body()?.data ?: emptyList()
                        customerLocations.clear()

                        warehouseLocations.forEach { location ->
                            location.barcode?.let { barcode ->
                                location.id?.let { id ->
                                    customerLocations[barcode] = id
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            (binding.dropdownLocations.rvDropdownList.adapter as DropdownListAdapter).update(
                                response.body()?.data!!
                            )
                            binding.dropdownLocations.rvDropdownList.expand(response.body()?.totalRecordsNo?.toInt()!!)
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(context, e.message)
                        } else {
                            Helper.showErrorMessage(context, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            Helper.showErrorMessage(
                context, getStringForFragment(R.string.error_check_internet_connection)
            )

        }
    }

    override fun onItemClick(item: DropdownItem, tag: DropdownTag) {
        when (tag) {
            DropdownTag.LOCATIONS -> {
                binding.dropdownLocations.rvDropdownList.collapse()
                binding.dropdownLocations.rvDropdownList.selectedItem = item
                binding.etLocations.editText.clearFocus()
                binding.etLocations.editText.setText(item.toString())
                selectedLocation = item.toString()
            }

            else -> {}
        }
    }
}