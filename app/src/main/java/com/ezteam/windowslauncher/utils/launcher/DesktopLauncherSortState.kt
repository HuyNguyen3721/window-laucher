package com.ezteam.windowslauncher.utils.launcher

enum class DesktopLauncherSortState(var value: Int) {
    NAME(1),
    NAME_DESC(2),
    TYPE(3),
    TYPE_DESC(4),
    DATE(5),
    DATE_DESC(6),
    POSITION(7);

    companion object {
        fun getSortState(value: Int): DesktopLauncherSortState {
            return when (value) {
                1 -> NAME
                2 -> NAME_DESC
                3 -> TYPE
                4 -> TYPE_DESC
                5 -> DATE
                6 -> DATE_DESC
                7 -> POSITION
                else -> NAME
            }
        }
    }
}