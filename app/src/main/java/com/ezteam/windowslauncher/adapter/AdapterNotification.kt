package com.ezteam.windowslauncher.adapter

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutTypeOneViewholderBinding
import com.ezteam.windowslauncher.model.ItemNotification
import java.text.SimpleDateFormat
import java.util.*

class AdapterNotification(
    var lists: MutableList<ItemNotification>,
    val context: Context,
    var listener: (() -> Unit)? = null,
    var listenerRemoveItem: ((ItemNotification) -> Unit)? = null,
    var listenerMore: ((Int) -> Unit)? = null
) : BaseRecyclerAdapter<ItemNotification, AdapterNotification.TypeOneViewHolder>(context, lists) {

    private val key = "KEY"
    private val ID = "ID"
    private val animClickIcon =
        AnimationUtils.loadAnimation(context, R.anim.anim_click_icon)

    inner class TypeOneViewHolder(var bindingTypeOne: LayoutTypeOneViewholderBinding) :
        RecyclerView.ViewHolder(bindingTypeOne.root) {
        @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
        fun bindData(item: ItemNotification) {
            bindingTypeOne.contentView.isVisible = !item.isAds
            bindingTypeOne.adsView.isVisible = item.isAds

            if (item.isAds) {
//                adsView?.let {
//                    if (it.parent != null) {
//                        (it.parent as ViewGroup).removeView(it)
//                    }
//                    bindingTypeOne.adsView.addView(it)
//                }

                Log.d("LoadAds", "Main$adapterPosition")
                return
            }

            if (!item.isParent) {
                bindingTypeOne.layoutAppMarket.visibility = View.GONE
            } else {
                bindingTypeOne.layoutAppMarket.visibility = View.VISIBLE
                val anim = AnimationUtils.loadAnimation(context, R.anim.anim_hide_view)
                bindingTypeOne.layoutAppMarket.startAnimation(anim)
            }
            var iconApp: Drawable? = null
            try {
                iconApp = context.packageManager.getApplicationIcon(item.packageName ?: "")
            } catch (e: PackageManager.NameNotFoundException) {
            }
            var icon: Drawable? = null
            icon = if (item.resId != 0) {
                val packageContext =
                    bindingTypeOne.icNotification.context.createPackageContext(item.packageName, 0)
                try {
                    packageContext.resources.getDrawable(item.resId)
                } catch (e: Resources.NotFoundException) {
                    iconApp
                }
            } else {
                iconApp
            }

            //
            val packageManager = bindingTypeOne.icNotification.context.packageManager
            val name = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(
                    item.packageName!!,
                    PackageManager.GET_META_DATA
                )
            )
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = item.time
            //
            bindingTypeOne.apply {
                imgAppMarket.setImageDrawable(iconApp)
                icNotification.setImageDrawable(icon)
                txtTitle.text = item.title
                time.text = SimpleDateFormat("h:mm a").format(calendar.time)
                appName.text = name
            }
            bindingTypeOne.txtMessage.text = if (item.message == null) "" else item.message
            if (bindingTypeOne.txtMessage.lineCount > 3) {
                if (!item.isMore) {
                    bindingTypeOne.more.text = context.getString(R.string.see_more)
                    bindingTypeOne.more.visibility = View.VISIBLE
                    bindingTypeOne.txtMessage.maxLines = 3
                    bindingTypeOne.txtMessage.invalidate()
                } else {
                    bindingTypeOne.more.text = context.getString(R.string.collapse_)
                    bindingTypeOne.more.visibility = View.VISIBLE
                    bindingTypeOne.txtMessage.maxLines = Int.MAX_VALUE
                    bindingTypeOne.txtMessage.invalidate()
                }
            } else {
                bindingTypeOne.more.visibility = View.GONE
                bindingTypeOne.txtMessage.maxLines = Int.MAX_VALUE
            }

            bindingTypeOne.close.setOnClickListener {
                if (item.isClearable) {
                    Handler().post(Runnable {
                        val view = bindingTypeOne.layoutItem
                        val animation =
                            AnimationUtils.loadAnimation(
                                view.context,
                                R.anim.anim_remove_item
                            )
                        view.startAnimation(animation)

                    })
                    Handler().postDelayed({
                        val intent =
                            Intent(bindingTypeOne.close.context.resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_FOR_KEY))
                        intent.putExtra(
                            key,
                            item.key
                        )
                        intent.putExtra(
                            ID,
                            item.notificationId
                        )
                        bindingTypeOne.close.context.sendBroadcast(intent)
                    }, 0)
                    listener?.invoke()
                } else {
                    bindingTypeOne.layoutItem.startAnimation(animClickIcon)
                }
            }
            //
            itemView.setOnClickListener {
                bindingTypeOne.layoutContent.startAnimation(animClickIcon)
                Handler().postDelayed({
                    try {
                        item.contentIntent?.send()
                    } catch (e: PendingIntent.CanceledException) {
                        list.remove(item)
                        notifyItemRemoved(list.indexOf(item))
                    }
                }, 100)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeOneViewHolder {
        return TypeOneViewHolder(
            LayoutTypeOneViewholderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun removeSingleItem(item: ItemNotification) {
        listenerRemoveItem?.invoke(item)
    }

    override fun onBindViewHolder(holder: TypeOneViewHolder, position: Int) {
        holder.bindData(list[position])
        holder.bindingTypeOne.layoutMore.setOnClickListener {
            listenerMore?.invoke(holder.adapterPosition)
        }
    }
}