package com.ezteam.windowslauncher.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemLauncherBinding
import com.ezteam.windowslauncher.model.LauncherModel

class LauncherView(var context: Context, var viewGroup: ViewGroup) {
    private var binding: ItemLauncherBinding =
        ItemLauncherBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.item_launcher, viewGroup, false)
        )

    fun bindData(launcher: LauncherModel): LauncherView {
        val drawable: Drawable? = context.packageManager.getDrawable(
            launcher.packageName,
            launcher.logoResId, null
        )
        drawable?.let {
            binding.ivLauncher.setImageDrawable(it)
        }

        binding.tvLauncher.text = launcher.appName

        /*binding.root.setOnClickListener {
            itemClickListener(launcher)
        }*/
        return this
    }

    fun build(): View {
        return binding.root
    }
}