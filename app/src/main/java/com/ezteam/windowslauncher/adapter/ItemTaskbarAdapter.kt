package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.databinding.ItemAppTaskbarBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.utils.launcher.LauncherType

class ItemTaskbarAdapter(
    context: Context,
    list: MutableList<LauncherModel>,
    val itemClickListener: (LauncherModel, View) -> Unit,
    val itemLongClickListener: (LauncherModel, View) -> Unit
) : BaseRecyclerAdapter<LauncherModel, ItemTaskbarAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(var binding: ItemAppTaskbarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(launcher: LauncherModel) {
            val drawable: Drawable? = mContext.packageManager.getDrawable(
                launcher.packageName,
                launcher.logoResId, null
            )
            if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                drawable?.let {
                    binding.ivLauncher.setImageDrawable(it)
                }
            } else {
                binding.ivLauncher.setImageResource(launcher.logoResId)
            }

            binding.root.setOnClickListener {
                itemClickListener(launcher, it)
            }

            binding.root.setOnLongClickListener {
                itemLongClickListener(launcher, it)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppTaskbarBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}