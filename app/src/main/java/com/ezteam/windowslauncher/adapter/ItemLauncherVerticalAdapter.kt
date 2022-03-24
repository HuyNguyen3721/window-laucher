package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.databinding.ItemLauncherVerticalBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.utils.launcher.LauncherType

class ItemLauncherVerticalAdapter(
    context: Context,
    list: MutableList<LauncherModel>,
    var itemClickListener: (LauncherModel) -> Unit,
    var itemLongClickListener: (View, LauncherModel) -> Unit,
    var itemHeaderClickListener: (LauncherModel) -> Unit
) : BaseRecyclerAdapter<LauncherModel, ItemLauncherVerticalAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(
        var binding: ItemLauncherVerticalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
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

            binding.tvHeader.isVisible = launcher.showHeader && launcher.appName.isNotEmpty()

            binding.tvLauncher.text = launcher.appName
            binding.tvHeader.text =
                if (launcher.appName.isEmpty()) "" else launcher.appName[0].toString()

            binding.contentView.setOnClickListener {
                itemClickListener(launcher)
            }

            binding.contentView.setOnLongClickListener {
                itemLongClickListener(it, launcher)
                true
            }

            binding.tvHeader.setOnClickListener {
                itemHeaderClickListener(launcher)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLauncherVerticalBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}