package com.ezteam.windowslauncher.utils.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.ScreenModel
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.extensions.getSizeGrid

object LauncherAppUtils {
    const val PORTRAIT_COLUMNS_MIN = 4
    const val PORTRAIT_COLUMNS_MAX = 7
    const val PORTRAIT_ROWS_MIN = 4
    const val PORTRAIT_ROWS_MAX = 9
    const val LANDSCAPE_COLUMNS_MIN = 4
    const val LANDSCAPE_COLUMNS_MAX = 10
    const val LANDSCAPE_ROWS_MIN = 3
    const val LANDSCAPE_ROWS_MAX = 5

    fun rePrioritize(launcherApps: List<LauncherModel>, context: Context) {
        launcherApps.forEach {
            it.prioritize = prioritizeRank(it.packageName, context)
        }
    }

    private fun prioritizeRank(packageName: String, context: Context): Int {
        return when {
            getPackagesOfDialerApps(context).contains(packageName) -> 4
            getPackagesOfMessageApps(context).contains(packageName) -> 3
            getPackagesOfCameraApps(context).contains(packageName) -> 2
            mutableListOf("com.android.settings",
                "com.android.gallery",
                "com.android.gallery3d",
                "com.coloros.gallery3d",
                "com.android.chrome").contains(packageName) -> 2
            mutableListOf("com.google.android.calendar",
                "com.android.email",
                "com.facebook.katana",
                "com.facebook.orca",
                "com.instagram.android",
                "org.telegram.messenger",
                "com.twitter.android").contains(packageName) -> 1
            else -> 0
        }
    }

    fun getPackagesOfDialerApps(context: Context): List<String> {
        val packageNames: MutableList<String> = ArrayList()

        // Declare action which target application listen to initiate phone call
        val intent = Intent()
        intent.action = Intent.ACTION_DIAL
        // Query for all those applications
        val resolveInfos: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, 0)
        // Read package name of all those applications
        for (resolveInfo in resolveInfos) {
            val activityInfo: ActivityInfo = resolveInfo.activityInfo
            packageNames.add(activityInfo.applicationInfo.packageName)
        }
        Log.e("Dialer", packageNames.toString())

        return packageNames.filter {
            it.contains("dialer") || it.contains("contacts")
        }
    }

    fun getPackagesOfCameraApps(context: Context): List<String> {
        val packageNames: MutableList<String> = ArrayList()

        // Declare action which target application listen to initiate phone call
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        // Query for all those applications
        val resolveInfos: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, 0)
        // Read package name of all those applications
        for (resolveInfo in resolveInfos) {
            val activityInfo: ActivityInfo = resolveInfo.activityInfo
            packageNames.add(activityInfo.applicationInfo.packageName)
        }
        Log.e("Camera", packageNames.toString())
        return packageNames
    }

    fun getPackagesOfMessageApps(context: Context): List<String> {
        val packageNames: MutableList<String> = ArrayList()
        val defApp: String? =
            Telephony.Sms.getDefaultSmsPackage(context)
        defApp?.let {
            packageNames.add(defApp)
        }
        // Declare action which target application listen to initiate phone call
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_DEFAULT).setType("vnd.android-dir/mms-sms")
        // Query for all those applications
        val resolveInfos: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, 0)
        // Read package name of all those applications
        for (resolveInfo in resolveInfos) {
            val activityInfo: ActivityInfo = resolveInfo.activityInfo
            packageNames.add(activityInfo.applicationInfo.packageName)
        }
        Log.e("Message", packageNames.toString())
        return packageNames
    }

    fun configFlagShowHeader(launcherApps: List<LauncherModel>) {
        val headers = listHeader(launcherApps)
        headers.forEach { header ->
            launcherApps.first {
                it.appName.startsWith(header)
            }.let {
                it.showHeader = true
            }
        }
    }

    fun listHeader(launcherApps: List<LauncherModel>): List<String> {
        return launcherApps.sortedBy {
            it.appName
        }.map {
            return@map if (it.appName.isEmpty()) "" else it.appName[0].toString()
        }.distinct()
    }

    fun listHeaderSearch(): List<String> {
        val headers: MutableList<String> = mutableListOf()
        headers.add("#")
        var c: Char = 'A'
        for (index in 0..25) {
            headers.add(c++ + "")
        }
        return headers
    }

    fun getSizeGrid(context: Context): ScreenModel {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val columns = PreferencesUtils.getInteger(PresKey.PORTRAIT_COLUMNS, context.getSizeGrid().columns)
            val rows = PreferencesUtils.getInteger(PresKey.PORTRAIT_ROWS, context.getSizeGrid().rows)
            ScreenModel(columns, rows)
        } else {
            val columns = PreferencesUtils.getInteger(PresKey.LANDSCAPE_COLUMNS, context.getSizeGrid().columns)
            val rows = PreferencesUtils.getInteger(PresKey.LANDSCAPE_ROWS, context.getSizeGrid().rows)
            ScreenModel(columns, rows)
        }
    }
}