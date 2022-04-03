package asset.trak.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData

class ConnectivityStatus(context: Context) : LiveData<Boolean>() {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object{
        var connectPopupShow=false
    }

    private val networkCallbacks = object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (connectPopupShow) {
                postValue(true)
                connectPopupShow=false
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            connectPopupShow=true
            postValue(false)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            connectPopupShow=true
            postValue(false)
        }
    }

    private fun checkInternet(){
        // we can user activeNetwork because our min sdk version is 23 if our min sdk version is less than 23
        // then we have to user connectivityManager.activeNetworkInfo (Note: Deperated)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                postValue(false)
            }
        }
        else {
            val network = connectivityManager.activeNetworkInfo
            if (network == null) {
                postValue(false)
            }
        }

            /**
             * After checking network its time to check network internet capabilities
             * whether connection has internet or not for that we will register the network
             * and then check network capabilities with the help of the callbacks
             */
            val requestBuilder =NetworkRequest.Builder().apply {
                addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // also for sdk version 23 or above
                addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            }.build()
            connectivityManager.registerNetworkCallback(requestBuilder,networkCallbacks)
    }

    override fun onActive() {
        super.onActive()
        checkInternet()
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallbacks)
    }
}