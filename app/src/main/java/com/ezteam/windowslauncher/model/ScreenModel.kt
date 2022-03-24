package com.ezteam.windowslauncher.model

data class ScreenModel(
    var columns: Int,
    var rows: Int
) {
    fun getSize(): Int {
        return columns * rows
    }
}