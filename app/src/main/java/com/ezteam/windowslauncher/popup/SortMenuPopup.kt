package com.ezteam.windowslauncher.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.WindowPopupSortBinding

class SortMenuPopup(
    context: Context,
    var onClickListener: (Int) -> Unit
) : ConstraintLayout(context) {
    private val binding = WindowPopupSortBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.window_popup_sort, this)
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
        binding.itemSortByName.setOnClickListener(this::onItemClick)
        binding.itemSortByType.setOnClickListener(this::onItemClick)
        binding.itemSortByDate.setOnClickListener(this::onItemClick)
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

    fun showAtLocation(view: View, xAxis: Int, yAxis: Int) {
        try {
            popupWindow.showAtLocation(view, Gravity.TOP or Gravity.LEFT, xAxis, yAxis + 70)
        } catch (e: Exception) {
        }
    }
}