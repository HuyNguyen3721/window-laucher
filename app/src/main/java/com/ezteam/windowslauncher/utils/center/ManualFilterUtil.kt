package com.ezteam.windowslauncher.utils.center

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.ezteam.windowslauncher.BackGroundActivity
import com.ezteam.windowslauncher.R

object ManualFilterUtil {

    fun detectManual(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE, 0
        ) != 0
    }

    fun clickFilter(context: Context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.System.canWrite(context)) {
                showExplanation(
                    context.resources.getString(R.string.app_name),
                    context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING), context
                )
            } else {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    if (detectManual(context)) 0 else Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                )
            }
        } else {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (detectManual(context)) 0 else Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
        }
        sendIntentManual(context)
    }

    fun sendIntentManual(context: Context) {
        val intent = Intent(context.resources.getString(R.string.ACTION_MAMUAL))
        context.sendBroadcast(intent)
    }

    fun longClickFilter(context: Context) {
        val intent = Intent("android.settings.DISPLAY_SETTINGS")
        context.startActivity(intent)
    }

    private fun showExplanation(title: String, message: String, context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(context, BackGroundActivity::class.java)
                intent.putExtra("PERMISSION", "WRITE_SETTING")
                context.startActivity(intent)
            }
            .create()
        alertDialog.show()
    }
}