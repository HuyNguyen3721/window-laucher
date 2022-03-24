package com.ezteam.windowslauncher.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.service.MyAccessibilityService

object AccessibilityKey {
    const val IS_RUNNING = "IS_RUNNING"
    const val AUTO_CLICKING = "AUTO_CLICK"
    fun startAccessibilitySetting(context: Context) {
        var intent = Intent("com.samsung.accessibility.installed_service")
        if (intent.resolveActivity(context.packageManager) == null) {
            intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        }
        val bundle = Bundle()
        val str = context.packageName + "/" + MyAccessibilityService::class.java.getName()
        bundle.putString(":settings:fragment_args_key", str)
        intent.putExtra(":settings:fragment_args_key", str)
        intent.putExtra(":settings:show_fragment_args", bundle)
        try {
            context.startActivity(intent)
            Toast.makeText(
                context,
                context.resources.getString(
                    R.string.find_app_here,
                    context.resources.getString(R.string.app_name)
                ),
                Toast.LENGTH_SHORT
            ).show()
        } catch (unused: java.lang.Exception) {
        }
    }
}