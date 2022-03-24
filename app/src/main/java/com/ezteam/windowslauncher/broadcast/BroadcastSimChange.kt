package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BroadcastSimChange : BroadcastReceiver() {
    var changeListener: (() -> Unit)? = null
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            if ("android.intent.action.SIM_STATE_CHANGED" == intent.action) {
                changeListener?.invoke()
            }
        }
    }
}