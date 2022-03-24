package com.ezteam.windowslauncher.widget

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.windowslauncher.R

class PopupMenuCustom(
    context: Context,
    rLayoutId: Int,
    onClickListener: (Int) -> Unit
) {
    private val popupWindow: PopupWindow
    private val popupView: View
    fun setAnimationStyle(animationStyle: Int) {
        popupWindow.animationStyle = animationStyle
    }

    fun show() {
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    fun show(anchorView: View?, offsetX: Int = 0, offsetY: Int = 0) {
        popupWindow.showAsDropDown(anchorView, offsetX, offsetY)
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(rLayoutId, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        popupWindow = PopupWindow(popupView, width, height, focusable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.elevation = 10f
        }
        val layoutParent = popupView.findViewById(R.id.parent) as ConstraintLayout
        for (i in 0 until layoutParent.childCount) {
            val view = layoutParent.getChildAt(i)
            view.setOnClickListener {
                onClickListener(it.id)
                popupWindow.dismiss()
            }
        }
    }
}