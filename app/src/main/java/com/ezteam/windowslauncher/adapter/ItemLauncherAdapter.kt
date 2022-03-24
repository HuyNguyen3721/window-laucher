package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.windowslauncher.databinding.ItemLauncherBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.utils.launcher.LauncherType

class ItemLauncherAdapter(
    context: Context,
    list: MutableList<LauncherModel>,
    var itemClickListener: (LauncherModel) -> Unit,
    var itemLongClickListener: (View, LauncherModel) -> Unit
) : BaseRecyclerAdapter<LauncherModel, ItemLauncherAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(
        var binding: ItemLauncherBinding
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

            binding.tvLauncher.text = launcher.appName

            binding.root.setOnClickListener {
                itemClickListener(launcher)
            }

            binding.root.setOnLongClickListener {
                itemLongClickListener(binding.ivLauncher, launcher)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(list[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLauncherBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}