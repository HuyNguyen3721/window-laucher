package com.ezteam.windowslauncher.widget.pcView.center

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutPcCenterBinding
import com.ezteam.windowslauncher.widget.pcView.BaseFileManagerView

class PcViewCenter(context: Context, attrs: AttributeSet?) : BaseFileManagerView(context, attrs) {


    private var binding = LayoutPcCenterBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_pc_center, this)
    )

    init {
        initView()
        initControl()
    }

    private fun initView() {

    }

    private fun initControl() {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.expendLeftLayout.observeForever {
            if (it) {
                binding.ctrLeft.layoutParams.width = resources.getDimensionPixelSize(R.dimen._120sdp)
            } else {
                binding.ctrLeft.layoutParams.width = resources.getDimensionPixelSize(R.dimen._30sdp)
            }
        }
    }

}