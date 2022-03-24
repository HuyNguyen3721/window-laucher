package com.ezteam.windowslauncher.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutFolderHorizontalBinding
import com.ezteam.windowslauncher.databinding.LayoutFolderVerticalBinding


class FolderView(context: Context, var attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var bindingHorizontal: LayoutFolderHorizontalBinding? = null
    private var bindingVertical: LayoutFolderVerticalBinding? = null

    init {
        iniView()
    }

    private fun iniView() {
        val viewAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.FolderView, 0, 0)
        val title = viewAttrs.getString(R.styleable.FolderView_title)
        val icon =
            viewAttrs.getResourceId(R.styleable.FolderView_icon, R.drawable.ic_folder_default)
        val orientation = viewAttrs.getInteger(R.styleable.FolderView_orientation, 1)
        val textSize = viewAttrs.getDimensionPixelSize(
            R.styleable.FolderView_textSize,
            context.resources.getDimensionPixelSize(R.dimen._12sdp)
        )
        val imageSize: Int
        if (orientation == 1) {
            bindingVertical = LayoutFolderVerticalBinding.bind(
                LayoutInflater.from(context).inflate(R.layout.layout_folder_vertical, this)
            )
            imageSize = viewAttrs.getDimensionPixelSize(
                R.styleable.FolderView_imageSize,
                context.resources.getDimensionPixelSize(R.dimen._50sdp)
            )
        } else {
            bindingHorizontal = LayoutFolderHorizontalBinding.bind(
                LayoutInflater.from(context).inflate(R.layout.layout_folder_horizontal, this)
            )
            imageSize = viewAttrs.getDimensionPixelSize(
                R.styleable.FolderView_imageSize,
                context.resources.getDimensionPixelSize(R.dimen._30sdp)
            )
        }

        bindingHorizontal?.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        bindingVertical?.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())

        setTitle(title)
        setIcon(icon)
        invalidate()
    }

    fun setTitle(title: String?) {
        if (!TextUtils.isEmpty(title)) {
            bindingVertical?.tvTitle?.text = title
            bindingHorizontal?.tvTitle?.text = title
        }
    }

    fun setIcon(resource: Int) {
        bindingHorizontal?.ivIcon?.setImageResource(resource)
        bindingVertical?.ivIcon?.setImageResource(resource)
    }

    fun setIcon(path: String, iconDefault: Int? = 0) {
        bindingHorizontal?.let {
            Glide.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .error(iconDefault)
                .into(it.ivIcon)
        }

        bindingVertical?.let {
            Glide.with(context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .error(iconDefault)
                .into(it.ivIcon)
        }
    }
}