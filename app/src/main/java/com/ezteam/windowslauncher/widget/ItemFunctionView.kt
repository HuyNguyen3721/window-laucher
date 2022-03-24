package com.ezteam.windowslauncher.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ItemFunctionBinding

class ItemFunctionView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    companion object {
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
    }

    private val binding =
        ItemFunctionBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_function, this))

    var textColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            binding.tvFunction.setTextColor(value)
            binding.ivNext.setColorFilter(value)
        }

    var textFunction = ""
        set(value) {
            field = value
            binding.tvFunction.text = value
        }

    var iconResId = 0
        set(value) {
            field = value
            if (value != 0) {
                binding.ivIcon.setImageResource(value)
                binding.ivIcon.isVisible = true
            } else {
                binding.ivIcon.isVisible = false
            }
        }

    var isShowIconNext = true
        set(value) {
            field = value
            binding.ivNext.isVisible = value
        }

    init {
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ItemFunctionView, 0, 0)
        textColor =
            typedArray.getColor(R.styleable.ItemFunctionView_fvTextColor, DEFAULT_TEXT_COLOR)
        textFunction = typedArray.getString(R.styleable.ItemFunctionView_fvTextFunction).toString()
        iconResId = typedArray.getResourceId(R.styleable.ItemFunctionView_fvIconResId, 0)
        isShowIconNext = typedArray.getBoolean(R.styleable.ItemFunctionView_fvShowIconNext, true)
    }
}