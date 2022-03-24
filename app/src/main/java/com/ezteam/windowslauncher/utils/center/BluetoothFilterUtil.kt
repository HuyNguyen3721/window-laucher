package com.ezteam.windowslauncher.utils.center

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.R

object BluetoothFilterUtil {

    fun detectBluetooth(context: Context): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return when {
            mBluetoothAdapter == null -> {
                false
            }
            mBluetoothAdapter.isEnabled -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun clickFilter(context: Context) {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            showExplanation(
                context.resources.getString(R.string.windows_launcher),
                context.getString(R.string.device_does_not_support_bluetooth), context
            )
        } else {
            if (detectBluetooth(context)) {
                mBluetoothAdapter.disable()
            } else {
                mBluetoothAdapter.enable()
            }
        }
    }

    fun longClickFilter(context: Context) {
        val intent = Intent()
        intent.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        context.startActivity(intent)
    }

    private fun showExplanation(title: String, message: String, context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ ->
            }
            .create()
        alertDialog.show()
    }
}
