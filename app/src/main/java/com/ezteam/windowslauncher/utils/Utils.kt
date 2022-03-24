package com.ezteam.windowslauncher.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.os.StatFs


object Utils {

    fun getTotalMemoryStorage(): Long {
        val statFs = StatFs(Environment.getExternalStorageDirectory().path)
        return statFs.blockSize.toLong() * statFs.blockCount.toLong()
    }

    fun getMemoryStorageAvailable(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.blockSize.toLong() * stat.availableBlocks.toLong()
    }

    fun hasWifiConnected(context: Context): Boolean {
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connManager?.let {
            val mWifi = it.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifi.isConnected
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected()
    }
}