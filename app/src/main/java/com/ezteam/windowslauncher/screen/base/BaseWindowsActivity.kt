package com.ezteam.windowslauncher.screen.base

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.Html
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.extensions.infoDetail
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.dialog.InputNameDialog
import com.ezteam.windowslauncher.dialog.InputPasswordDialog
import com.ezteam.windowslauncher.model.LauncherModel
import java.io.File

abstract class BaseWindowsActivity<B : ViewBinding> : BaseActivity<B>(), IWindowsControl {
    private var launcher: LauncherModel? = null
    private var uninstallComplete: ((Boolean) -> Unit)? = null

    override fun openApp(launcherModel: LauncherModel) {
        if (launcherModel.password.isEmpty()) {
            val launchIntent: Intent? =
                packageManager.getLaunchIntentForPackage(launcherModel.packageName)
            launchIntent?.let {
                startActivity(it)
            }
        } else {
            showInputPassword {
                if (checkPassword(launcherModel, it)) {
                    val launchIntent: Intent? =
                        packageManager.getLaunchIntentForPackage(launcherModel.packageName)
                    launchIntent?.let {
                        startActivity(it)
                    }
                } else {
                    toast(resources.getString(R.string.incorrect_password))
                }
            }
        }
    }

    override fun openAppProperties(launcherModel: LauncherModel) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", launcherModel.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun openWallpaperSetting() {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        startActivity(Intent.createChooser(intent, "Select Wallpaper"))
    }

    override fun uninstallApp(launcherModel: LauncherModel, complete: (Boolean) -> Unit) {
        launcher = launcherModel
        uninstallComplete = complete
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:${launcherModel.packageName}")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun checkAppUninstall() {
        launcher?.let {
            uninstallComplete?.invoke(!isPackageInstalled(it.packageName, packageManager))
            launcher = null
            uninstallComplete = null
        }
    }

    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun showInputPassword(complete: (String) -> Unit) {
        val dialog = InputPasswordDialog.ExtendBuilder()
            .setTitle(resources.getString(R.string.password_title))
            .onSetPositiveButton(resources.getString(R.string.save)) { _, data ->
                data[InputPasswordDialog.INPUT_PASSWORD]?.let { name ->
                    complete(name as String)
                }
            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) {}
            .build()
        dialog.show(supportFragmentManager, InputPasswordDialog::javaClass.name)
    }

    private fun checkPassword(launcher: LauncherModel, password: String): Boolean {
        return launcher.password == password
    }

    override fun showPropertiesFile(file: File) {
        MaterialDialog(this)
            .cancelable(false)
            .title(text = resources.getString(R.string.properties))
            .message(text = Html.fromHtml(file.infoDetail))
            .positiveButton(0, resources.getString(R.string.ok)) {
                it.dismiss()
            }
            .show()
    }

    override fun showInputFolderName(created: Boolean, complete: (String) -> Unit) {
        val dialog = InputNameDialog.ExtendBuilder()
            .setTitle(
                if (created) resources.getString(R.string.new_folder) else
                    resources.getString(R.string.rename)
            )
            .onSetPositiveButton(resources.getString(R.string.save)) { _, data ->
                data[InputNameDialog.INPUT_NAME]?.let { name ->
                    complete(name as String)
                }
            }
            .onSetNegativeButton(resources.getString(R.string.cancel)) {}
            .build()
        dialog.show(
            supportFragmentManager, InputNameDialog::javaClass.name
        )
    }

    override fun onResume() {
        super.onResume()
        checkAppUninstall()
    }
}