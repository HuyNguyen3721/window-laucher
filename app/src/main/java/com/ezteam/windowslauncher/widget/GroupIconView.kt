package com.ezteam.windowslauncher.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.GroupIconViewBinding
import com.ezteam.windowslauncher.model.LauncherModel

class GroupIconView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    val binding = GroupIconViewBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.group_icon_view, this)
    )

    fun loadIcon(apps: List<LauncherModel>) {
        if (apps.isNotEmpty()) {
            binding.ivOne.setImageDrawable(getIcon(apps[0]))
        }

        if (apps.size > 1) {
            binding.ivTwo.setImageDrawable(getIcon(apps[1]))
        }

        if (apps.size > 2) {
            binding.ivThree.setImageDrawable(getIcon(apps[2]))
        }

        if (apps.size > 3) {
            binding.ivFour.setImageDrawable(getIcon(apps[3]))
        }
    }

    fun getIcon(launcher: LauncherModel): Drawable? {
        return context.packageManager.getDrawable(
            launcher.packageName,
            launcher.logoResId, null
        )
    }
}