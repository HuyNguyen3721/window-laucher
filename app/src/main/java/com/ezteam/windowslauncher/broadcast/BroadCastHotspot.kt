package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.utils.center.HotspotFilterUtil

class BroadCastHotspot : BroadcastReceiver() {
    lateinit var lisDataControl: MutableList<ItemControl>
    lateinit var adapter: AdapterControl
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            context?.let { c ->
                if (c.resources.getString(R.string.ACTION_HOTSPOT) == it.action) {
                    for (item in lisDataControl) {
                        if (item.name == c.resources.getString(R.string.hotspot)) {
                            item.isEnable = HotspotFilterUtil.detectHotspot(c)
                            adapter.notifyItemChanged(lisDataControl.indexOf(item))
                        }
                    }
                }
            }
        }
    }
}