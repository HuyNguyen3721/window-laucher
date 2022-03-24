package com.ezteam.windowslauncher.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterNotification
import com.ezteam.windowslauncher.model.ItemNotification
import com.ezteam.windowslauncher.utils.Config

class BroadCastListenerNotification : BroadcastReceiver() {
    lateinit var listDataNotification: MutableList<ItemNotification>
    lateinit var adapter: AdapterNotification
    var listener: (() -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            context?.let { c ->
                if (it.action == c.getString(R.string.ACTION_NOTIFICATION)) {
                    it.setExtrasClassLoader(ItemNotification::class.java.classLoader)
                    val data = it.getParcelableExtra<ItemNotification>("ITEM")
                    if (data != null) {
                        var check = false
                        if (data.time != -1L) {
                            if (data.title == null) {
                                check = true
                            } else {
                                for (item in listDataNotification) {
                                    if (item.key == data.key) {
                                        if (item.time != data.time) {
                                            item.message = "${item.message}\n${data.message}"
                                            adapter.notifyItemChanged(
                                                listDataNotification.indexOf(
                                                    item
                                                )
                                            )
                                        }
                                        check = true
                                        break
                                    }
                                }
                            }

                            if (!check) {
                                if (listDataNotification.size > 0) {
                                    for (i in (listDataNotification.size - 1) downTo 0) {
                                        val item = listDataNotification[i]
                                        if (item.packageName == data.packageName) {
                                            data.isParent = false
                                            listDataNotification.add(i + 1, data)
                                            adapter.notifyItemInserted(i + 1)
                                            break
                                        }
                                    }
                                }
                                //
                                if (data.isParent) {
                                    listDataNotification.add(data)
                                    adapter.notifyItemInserted(listDataNotification.size - 1)
                                }
                            }
                        } else {
                            for (item in listDataNotification) {
                                if (item.key == data.key) {
                                    adapter.removeSingleItem(item)
                                    break
                                }
                            }
                        }
                    }
                    listener?.invoke()
                } else if (it.action == Config.ACTION_RELOAD) {
                    listDataNotification.clear()
                    c.sendBroadcast(Intent(c.getString(R.string.ACTION_NOTIFICATION_SERVICE)))
                } else {

                }
            }
        }
    }
}