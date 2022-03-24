package com.ezteam.windowslauncher.model

import android.content.Context
import android.content.pm.ResolveInfo
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ezteam.baseproject.extensions.capitalizeFirst
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.windowslauncher.utils.isSystemPackage
import com.ezteam.windowslauncher.utils.launcher.LauncherType

@Keep
@Entity(tableName = "launcher")
data class LauncherModel(
    @ColumnInfo(name = "groupId")
    var groupId: String = "",
    @ColumnInfo(name = "packageName")
    @PrimaryKey
    var packageName: String = "",
    @ColumnInfo(name = "logoResId")
    var logoResId: Int = 0,
    @ColumnInfo(name = "appName")
    var appName: String = "",
    @ColumnInfo(name = "position")
    var position: Int = 0,
    @ColumnInfo(name = "recentCount")
    var recentCount: Int = 0,
    @ColumnInfo(name = "prioritize")
    var prioritize: Int = 0,
    @ColumnInfo(name = "pinHome")
    var pinHome: Boolean = false,
    @ColumnInfo(name = "pinDesktop")
    var pinDesktop: Boolean = false,
    @Ignore
    var showHeader: Boolean = false,
    @ColumnInfo(name = "launcherType")
    var launcherType: Int = 0,
    @ColumnInfo(name = "systemPackage")
    var systemPackage: Boolean = false,
    @ColumnInfo(name = "password")
    var password: String = "",
    @ColumnInfo(name = "timeRecent")
    var timeRecent: Long = 0,
    @ColumnInfo(name = "timeCreated")
    var timeCreated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "pinTaskbar")
    var pinTaskbar: Boolean = false,
    @ColumnInfo(name = "timePinTaskbar")
    var timePinTaskbar: Long = System.currentTimeMillis()
) {
    constructor(resolveInfo: ResolveInfo, context: Context) : this() {
        packageName = resolveInfo.activityInfo.packageName
        logoResId = getLogo(resolveInfo)
        appName = getName(resolveInfo, context).capitalizeFirst()
        launcherType = LauncherType.APP_SYSTEM.value
        systemPackage = resolveInfo.isSystemPackage()
        timeRecent = System.currentTimeMillis()
    }


    private fun getLogo(resolveInfo: ResolveInfo): Int {
        val logo = resolveInfo.activityInfo.applicationInfo.logo
        val icon = resolveInfo.activityInfo.applicationInfo.icon
        return if (icon == 0) logo else icon
    }

    private fun getName(resolveInfo: ResolveInfo, context: Context): String {
        val labelResId = resolveInfo.activityInfo.applicationInfo.labelRes
        val label = resolveInfo.activityInfo.applicationInfo.nonLocalizedLabel?.toString() ?: ""
        return if (labelResId == 0) label else resolveInfo.loadLabel(context.packageManager)
            .toString()
    }

    fun getTimeUsageAgo(): String {
        val timeLastUsage = System.currentTimeMillis() - timeRecent
        val time = when {
            timeLastUsage < 60000 -> {
                DateUtils.longToDateString(timeLastUsage, "ss") + "s"
            }
            timeLastUsage < 3600000 -> {
                DateUtils.longToDateString(timeLastUsage, "mm") + "m"
            }
            else -> {
                DateUtils.longToDateString(timeLastUsage, "hh") + "h"
            }
        }
        return if (time == "00s") "Recently" else "$time ago"
    }
}
