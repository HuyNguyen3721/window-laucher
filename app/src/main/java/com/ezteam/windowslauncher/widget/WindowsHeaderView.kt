package com.ezteam.windowslauncher.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutHeaderWindowsBinding

class WindowsHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    companion object {
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
    }

    val binding = LayoutHeaderWindowsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_header_windows, this)
    )

    var textColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            binding.tvHeader.setTextColor(value)
            binding.tvButton.setTextColor(value)
            binding.ivNext.setColorFilter(value)
        }

    var textHeader = ""
        set(value) {
            field = value
            binding.tvHeader.text = value
        }

    var textButton = ""
        set(value) {
            field = value
            binding.tvButton.text = value
        }

    var isBack = false
        set(value) {
            field = value
            binding.ivBack.isVisible = value
            binding.ivNext.isVisible = !value
        }

    var isMore = true
        set(value) {
            field = value
            binding.buttonView.isVisible = value
        }

    var onHeaderClickListener: (() -> Unit)? = null

    init {
        setupAttributes(attrs)

        binding.buttonView.setOnClickListener {
            onHeaderClickListener?.invoke()
        }
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.WindowsHeaderView, 0, 0)
        textColor =
            typedArray.getColor(R.styleable.WindowsHeaderView_whTextColor, DEFAULT_TEXT_COLOR)
        textHeader = typedArray.getString(R.styleable.WindowsHeaderView_whTextHeader).toString()
        textButton = typedArray.getString(R.styleable.WindowsHeaderView_whTextButton).toString()
        isBack = typedArray.getBoolean(R.styleable.WindowsHeaderView_whIsBack, false)
        isMore = typedArray.getBoolean(R.styleable.WindowsHeaderView_whIsMore, true)
    }
}