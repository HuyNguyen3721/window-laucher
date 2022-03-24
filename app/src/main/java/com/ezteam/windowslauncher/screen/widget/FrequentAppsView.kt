package com.ezteam.windowslauncher.screen.widget

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemLauncherAdapter
import com.ezteam.windowslauncher.databinding.LayoutFrequentAppsBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FrequentAppsView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs),
    KoinComponent {
    private val binding = LayoutFrequentAppsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_frequent_apps, this)
    )

    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: ItemLauncherAdapter
    var currentOrientation = Configuration.ORIENTATION_PORTRAIT
        set(value) {
            field = value
            initData()
        }

    init {
        initViews()
        initData()
    }

    private fun initViews() {
        adapter = ItemLauncherAdapter(
            context,
            mutableListOf(),
            this::itemLauncherPress,
            this::itemLongClick
        )
        binding.rcvApps.adapter = adapter
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.getFrequentApps().observe(it) {
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
        }

        context.lifecycleOwner()?.let {

        }
    }

    private fun itemLauncherPress(launcher: LauncherModel) {
        viewModel.updateRecentApp(launcher)
        (context as BaseWindowsActivity<*>).openApp(launcher)
    }

    private fun itemLongClick(view: View, launcher: LauncherModel) {

    }
}