package com.logestechs.driver.api

import com.logestechs.driver.BuildConfig
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.yariksoffice.lingver.Lingver
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiAdapter {
    private val loggingInterceptor = HttpLoggingInterceptor()
    private val headerInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()

        builder.header(
            "Authorization-Token",
            SharedPreferenceWrapper.getLoginResponse()?.authToken ?: "",
        )
        builder.header(
            "languageCode",
            Lingver.getInstance().getLocale().toString()
        )

        return@Interceptor chain.proceed(builder.build())
    }

    private val baseUrl: String
        get() {
            return if (Helper.isBackendDriver()) {
                val selectedServerIp = SharedPreferenceWrapper.getSelectedServerIp()
                if (selectedServerIp.isEmpty()) {
                    AppConstants.BASE_URL
                } else {
                    "http://${selectedServerIp}/"
                }
            } else {
                AppConstants.BASE_URL
            }
        }

    var apiClient: LogesTechsDriverApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LogesTechsDriverApi::class.java)


    private val timeOut: Long
        get() {
            return if (Helper.isBackendDriver()) {
                30L
            } else {
                200L
            }
        }

    private val okHttpClient: OkHttpClient
        get() {
            if (BuildConfig.DEBUG) {
                return OkHttpClient.Builder()
                    .connectTimeout(timeOut, TimeUnit.SECONDS)
                    .readTimeout(timeOut, TimeUnit.SECONDS)
                    .writeTimeout(timeOut, TimeUnit.SECONDS)
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            } else {
                return OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.SECONDS)
                    .readTimeout(200, TimeUnit.SECONDS)
                    .writeTimeout(200, TimeUnit.SECONDS)
                    .addInterceptor(headerInterceptor)
                    .build()
            }
        }

    fun recreateApiClient() {
        apiClient = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LogesTechsDriverApi::class.java)

    }
}