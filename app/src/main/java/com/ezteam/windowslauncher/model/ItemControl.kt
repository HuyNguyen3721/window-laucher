package com.ezteam.windowslauncher.model

data class ItemControl(
    var resId: Int,
    var name: String,
    var isEnable: Boolean = false,
    var isClickEnable: Boolean = false
)