package m.kampukter.travelexpenses

import android.app.Application
import android.content.Context
import android.net.*
import androidx.lifecycle.LiveData

object NetworkLiveData : LiveData<Boolean>() {

    private lateinit var application: Application
    private lateinit var networkRequest: NetworkRequest

    override fun onActive() {
        super.onActive()
        getNetworkStatus()
    }

    fun init(application: Application) {
        this.application = application
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities. TRANSPORT_CELLULAR )
            .build()
    }

    private fun getNetworkStatus() {
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    postValue(true)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    postValue(false)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    postValue(false)
                }
            })
    }
}