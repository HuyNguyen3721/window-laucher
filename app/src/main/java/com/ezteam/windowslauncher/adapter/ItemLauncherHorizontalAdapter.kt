package com.ezteam.windowslauncher.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.adapter.BaseRecyclerAdapter
import com.ezteam.baseproject.extensions.getDisplayMetrics
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemLauncherHorizontalBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.utils.launcher.LauncherType


class ItemLauncherHorizontalAdapter(
    context: Context,
    list: MutableList<LauncherModel>,
    var itemClickListener: (LauncherModel) -> Unit,
    var itemLongClickListener: (View, LauncherModel) -> Unit
) : BaseRecyclerAdapter<LauncherModel, ItemLauncherHorizontalAdapter.ViewHolder>(context, list) {
    inner class ViewHolder(
        var binding: ItemLauncherHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(launcher: LauncherModel) {
            val drawable: Drawable? = mContext.packageManager.getDrawable(
                launcher.packageName,
                launcher.logoResId, null
            )

            val displayWidth = mContext.getDisplayMetrics().widthPixels
            binding.root.apply {
                layoutParams.width = displayWidth / 5
            }

            val paddingLarge = mContext.resources.getDimensionPixelOffset(R.dimen._7sdp)
            val paddingNormal = mContext.resources.getDimensionPixelOffset(R.dimen._5sdp)

            if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                drawable?.let {
                    binding.ivLauncher.setImageDrawable(it)
                    binding.ivLauncher.setPadding(
                        paddingLarge,
                        paddingLarge,
                        paddingLarge,
                        paddingNormal
                    )
                }
            } else {
                binding.ivLauncher.setImageResource(launcher.logoResId)
                binding.ivLauncher.setPadding(paddingNormal)
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
        val binding = ItemLauncherHorizontalBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }
}