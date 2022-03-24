package com.ezteam.windowslauncher.broadcast

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.utils.center.BluetoothFilterUtil

class BroadCastBluetooth : BroadcastReceiver() {
    var lisDataControl: MutableList<ItemControl>? = null
    var adapter: AdapterControl? = null
    var listenerChangeAction: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == it.action) {
                context?.let { c ->
                    if (lisDataControl != null && adapter != null) {
                        for (item in lisDataControl!!) {
                            if (item.name == c.resources.getString(R.string.bluetooth)) {
                                if (item.isEnable != BluetoothFilterUtil.detectBluetooth(c)) {
                                    item.isEnable = BluetoothFilterUtil.detectBluetooth(c)
                                    item.isClickEnable = false
                                    adapter!!.notifyItemChanged(lisDataControl!!.indexOf(item))
                                }
                            }
                        }
                    } else {
                        listenerChangeAction?.invoke()
                    }
                }
            }
        }
    }
}