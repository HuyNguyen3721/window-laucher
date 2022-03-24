package com.ezteam.windowslauncher.screen.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutCalendarBinding
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WindowsCalendarView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val binding = LayoutCalendarBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_calendar, this)
    )

    private val viewModel by inject<MainViewModel>()

    init {
        initViews()
        initData()
        initListener()
    }

    private fun initViews() {
        binding.root.isVisible = false
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.calendarShowing.observe(it) {
                if (it) {
                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                    binding.calendarView?.setDate(System.currentTimeMillis(), false, true)
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
                }
            }
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.calendarShowing.value = false
        }
    }
}