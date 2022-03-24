package com.ezteam.windowslauncher.service

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.broadcast.BroadCastNotificationManager
import com.ezteam.windowslauncher.model.ItemNotification
import com.ezteam.windowslauncher.screen.MainActivity

class NotificationListener : NotificationListenerService() {
    private var connected = false
    private val broadCastNotificationListenerService = BroadCastNotificationManager()
    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null) {
            sbn.postTime
            val itemDataNotification = ItemNotification(
                sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                sbn.postTime,
                sbn.packageName,
                sbn.key,
                sbn.id,
                sbn.notification.contentIntent,
                sbn.isClearable
            )
            if (itemDataNotification.resId != 0) {
                val intent = Intent(resources.getString(R.string.ACTION_NOTIFICATION))
                intent.putExtra("ITEM", itemDataNotification)
                sendBroadcast(intent)
            }
        }


    }

    override fun onListenerConnected() {
        super.onListenerConnected()
//        backMainActivity
        backMainActivity()
        //
        connected = true
        broadCastNotificationListenerService.notificationListenerService = this
        val intent = IntentFilter(resources.getString(R.string.ACTION_NOTIFICATION_SERVICE))
        intent.addAction(resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_ALL))
        intent.addAction(resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_FOR_KEY))
        registerReceiver(broadCastNotificationListenerService, intent)
        getNotification()
    }

    @SuppressLint("SimpleDateFormat")
    fun getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && connected) {
            try {
                for (sbn in this.activeNotifications) {
                    val itemDataNotification = ItemNotification(
                        sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                        sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                        sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                        sbn.postTime,
                        sbn.packageName,
                        sbn.key,
                        sbn.id,
                        sbn.notification.contentIntent,
                        sbn.isClearable
                    )
                    val intent = Intent(resources.getString(R.string.ACTION_NOTIFICATION))
                    intent.putExtra("ITEM", itemDataNotification)
                    sendBroadcast(intent)
                }
            } catch (e: Exception) {
                //
            }
        }
    }

    private fun backMainActivity() {
        Log.d("Huy", "backMainActivity: ")
        if (MainActivity.isNotifyPermission) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun clearAll() {
        this.cancelAllNotifications()
    }

    fun clearNotificationForKey(key: String, id: Int) {
//        val notificationManager: NotificationManager =
//            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(id)
        if (connected) {
            this.cancelNotification(key)
        }
//        sendBroadcast(Intent(resources.getString(R.string.ACTION_NOTIFICATION_SERVICE)))
    }

    override fun onListenerDisconnected() {
        try {
            connected = false
            super.onListenerDisconnected()
            unregisterReceiver(broadCastNotificationListenerService)
        } catch (e: IllegalArgumentException) {
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn != null) {
            val itemDataNotification = ItemNotification(
                sbn.notification.extras.getInt(Notification.EXTRA_SMALL_ICON),
                sbn.notification.extras.getString(Notification.EXTRA_TITLE),
                sbn.notification.extras.getString(Notification.EXTRA_TEXT),
                -1,
                sbn.packageName,
                sbn.key,
                sbn.id,
                sbn.notification.contentIntent,
                sbn.isClearable
            )
//            sbn.notification.channelId
            val intent = Intent(resources.getString(R.string.ACTION_NOTIFICATION))
            intent.putExtra("ITEM", itemDataNotification)
            sendBroadcast(intent)
        }
    }
}