package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.screen.MainActivity

class BroadCastChangeBackGround : BroadcastReceiver() {
    lateinit var mainActivity: MainActivity
    override fun onReceive(p0: Context?, p1: Intent?) {
        mainActivity.changeBackground()
    }
}