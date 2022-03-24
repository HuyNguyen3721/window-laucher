package com.ezteam.windowslauncher.service

class SingleSettingStage {
    var isEnableFightMode = false
    var isEnableAutoRotate = false
    var isEnableAirPlane = false
    var isEnableHotspot = false
    var isWifi = false
    var isMobileData = false
    var isEnableLocation = false

    private object Holder {
        val INSTANCE = SingleSettingStage()
    }

    fun cleanAll() {
        isEnableFightMode = false
        isEnableAutoRotate = false
        isEnableHotspot = false
        isEnableLocation = false
        isEnableAirPlane = false
        isWifi = false
        isMobileData = false
    }

    companion object {
        @JvmStatic
        fun getInstance(): SingleSettingStage {
            return Holder.INSTANCE
        }
    }
}