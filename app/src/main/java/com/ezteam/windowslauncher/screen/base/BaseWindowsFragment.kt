package com.ezteam.windowslauncher.screen.base

import androidx.viewbinding.ViewBinding
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.windowslauncher.model.LauncherModel
import java.io.File

abstract class BaseWindowsFragment<B : ViewBinding> : BaseFragment<B>(), IWindowsControl {
    override fun openApp(launcherModel: LauncherModel) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.openApp(launcherModel)
            }
        }
    }

    override fun openAppProperties(launcherModel: LauncherModel) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.openAppProperties(launcherModel)
            }
        }
    }

    override fun openWallpaperSetting() {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.openWallpaperSetting()
            }
        }
    }

    override fun showInputPassword(complete: (String) -> Unit) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.showInputPassword(complete)
            }
        }
    }

    override fun uninstallApp(launcherModel: LauncherModel, complete: (Boolean) -> Unit) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.uninstallApp(launcherModel, complete)
            }
        }
    }

    override fun showPropertiesFile(file: File) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.showPropertiesFile(file)
            }
        }
    }

    override fun showInputFolderName(created: Boolean, complete: (String) -> Unit) {
        activity?.let {
            if (it is BaseWindowsActivity<*>) {
                it.showInputFolderName(created, complete)
            }
        }
    }
}