package com.ezteam.windowslauncher.utils

import android.content.Context
import android.os.Environment
import com.ezteam.windowslauncher.R
import java.io.File

object Config {
    const val ACTION_RELOAD = "ACTION_RELOAD"
    const val NEWSPAPER_API_KEY = "186ef92e85104524b1e0a920b7968498"

    object Preferences {
        const val folderIsGridView = "folder is gridview"
        const val initDataQuickAccess = "init data quick access"
    }

    object Notification {
        const val ActionClickNotification = "action click notification"
    }

    object FileManager {
        const val thisPCPath = "This_pc_path"
        const val thisUserPath = "This_user_path"
        const val thisFtpPath = "This_ftp_path"
        const val thisLanPath = "This_lan_path"
        const val thisQuickAccessPath = "This_quick_access_path"
        val externalStoragePath: String? = Environment.getExternalStorageDirectory().path
        val documentPath: String? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path
        val downloadPath: String? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        val picturePath: String? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
        val videoPath: String? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path
        val desktopPath = fun(context: Context): String {
            val folder =
                File(context.filesDir.parent + File.separator + context.getString(R.string.desktop))
            if (!folder.exists()) {
                folder.mkdir()
            }
            return folder.path
        }

        var isPrimaryFolder = fun(path: String, context: Context): Boolean {
            return (path == documentPath ||
                    path == downloadPath ||
                    path == picturePath ||
                    path == desktopPath(context) ||
                    path == videoPath)
        }
    }
}