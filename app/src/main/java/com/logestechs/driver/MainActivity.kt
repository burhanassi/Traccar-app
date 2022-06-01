package com.logestechs.driver

import android.os.Bundle
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.LoginRequestBody
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class MainActivity : LogesTechsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callLoginApi()
    }

    //APIs
    private fun callLoginApi() {
        var uuid = SharedPreferenceWrapper.getUUID()

        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            SharedPreferenceWrapper.saveUUID(uuid)
        }
        val loginRequestBody = LoginRequestBody(
            "driver@gmail.com",
            "test123",
            Device(
                uuid,
                "ANDROID",
                "cUqOpVF5T3iPU6-89fXa-O:APA91bHbjiqNuinIymIdRNEd91IvB6rUzPJpXxHQZV48ByJPh_6z22HctRWz7AKICX8zE1WIJqQ1UtnQvg4F9QFIyO-6i8Mz82wsr5zU1MuUQKxRw_602H_vaUuhQNymRbwBekuie4vH"
            ),
            "logestechs",
            "MOBILE"
        )
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.login(loginRequestBody)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        SharedPreferenceWrapper.saveLoginResponse(body)
                        withContext(Dispatchers.Main) {
                            Helper.showErrorMessage(
                                context = this@MainActivity,
                                message = response.body()?.authToken
                            )
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@MainActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@MainActivity,
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
                            Helper.showErrorMessage(this@MainActivity, e.message)
                        } else {
                            Helper.showErrorMessage(this@MainActivity, e.stackTraceToString())
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
}