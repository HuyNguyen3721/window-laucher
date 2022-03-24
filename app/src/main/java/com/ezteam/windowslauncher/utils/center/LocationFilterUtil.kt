package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.service.MyAccessibilityService
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_DISABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_ENABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.NOT_CLICKABLE

object LocationFilterUtil {
    private val LABEL_LOCATION = "quick_settings_location_label"
    private val LABEL_LOCATION_HUAWEI = "location_access"

    fun detectLocation(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = detectLocation(context)

        var isClickSuccess = false
        var label = BaseAccessiblilityUtil.getStringByName(context, LABEL_LOCATION)
        //
        var getList = nodeInfo
            .findAccessibilityNodeInfosByText(label)
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText(
                    BaseAccessiblilityUtil.getStringByName(
                        context,
                        LABEL_LOCATION_HUAWEI
                    )
                )
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Location")
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("GPS")
        }
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Vị trí")
        }
        if (getList.isNullOrEmpty()) {
            val labels = PreferencesUtils.getString(
                "${context.resources.getString(R.string.location)} - Name",
                ""
            )
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }
        if (!getList.isNullOrEmpty()) {
            for (child in getList) {
                if (child != null) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    child.recycle()
                    isClickSuccess = true
                }
            }
        }
        return if (isClickSuccess) {
            if (firstStage) CLICKABLE_DISABLE else CLICKABLE_ENABLE
        } else {
            NOT_CLICKABLE
        }
    }


    fun clickFilter(context: Context) {
        val enableAccessibilityKey = PreferencesUtils.getBoolean(AccessibilityKey.IS_RUNNING, false)
        if (enableAccessibilityKey && MyAccessibilityService.instance != null) {
            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.Location)
        } else {
            AccessibilityKey.startAccessibilitySetting(context)
        }
    }

    fun sendIntentLocation(context: Context) {
        val intent = Intent(context.resources.getString(R.string.ACTION_LOCATION))
        context.sendBroadcast(intent)
    }

    fun longClickFilter(context: Context) {
        val intent = Intent("android.settings.LOCATION_SOURCE_SETTINGS")
        context.startActivity(intent)
    }

    fun settingLocation(context: Context) {
        val intent = Intent("android.settings.LOCATION_SOURCE_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}