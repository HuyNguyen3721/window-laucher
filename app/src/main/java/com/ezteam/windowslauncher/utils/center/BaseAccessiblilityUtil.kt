package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.content.pm.PackageManager

object BaseAccessiblilityUtil {

    const val NOT_CLICKABLE =
        0 // k click đc
    const val CLICKABLE_ENABLE =
        1 // click duoc - trang thai sau khi click la BAT >><< trong ham check thi no co nghia la dang bat
    const val CLICKABLE_DISABLE =
        2 // click duoc - trang thai sau khi click la TAT >><< trong ham check thi no co nghia la dang tat


    fun getStringByName(context: Context, name: String?): String {
        try {
            val resourcesPackageName = "com.android.systemui"
            val resources = context.packageManager.getResourcesForApplication(resourcesPackageName)
            val resourceId = resources.getIdentifier(name, "string", resourcesPackageName)
            return if (resourceId > 0) {
                resources.getString(resourceId)
            } else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}