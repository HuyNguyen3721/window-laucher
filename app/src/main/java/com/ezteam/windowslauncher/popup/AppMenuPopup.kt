package com.ezteam.windowslauncher.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.WindowPopupAppsBinding
import com.ezteam.windowslauncher.model.LauncherModel

class AppMenuPopup(
    context: Context,
    var launcher: LauncherModel,
    var onClickListener: (Int) -> Unit
) : ConstraintLayout(context) {
    private val binding = WindowPopupAppsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.window_popup_apps, this)
    )
    private val popupWindow: PopupWindow

    init {
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        popupWindow = PopupWindow(binding.root, width, height, focusable)
        popupWindow.isFocusable = false
        popupWindow.isOutsideTouchable = true
        initViews()
        initListener()
    }

    private fun initViews() {
        binding.itemUninstallApp.isVisible = !launcher.systemPackage
        binding.itemPinWindows.isVisible = !launcher.pinHome
        binding.itemUnpinWindows.isVisible = launcher.pinHome
        binding.itemPinTaskbar.isVisible = !launcher.pinTaskbar
        binding.itemUnpinTaskbar.isVisible = launcher.pinTaskbar
        binding.itemLockApp.isVisible = launcher.password.isEmpty()
        binding.itemUnlockApp.isVisible = launcher.password.isNotEmpty()
    }

    private fun initListener() {
        binding.itemCreateShortcut.setOnClickListener(this::onItemClick)
        binding.itemProperties.setOnClickListener(this::onItemClick)
        binding.itemUninstallApp.setOnClickListener(this::onItemClick)
        binding.itemPinWindows.setOnClickListener(this::onItemClick)
        binding.itemUnpinWindows.setOnClickListener(this::onItemClick)
        binding.itemLockApp.setOnClickListener(this::onItemClick)
        binding.itemUnlockApp.setOnClickListener(this::onItemClick)
        binding.itemPinTaskbar.setOnClickListener(this::onItemClick)
        binding.itemUnpinTaskbar.setOnClickListener(this::onItemClick)
    }

    private fun onItemClick(view: View) {
        onClickListener(view.id)
        popupWindow.dismiss()
    }

    fun show() {
        try {
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        } catch (e: Exception) {
        }
    }

    fun show(anchorView: View?, offsetX: Int = 0, offsetY: Int = 0) {
        try {
            popupWindow.showAsDropDown(anchorView, offsetX, offsetY)
        } catch (e: Exception) {
        }
    }
}