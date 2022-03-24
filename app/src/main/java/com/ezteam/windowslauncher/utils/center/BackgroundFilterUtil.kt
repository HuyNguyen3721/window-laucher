package com.ezteam.windowslauncher.utils.center

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object BackgroundFilterUtil {
    const val CODE_SELECT_PICKTURE = 7
    fun checkPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun clickFilter(activityCompat: AppCompatActivity) {
        try {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityCompat.startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                CODE_SELECT_PICKTURE
            )
        } catch (e: Exception) {
        }
    }

}