package com.ezteam.windowslauncher.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.WindowPopupAppsTaskbarBinding

class TaskbarMenuPopup(
    context: Context,
    var onClickListener: (PopupWindow, View) -> Unit
) : ConstraintLayout(context) {
    private val binding = WindowPopupAppsTaskbarBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.window_popup_apps_taskbar, this)
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

    }

    private fun initListener() {
        binding.itemOpen.setOnClickListener(this::onItemClick)
        binding.itemUnpinTaskbar.setOnClickListener(this::onItemClick)
    }

    private fun onItemClick(view: View) {
        onClickListener(popupWindow, view)
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
            popupWindow.showAtLocation(anchorView, Gravity.BOTTOM or Gravity.LEFT, offsetX, offsetY)
        } catch (e: Exception) {
        }
    }

    fun show(anchorView: View?) {
        try {
            popupWindow.showAsDropDown(anchorView, 0, 0)
        } catch (e: Exception) {
        }
    }

    fun showAtLocation(view: View, xAxis: Int, yAxis: Int) {
        try {
            popupWindow.showAtLocation(view, Gravity.TOP or Gravity.LEFT, xAxis, yAxis + 70)
        } catch (e: Exception) {
        }
    }
}