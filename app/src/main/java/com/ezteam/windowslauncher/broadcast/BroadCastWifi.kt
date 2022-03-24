package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.utils.center.WifiFilterUtil

class BroadCastWifi : BroadcastReceiver() {
    var lisDataControl: MutableList<ItemControl>? = null
    var adapter: AdapterControl? = null
    var listenerConnected: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION == it.action) {
                context?.let { c ->
                    if (lisDataControl != null && adapter != null) {
                        for (item in lisDataControl!!) {
                            if (item.name == c.resources.getString(R.string.wifi)) {
                                if (item.isEnable != WifiFilterUtil.detectWifi(c)) {
                                    item.isEnable = WifiFilterUtil.detectWifi(c)
                                    item.isClickEnable = false
                                    listenerConnected?.invoke()
                                    adapter!!.notifyItemChanged(lisDataControl!!.indexOf(item))
                                }
                            }
                        }
                    } else {
                        listenerConnected?.invoke()
                    }
                }
            } else {
                listenerConnected?.invoke()
            }
        }
    }
}