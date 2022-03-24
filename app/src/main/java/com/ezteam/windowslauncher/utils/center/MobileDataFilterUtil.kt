package com.ezteam.windowslauncher.utils.center

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.service.MyAccessibilityService
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_DISABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.CLICKABLE_ENABLE
import com.ezteam.windowslauncher.utils.center.BaseAccessiblilityUtil.NOT_CLICKABLE
import java.lang.reflect.Method

object MobileDataFilterUtil {
    private val LABEL_MOBLIE_DATA = "quick_settings_mobile_data_detail_title"

    fun detectMobileData(context: Context): Boolean {
        var mobileDataEnabled = false
        val cm: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true
            mobileDataEnabled = method.invoke(cm) as Boolean
        } catch (e: Exception) {

        }
        return mobileDataEnabled
    }

    fun setEnable(context: Context, nodeInfo: AccessibilityNodeInfo?): Int {
        if (nodeInfo == null) {
            return NOT_CLICKABLE
        }

        val firstStage = detectMobileData(context)

        var isClickSuccess = false
        var getList =
            nodeInfo.findAccessibilityNodeInfosByText(
                BaseAccessiblilityUtil.getStringByName(
                    context,
                    LABEL_MOBLIE_DATA
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

    fun clickFilter(context: Context) {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (AirPlaneFilterUtil.detectAirPlane(context)) {
            showExplanation(
                context.resources.getString(R.string.app_name),
                context.getString(R.string.describe_airplane_mode),
                context
            )
        } else {
            if (telephonyManager?.simState != TelephonyManager.SIM_STATE_READY) {
                showExplanation(
                    context.resources.getString(R.string.no_sim_card),
                    context.getString(R.string.describe_no_sim_card),
                    context
                )
            } else {
                val enableAccessibilityKey =
                    PreferencesUtils.getBoolean(AccessibilityKey.IS_RUNNING, false)
                if (enableAccessibilityKey && MyAccessibilityService.instance != null) {
                    MyAccessibilityService.instance?.actionAutoClick(MyAccessibilityService.DoingAction.MobileData)
                } else {
                    AccessibilityKey.startAccessibilitySetting(context)
                }
            }
        }
        sendIntentMobileData(context)
    }

    fun longClickFilter(context: Context) {
        settingMobileData(context)
    }

    fun sendIntentMobileData(context: Context) {
        val intent = Intent(context.resources.getString(R.string.ACTION_MOBILE_DATA))
        context.sendBroadcast(intent)
    }

    fun settingMobileData(context: Context) {
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            intent.component = ComponentName(
                "com.android.phone", "com.android.phone.settings.MobileNetworkSettings"
            )
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                intent.component = ComponentName(
                    "com.android.phone", "com.android.phone.MobileNetworkSettings"
                )
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    intent.component = ComponentName(
                        "com.android.phone", "com.android.phone.MSimMobileNetworkSettings"
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        if (Build.VERSION.SDK_INT < 28) {
                            intent.component = ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings\$DataUsageSummaryActivity"
                            )
                            context.startActivity(intent)

                        } else {
                            intent.component = ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings\$NetworkDashboardActivity"
                            )
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        intent.action = "android.settings.DATA_USAGE_SETTINGS"
                        context.startActivity(intent)
                    }
                }
            }
        }
    }


    private fun showExplanation(title: String, message: String, context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ ->
            }
            .create()
        alertDialog.show()
    }
}
