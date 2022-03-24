package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.databinding.LayoutFastControlBinding
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.utils.center.AirPlaneFilterUtil

class BroadCastAirplane : BroadcastReceiver() {
    var lisDataControl: MutableList<ItemControl>? = null
    var adapter: AdapterControl? = null
    var binding: LayoutFastControlBinding? = null
    var position = 0
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED == it.action) {
                context?.let { c ->
                    if (lisDataControl != null && adapter != null) {
                        for (item in lisDataControl!!) {
                            if (item.name == c.resources.getString(R.string.airplane)) {
                                item.isEnable = AirPlaneFilterUtil.detectAirPlane(c)
                                adapter!!.notifyItemChanged(lisDataControl!!.indexOf(item))
                            }
                        }
                    }
                    if (checkAirplaneMode(c)) {
                        binding?.ivMobileData?.setImageResource(R.drawable.ic_airplane)
                    } else {
                        binding?.ivMobileData?.setImageResource(R.drawable.ic_signal_sim_full)
                    }
                }
            }
        }
    }

    private fun checkAirplaneMode(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}