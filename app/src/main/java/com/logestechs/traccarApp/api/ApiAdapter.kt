package com.logestechs.traccarApp.api

import android.util.Log
import com.logestechs.traccarApp.BuildConfig
import com.logestechs.traccarApp.utils.AppConstants
import com.yariksoffice.lingver.Lingver
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiAdapter {

    private val loggingInterceptor = HttpLoggingInterceptor()

    // Language header interceptor
    private val headerInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()
        builder.header("languageCode", Lingver.getInstance().getLocale().toString())
        chain.proceed(builder.build())
    }

    // Session cookie jar
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
            cookies.forEach {
                Log.d("COOKIE_JAR", "Saved: ${it.name}=${it.value} for ${url.host}")
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val loadedCookies = cookieStore[url.host] ?: emptyList()
            loadedCookies.forEach {
                Log.d("COOKIE_JAR", "Loaded: ${it.name}=${it.value} for ${url.host}")
            }
            return loadedCookies
        }
    }

    // Define OkHttpClient
    private val okHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(200, TimeUnit.SECONDS)
                .readTimeout(200, TimeUnit.SECONDS)
                .writeTimeout(200, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)

            if (BuildConfig.DEBUG) {
                builder.addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
            }

            return builder.build()
        }

    // Dynamic base URL getter
    private val baseUrl: String
        get() = AppConstants.BASE_URL

    // Retrofit client initialized here
    var apiClient: TraccarAppApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TraccarAppApi::class.java)
}