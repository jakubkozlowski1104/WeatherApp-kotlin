package com.example.weatherappkotlin
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import androidx.lifecycle.LiveData

class NetworkConnection(private val context: Context) : LiveData<Boolean>() {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateNetworkConnection()
        }
    }

    override fun onActive() {
        super.onActive()
        registerNetworkCallback()
        context.registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onInactive() {
        super.onInactive()
        unregisterNetworkCallback()
        context.unregisterReceiver(networkReceiver)
    }

    private fun registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    postValue(true)
                }

                override fun onLost(network: Network) {
                    postValue(false)
                }
            }
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        }
    }

    private fun unregisterNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
                networkCallback = null
            }
        }
    }

    private fun updateNetworkConnection() {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(networkInfo?.isConnected == true)
    }
}
