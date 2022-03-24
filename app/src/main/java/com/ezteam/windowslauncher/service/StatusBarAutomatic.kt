package com.ezteam.windowslauncher.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log

object StatusBarAutomatic {

    fun expandStatusBar(context: Context) {
        try {
            @SuppressLint("WrongConstant") val service: Any = context.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("expandSettingsPanel")
            expand.invoke(service)
            object : CountDownTimer(600, 600) {
                override fun onTick(l: Long) {}
                override fun onFinish() {
                    Log.d("Huy", "collapseStatusBar : ")
                    collapseStatusBar(context)
                }
            }.start()
        } catch (ex: Exception) {
            Log.d("Huy", "Exception : ")
        }
    }

    private fun collapseStatusBar(context: Context) {
        try {
            @SuppressLint("WrongConstant") val service: Any = context.getSystemService("statusbar")
            val statusbarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusbarManager.getMethod("collapsePanels")
            expand.invoke(service)
        } catch (ex: java.lang.Exception) {
            Log.e("XXX", "onNewIntent: ", ex)
        }
    }
}