package com.logestechs.driver.ui.signUp

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.rxbinding4.widget.textChanges
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.SignUpRequestBody
import com.logestechs.driver.data.model.*
import com.logestechs.driver.databinding.ActivitySignUpBinding
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.adapters.DropdownListAdapter
import com.logestechs.driver.utils.interfaces.OnDropDownItemClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit


class SignUpActivity : LogesTechsActivity(), View.OnClickListener, OnDropDownItemClickListener {

    private lateinit var binding: ActivitySignUpBinding

    private var villagesList: List<DropdownItem> = emptyList()

    private var companyInfo: CompanyInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initDropdowns()
        initListeners()
    }

    private fun initData() {
        companyInfo = intent.getParcelableExtra(BundleKeys.COMPANY_INFO.name)
        if (companyInfo?.currency == AppCurrency.SAR.name) {
            binding.etMobileNumber.textView.text = getString(R.string.hint_jawwal_number)
            binding.etEmail.textView.text = getString(R.string.hint_email_without_username)
        }
    }

    private fun initDropdowns() {
        binding.dropdownVillages.rvDropdownList.apply {
            layoutManager = LinearLayoutManager(this@SignUpActivity)
            adapter =
                DropdownListAdapter(villagesList, this@SignUpActivity, DropdownTag.SIGN_UP_VILLAGES)
        }
    }

    private fun initListeners() {
        binding.buttonDone.setOnClickListener(this)
        binding.buttonExit.setOnClickListener(this)
        binding.etAddress.editText.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribe({
                if (binding.etAddress.editText.hasFocus() && it.isNotEmpty()) {
                    binding.dropdownVillages.rvDropdownList.selectedItem = null
                    searchVillages(it.toString())
                }
            }, {
                Log.e("MainActivity", it.toString())
            })
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.etDriverName.isEmpty()) {
            binding.etDriverName.makeInvalid()
            isValid = false
        } else {
            binding.etDriverName.makeValid()
        }

        if (binding.dropdownVillages.rvDropdownList.selectedItem == null || binding.etAddress.getText()
                .isEmpty()
        ) {
            binding.etAddress.makeInvalid()
            isValid = false
        } else {
            binding.etAddress.makeValid()
        }

        if (binding.etAddressDescription.isEmpty()) {
            binding.etAddressDescription.makeInvalid()
            isValid = false
        } else {
            binding.etAddressDescription.makeValid()
        }

        if (binding.etEmail.isEmpty()) {
            binding.etEmail.makeInvalid()
            isValid = false
        } else {
            binding.etEmail.makeValid()
        }

        val currency = if (companyInfo?.currency == "NIS") {
            AppCurrency.NIS.value
        } else {
            companyInfo?.currency
        }

        val phoneValidationResult =
            Helper.validateMobileNumber(
                PhoneType.MOBILE,
                binding.etMobileNumber.getText(),
                currency
            )
        if (phoneValidationResult.isValid != true) {
            isValid = false
            binding.etMobileNumber.makeInvalid()
        } else {
            binding.etMobileNumber.makeValid()
        }

        if (binding.etPassword.isEmpty()) {
            binding.etPassword.makeInvalid()
            isValid = false
        } else {
            binding.etPassword.makeValid()
        }


        return isValid
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    //apis
    private fun searchVillages(searchPhrase: String) {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getVillages(search = searchPhrase)
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        withContext(Dispatchers.Main) {
                            (binding.dropdownVillages.rvDropdownList.adapter as DropdownListAdapter).update(
                                data.data
                            )
                            binding.dropdownVillages.rvDropdownList.expand(data.data.size)
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@SignUpActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@SignUpActivity,
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(this@SignUpActivity, e.message)
                        } else {
                            Helper.showErrorMessage(this@SignUpActivity, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            Helper.showErrorMessage(
                this@SignUpActivity, getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callSignUp(fcmToken: String?) {
        var uuid = SharedPreferenceWrapper.getUUID()

        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            SharedPreferenceWrapper.saveUUID(uuid)
        }
        val body = SignUpRequestBody(
            binding.etDriverName.getText(),
            companyInfo?.name,
            binding.etEmail.getText(),
            binding.etMobileNumber.getText(),
            binding.etPassword.getText(),
            Address.getAddressFromVillage(
                binding.dropdownVillages.rvDropdownList.selectedItem as Village,
                binding.etAddressDescription.getText()
            ),
            Device(uuid, "ANDROID", fcmToken)
        )
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.signUp(body)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response!!.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            Helper.showSuccessMessage(
                                this@SignUpActivity,
                                getString(R.string.success_sign_up)
                            )
                            finish()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@SignUpActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@SignUpActivity,
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
                            Helper.showErrorMessage(this@SignUpActivity, e.message)
                        } else {
                            Helper.showErrorMessage(this@SignUpActivity, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                this, getString(R.string.error_check_internet_connection)
            )
        }
    }


    override fun onItemClick(item: DropdownItem, tag: DropdownTag) {

        when (tag) {
            DropdownTag.SIGN_UP_VILLAGES -> {
                binding.dropdownVillages.rvDropdownList.collapse()
                binding.dropdownVillages.rvDropdownList.selectedItem = item
                binding.etAddress.editText.clearFocus()
                binding.etAddress.editText.setText(item.toString())
            }

            else -> {}
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.button_done -> {
                if (validateInput()) {
                    showWaitDialog()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            callSignUp(null)
                            return@OnCompleteListener
                        }
                        callSignUp(task.result)
                    })
                } else {
                    Helper.showErrorMessage(
                        this@SignUpActivity,
                        getString(R.string.error_fill_all_mandatory_fields)
                    )
                }
            }
            R.id.button_exit -> {
                finish()
            }
        }
    }
}