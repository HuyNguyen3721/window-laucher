package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.content.Intent
import android.provider.Settings

object SettingFilerUtil {
    fun onClickFilter(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        context.startActivity(intent)
    }
}