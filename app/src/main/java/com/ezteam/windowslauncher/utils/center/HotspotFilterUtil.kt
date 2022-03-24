package com.ezteam.windowslauncher.utils.center

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.service.MyAccessibilityService
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_DISABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_ENABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.NOT_CLICKABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.getStringByName

object HotspotFilterUtil {
    private val LABEL_HOTSPOT = "quick_settings_hotspot_label"

    fun detectHotspot(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method = wifiManager.javaClass.getMethod("getWifiApState")
        val tmp = method.invoke(wifiManager) as Int
        return tmp == 13
    }

    fun clickFilter(context: Context) {
        val enableAccessibilityKey = PreferencesUtils.getBoolean(AccessibilityKey.IS_RUNNING, false)
        if (enableAccessibilityKey && MyAccessibilityService.instance != null) {
            MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.Hotspot)
        } else {
            AccessibilityKey.startAccessibilitySetting(context)
        }
    }

    fun sendIntentHotspot(context: Context) {
        val intent = Intent(context.resources.getString(R.string.ACTION_HOTSPOT))
        context.sendBroadcast(intent)
    }

    fun longClickFilter(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = cn
        context.startActivity(intent)
    }

    fun settingHotpot(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = cn
        context.startActivity(intent)
    }

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = detectHotspot(context)

        var isClickSuccess = false
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(
                getStringByName(
                    context,
                    LABEL_HOTSPOT
                )
            )
        if (getList.isNullOrEmpty()) {
            getList = nodeInfo
                .findAccessibilityNodeInfosByText("Hotspot")
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

}