package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.service.NotificationListener

class BroadCastNotificationManager : BroadcastReceiver() {
    lateinit var notificationListenerService: NotificationListener
    private val key = "KEY"
    private val ID = "ID"
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            context?.let { c ->
                if (it.action == c.resources.getString(R.string.ACTION_NOTIFICATION_SERVICE)) {
                    notificationListenerService.getNotification()
                } else if (it.action == c.resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_ALL)) {
                    notificationListenerService.clearAll()
//                    notificationListenerService.getNotification()
                } else if (it.action == c.resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_FOR_KEY)) {
                    notificationListenerService.clearNotificationForKey(
                        it.getStringExtra(key) ?: "", it.getIntExtra(ID, -100)
                    )
                }
            }
        }
    }
}