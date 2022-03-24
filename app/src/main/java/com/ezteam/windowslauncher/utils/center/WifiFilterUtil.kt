package com.ezteam.windowslauncher.utils.center

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.service.MyAccessibilityService
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_DISABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_ENABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.NOT_CLICKABLE

object WifiFilterUtil {
    private val LABEL_WIFI = "quick_settings_wifi_label"

    fun detectWifi(context: Context): Boolean {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifi?.isWifiEnabled ?: false
    }

    fun clickFilter(context: Context) {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val isEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            if (isEnabled) {
                longClickFilter(context)
            } else {
                wifiManager.isWifiEnabled = !detectWifi(context)
            }
        } else {
            val enableAccessibilityKey =
                PreferencesUtils.getBoolean(AccessibilityKey.IS_RUNNING, false)
            if (enableAccessibilityKey && MyAccessibilityService.instance != null) {
                MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.Wifi)
            } else {
                AccessibilityKey.startAccessibilitySetting(context)
            }
        }
    }

    fun longClickFilter(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        ContextCompat.startActivity(context, intent, null)
    }

    fun settingWifi(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, intent, null)
    }

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = detectWifi(context)

        var isClickSuccess = false
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(
                BaseAccessiblilityUtil.getStringByName(
                    context,
                    LABEL_WIFI
                )
            )

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
