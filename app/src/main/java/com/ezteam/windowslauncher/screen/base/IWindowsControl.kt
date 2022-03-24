package com.ezteam.windowslauncher.screen.base

import com.ezteam.windowslauncher.model.LauncherModel
import java.io.File

interface IWindowsControl {
    fun openApp(launcherModel: LauncherModel)

    fun openAppProperties(launcherModel: LauncherModel)

    fun uninstallApp(launcherModel: LauncherModel, complete: (Boolean) -> Unit)

    fun openWallpaperSetting()

    fun showInputPassword(complete: (String) -> Unit)

    fun showPropertiesFile(file: File)

    fun showInputFolderName(created: Boolean = true, complete: (String) -> Unit)
}