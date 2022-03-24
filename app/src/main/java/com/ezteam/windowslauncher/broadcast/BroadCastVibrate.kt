package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.utils.center.VibrateFilterUtil

class BroadCastVibrate : BroadcastReceiver() {
    var lisDataControl: MutableList<ItemControl>? = null
    var adapter: AdapterControl? = null
    var position = 0
    var listenerChangeAction: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (AudioManager.RINGER_MODE_CHANGED_ACTION == it.action) {
                context?.let { c ->
                    if (lisDataControl != null && adapter != null) {
                        for (item in lisDataControl!!) {
                            if (item.name == c.resources.getString(R.string.vibrate) || item.name == c.resources.getString(
                                    R.string.mute
                                ) || item.name == c.resources.getString(R.string.ring)
                            ) {
                                item.isEnable = VibrateFilterUtil.detectVibrate(c)
                                item.name = VibrateFilterUtil.getNameState(context)
                                item.resId = VibrateFilterUtil.getIconState(context)
                                adapter!!.notifyItemChanged(lisDataControl!!.indexOf(item))
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