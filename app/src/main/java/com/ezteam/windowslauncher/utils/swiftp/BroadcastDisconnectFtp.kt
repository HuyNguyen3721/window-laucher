package com.ezteam.windowslauncher.utils.swiftp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BroadcastDisconnectFtp : BroadcastReceiver() {
    var disconnectListener: ((Unit) -> Unit)? = null
    override fun onReceive(context: Context, intent: Intent) {
        disconnectListener?.invoke(Unit)
    }
}