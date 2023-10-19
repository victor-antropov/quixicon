package com.yymonitor.project.data.network.interceptors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.quixicon.domain.entities.exceptions.NoInternetConnectionException
import okhttp3.Interceptor
import okhttp3.Response

class InternetConnectionInterceptor(context: Context) : Interceptor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isConnected()) {
            return chain.proceed(chain.request().newBuilder().build())
        } else {
            throw NoInternetConnectionException()
        }
    }

    @Suppress("DEPRECATION")
    private fun isConnected() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    } else {
        connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}
