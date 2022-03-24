package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BroadCastChangeBattery : BroadcastReceiver() {
    //    lateinit var binding: LayoutStatusBinding
    var isPowerConnected = false
    var batteryPct = 0
    var listenerChangeAction: ((Int, Boolean) -> Unit)? = null
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                // khi sac luon luon vao
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPct = (level * 100 / scale)
                listenerChangeAction?.invoke(batteryPct, isPowerConnected)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                // co vao ACTION_POWER_CONNECTED
                isPowerConnected = true
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                isPowerConnected = false
                listenerChangeAction?.invoke(batteryPct, isPowerConnected)
            }
        }
    }


}