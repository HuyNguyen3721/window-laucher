package com.ezteam.windowslauncher.utils

import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo

fun ResolveInfo.isSystemPackage(): Boolean {
    return activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}