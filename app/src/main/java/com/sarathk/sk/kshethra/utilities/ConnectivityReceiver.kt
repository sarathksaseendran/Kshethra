package com.sarathk.sk.kshethra.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sarathk.sk.kshethra.R

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnected)
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null

        val isConnected: Boolean
            get() {
                var result = false
                val connectivityManager = KshethraApplication.instance!!.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val networkCapabilities = connectivityManager.activeNetwork ?: return false
                    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                    result = when {
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                } else {
                    connectivityManager.run {
                        connectivityManager.activeNetworkInfo?.run {
                            result = when (type) {
                                ConnectivityManager.TYPE_WIFI -> true
                                ConnectivityManager.TYPE_MOBILE -> true
                                ConnectivityManager.TYPE_ETHERNET -> true
                                else -> false
                            }

                        }
                    }
                }
                return result
                //val activeNetwork = cm.activeNetworkInfo
                //return activeNetwork != null && activeNetwork.isConnected
            }

        fun errorDialog(context: Context) {
            val builder1 = AlertDialog.Builder(context)
            val inflater1 = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view1 = inflater1.inflate(R.layout.dialog_custom_alert_layout, null)
            builder1.setView(view1)
            val txtAlert1 = view1.findViewById<View>(R.id.message_tv) as TextView
            txtAlert1.text = context.getString(R.string.connection_error)
            val alertDialog = builder1.create()
            alertDialog.setCancelable(false)
            view1.findViewById<View>(R.id.cancel_button).visibility = View.GONE
            val btnOk = view1.findViewById<View>(R.id.ok_button) as Button
            btnOk.text = context.getString(R.string.ok)
            view1.findViewById<View>(R.id.ok_button).setOnClickListener { alertDialog.dismiss() }
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }
}