package com.ezteam.windowslauncher.utils.center

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.ezteam.windowslauncher.R

object VibrateFilterUtil {
    fun detectVibrate(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                false
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                true
            }
            AudioManager.RINGER_MODE_NORMAL -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun clickFilter(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 23) {
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                showExplanation(
                    context.resources.getString(R.string.app_name),
                    context.getString(R.string.DESCRIBE_ACCESS_NOTIFICATION_POLICY), context
                )
            } else {
                when (am.ringerMode) {
                    AudioManager.RINGER_MODE_SILENT -> {
                        am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }
                    AudioManager.RINGER_MODE_NORMAL -> {
                        am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    }
                }
            }
        } else {
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> {
                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                }
                AudioManager.RINGER_MODE_VIBRATE -> {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
                AudioManager.RINGER_MODE_NORMAL -> {
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
            }
        }

    }

    fun longClickFilter(context: Context) {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showExplanation(title: String, message: String, context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
            .create()
        alertDialog.show()
    }

    fun getIconState(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> R.drawable.ic_mute
            AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_vibrate
            AudioManager.RINGER_MODE_NORMAL -> R.drawable.ic_ring
            else -> R.drawable.ic_mute
        }
    }

    fun getNameState(context: Context): String {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> context.getString(R.string.mute)
            AudioManager.RINGER_MODE_VIBRATE -> context.getString(R.string.vibrate)
            AudioManager.RINGER_MODE_NORMAL -> context.getString(R.string.ring)
            else -> context.getString(R.string.mute)
        }
    }
}