package com.ezteam.windowslauncher.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.screen.MainActivity
import com.ezteam.windowslauncher.utils.AccessibilityKey
import com.ezteam.windowslauncher.utils.center.*
import com.ezteam.windowslauncher.utils.center.AirPlaneFilterUtil.settingAirplane
import com.ezteam.windowslauncher.utils.center.WifiFilterUtil.settingWifi
import com.ezteam.windowslauncher.windowmanager.MyWindowManager

class MyAccessibilityService : AccessibilityService() {
    private lateinit var myWindowManager: MyWindowManager

    enum class DoingAction {
        None, AirPlane, AutoRotate, Hotspot, Location, Wifi, MobileData
    }

    var doing = DoingAction.None

    companion object {
        @JvmStatic
        var instance: MyAccessibilityService? = null
    }

    fun actionAutoClick(doingAction: DoingAction) {
        StatusBarAutomatic.expandStatusBar(this)
        Handler().postDelayed({
            doing = doingAction
        }, 300) /*time delay show status bar*/
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        PreferencesUtils.putBoolean(AccessibilityKey.IS_RUNNING, true)
        instance ?: let {
            instance = this
        }
        initView()
        backMainActivity()
    }

    private fun backMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun initView() {
        myWindowManager = MyWindowManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null) {
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.eventType
            ) {
                var nodeInfo: AccessibilityNodeInfo? = null
                try {
                    nodeInfo = event.source ?: return
                } catch (e: NullPointerException) {
                    return
                }
                Handler().postDelayed({
                    doing = DoingAction.None
                }, 1000)
                when (doing) {
                    DoingAction.AirPlane -> {
                        val isEnable: Int = AirPlaneFilterUtil.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableAirPlane = isEnable == 1
                        } else {
                            settingAirplane(this)
                        }
                        doing = DoingAction.None
                    }
                    DoingAction.Wifi -> {
                        val isEnable: Int = WifiFilterUtil.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isWifi = isEnable == 1
                        } else {
                            settingWifi(this)
                        }
                        doing = DoingAction.None
                    }
                    DoingAction.MobileData -> {
                        val isEnable: Int = MobileDataFilterUtil.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isMobileData = isEnable == 1
                        } else {
                            MobileDataFilterUtil.settingMobileData(this)
                        }
                        doing = DoingAction.None
                    }
                    DoingAction.AutoRotate -> {

                    }
                    DoingAction.Hotspot -> {
                        val isEnable: Int = HotspotFilterUtil.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableHotspot = isEnable == 1
                        } else {
                            HotspotFilterUtil.settingHotpot(this)
                        }
                        doing = DoingAction.None
                    }
                    DoingAction.Location -> {
                        val isEnable: Int = LocationFilterUtil.setEnable(this, nodeInfo)
                        if (isEnable != 0) {
                            SingleSettingStage.getInstance().isEnableLocation = isEnable == 1
                        } else {
                            LocationFilterUtil.settingLocation(this)
                        }
                        doing = DoingAction.None
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
//        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        Log.d("Huy", "onDestroy : ")
        super.onDestroy()
        PreferencesUtils.putBoolean(AccessibilityKey.IS_RUNNING, false)
        instance = null
    }

    private fun endClick(action: Boolean? = null) {
//        myWindowManager.updateViewUseAccessibility(doing, action)
        doing = DoingAction.None
    }

}