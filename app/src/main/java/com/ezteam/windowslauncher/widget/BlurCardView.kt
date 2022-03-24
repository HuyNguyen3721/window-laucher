package com.ezteam.windowslauncher.widget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.BlurCardViewBinding
import eightbitlab.com.blurview.RenderScriptBlur

class BlurCardView(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    companion object {
        private const val BLUR_RADIUS_DEFAULT = 5f
    }

    private val binding = BlurCardViewBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.blur_card_view, this)
    )

    var blurRadius: Float = BLUR_RADIUS_DEFAULT

    @ColorInt
    var blurResColor: Int = resources.getColor(R.color.tokenAppColor40)

    init {
        setupAttributes(attrs)
        setCardBackgroundColor(resources.getColor(android.R.color.transparent))
        cardElevation = 0f
        setupBlurView()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.BlurCardView, 0, 0)
        blurRadius = typedArray.getFloat(R.styleable.BlurCardView_bcBlurRadius, BLUR_RADIUS_DEFAULT)
        blurResColor = typedArray.getColor(
            R.styleable.BlurCardView_bcBlurColor,
            resources.getColor(R.color.tokenAppColor40)
        )
    }

    private fun setupBlurView() {
        val radius = blurRadius

        val decorView: View = (context as Activity).window.decorView
        val rootView: ViewGroup = decorView.findViewById(android.R.id.content) as ViewGroup
        val windowBackground: Drawable = decorView.background

        binding.blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setOverlayColor(blurResColor)
            .setHasFixedTransformationMatrix(true)
    }
}