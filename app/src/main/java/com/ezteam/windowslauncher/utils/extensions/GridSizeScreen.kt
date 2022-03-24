package com.ezteam.windowslauncher.utils.extensions

import android.content.Context
import android.util.Log
import com.ezteam.baseproject.extensions.getDisplayMetrics
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.model.ScreenModel

enum class LauncherSize(var size: Int) {
    SMALL(0),
    MEDIUM(1),
    LARGE(2)
}

fun Context.getSizeGrid(): ScreenModel {
    val orientation = resources.configuration.orientation
    val isTablet = resources.getBoolean(R.bool.isTablet)

    if (orientation == 1) {
        return when {
            getDisplayMetrics().heightPixels <= 1080 -> {
                ScreenModel(if (isTablet) 5 else 4, if (isTablet) 5 else 5)
            }

            getDisplayMetrics().heightPixels <= 1920 -> {
                ScreenModel(if (isTablet) 6 else 4, if (isTablet) 6 else 5)
            }

            else -> {
                val isDeviceTall =
                    (getDisplayMetrics().heightPixels.toFloat() / getDisplayMetrics().widthPixels.toFloat()) > (16.0f / 9.0f)
                ScreenModel(
                    if (isTablet) 6 else 5,
                    if (isDeviceTall) 8 else 7
                )
            }
        }
    } else {
        return when {
            getDisplayMetrics().widthPixels <= 1080 -> {
                ScreenModel(if (isTablet) 6 else 5, if (isTablet) 4 else 4)
            }

            getDisplayMetrics().widthPixels <= 1920 -> {
                ScreenModel(if (isTablet) 7 else 6, if (isTablet) 4 else 3)
            }

            else -> {
                val isDeviceTall =
                    (getDisplayMetrics().widthPixels.toFloat() / getDisplayMetrics().heightPixels.toFloat()) > (16.0f / 9.0f)
                ScreenModel(
                    if (isDeviceTall) 9 else 8,
                    if (isTablet) 4 else 3
                )
            }
        }
    }
}
