package com.ezteam.windowslauncher.screen.taskbar

import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.SpecialPackage
import com.ezteam.windowslauncher.utils.launcher.LauncherType

object TaskBarData {
    fun getItemOriginal(): MutableList<LauncherModel> {
        return mutableListOf(
            LauncherModel(
                packageName = SpecialPackage.WINDOWS_PACKAGE.packageId,
                logoResId = R.drawable.ic_microsoft_windows,
                launcherType = LauncherType.APP_DESKTOP.value
            ),
            LauncherModel(
                packageName = SpecialPackage.SEARCH_PACKAGE.packageId,
                logoResId = R.drawable.ic_search,
                launcherType = LauncherType.APP_DESKTOP.value
            ),
            LauncherModel(
                packageName = SpecialPackage.WIDGETS_PACKAGE.packageId,
                logoResId = R.drawable.ic_widgets,
                launcherType = LauncherType.APP_DESKTOP.value
            )
        )
    }
}