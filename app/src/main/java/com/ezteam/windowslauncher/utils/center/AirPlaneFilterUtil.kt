package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.service.MyAccessibilityService
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_DISABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_ENABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.NOT_CLICKABLE

object AirPlaneFilterUtil {

    const val LABEL_AIR_PLANE = "quick_settings_flight_mode_detail_title"
    const val LABEL_AIR_PLANE_SS_LOW = "quick_settings_airplane_mode_label"
    const val LABEL_AIR_PLANE_HUAWEI = "airplane_mode"

    //

    fun detectAirPlane(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = detectAirPlane(context)

        var isClickSuccess = false
        var labels = BaseAccessiblilityUtil.getStringByName(context, LABEL_AIR_PLANE)
        var index = labels.indexOf("\n")
        if (index != -1) {
            labels = labels.substring(0, index)
        }
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(labels)
        //
        if (getList.isNullOrEmpty()) {
            labels = BaseAccessiblilityUtil.getStringByName(
                context,
                LABEL_AIR_PLANE_SS_LOW
            )
            index = labels.indexOf("\n")
            if (index != -1) {
                labels = labels.substring(index + 1, labels.length)
            }
            getList =
                nodeInfo.findAccessibilityNodeInfosByText(labels)
        }

        if (getList.isNullOrEmpty()) {
            labels = BaseAccessiblilityUtil.getStringByName(
                context,
                LABEL_AIR_PLANE_HUAWEI
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
            Log.d("Huy", "clickFilter : AirPlane  ${MyAccessibilityService.instance}")
            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.AirPlane)
        } else {
            AccessibilityKey.startAccessibilitySetting(context)
        }
    }

    fun longClickFilter(context: Context) {
        val intent = Intent("android.settings.AIRPLANE_MODE_SETTINGS")
        context.startActivity(intent)
    }

    fun settingAirplane(context: Context) {
        val intent = Intent("android.settings.AIRPLANE_MODE_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}
