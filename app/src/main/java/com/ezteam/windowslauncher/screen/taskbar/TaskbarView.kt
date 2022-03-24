package com.ezteam.windowslauncher.screen.taskbar

import android.content.Context
import android.content.res.Configuration
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemTaskbarAdapter
import com.ezteam.windowslauncher.databinding.WindowsTaskbarBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.SpecialPackage
import com.ezteam.windowslauncher.utils.launcher.LauncherType
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class TaskbarView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs),
    KoinComponent {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: ItemTaskbarAdapter
    var taskbarItemClickListener: ((String) -> Unit)? = null
    var taskbarAppItemClickListener: ((LauncherModel, View) -> Unit)? = null
    var taskbarAppItemLongClickListener: ((LauncherModel, View) -> Unit)? = null

    private val binding = WindowsTaskbarBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.windows_taskbar, this)
    )

    private val countDownTimer = object : CountDownTimer(1000, 100) {
        override fun onTick(millisUntilFinished: Long) {
            val currentTime = System.currentTimeMillis()
            val time = DateUtils.longToDateString(currentTime, DateUtils.TIME_FORMAT_1)
            val date = DateUtils.longToDateString(currentTime, DateUtils.DATE_FORMAT_8)
            binding.tvTime.text = "$time\n$date"
        }

        override fun onFinish() {
            start()
        }
    }

    init {
        initViews()
        initData()
        initListener()
    }

    private fun initViews() {
        countDownTimer.start()
        adapter = ItemTaskbarAdapter(
            context,
            mutableListOf(),
            ::itemClickListener,
            ::itemLongClickListener
        )
        binding.rcvApp.adapter = adapter
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            val orientation = resources.configuration.orientation
            viewModel.getTaskbarItems().observe(it) {
                val apps = mutableListOf<LauncherModel>()
                apps.addAll(TaskBarData.getItemOriginal())
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (it.size > 2) {
                        apps.addAll(it.subList(0, 2))
//                        binding.groupApps.loadIcon(it.subList(2, it.size))
                    } else {
                        apps.addAll(it)
                    }

//                    binding.groupApps.isVisible = it.size > 3
                } else {
                    if (it.size > 10) {
                        apps.addAll(it.subList(0, 10))
//                        binding.groupApps.loadIcon(it.subList(10, it.size))
                    } else {
                        apps.addAll(it)
                    }

//                    binding.groupApps.isVisible = it.size > 11
                }

                adapter.setList(apps)
                adapter.notifyDataSetChanged()
            }

            viewModel.appsRunning.observe(it) {
                binding.groupApps.isVisible = it.size > 0
                binding.groupApps.loadIcon(it.distinctBy { it.packageName })
            }
        }
    }

    private fun initListener() {
        binding.ivMessageAlt.setOnClickListener {
            taskbarItemClickListener?.invoke(SpecialPackage.CENTER_PACKAGE.packageId)
        }

        binding.ivMoreFunc.setOnClickListener {
            taskbarItemClickListener?.invoke(SpecialPackage.FAST_CONTROL_PACKAGE.packageId)
        }

        binding.tvTime.setOnClickListener {
            taskbarItemClickListener?.invoke(SpecialPackage.CALENDAR_PACKAGE.packageId)
        }

        binding.groupApps.setOnClickListener {
            taskbarItemClickListener?.invoke(SpecialPackage.TASKBAR_GROUP_APPS.packageId)
        }
    }

    private fun itemClickListener(launcher: LauncherModel, view: View) {
        taskbarAppItemClickListener?.invoke(launcher, view)
    }

    private fun itemLongClickListener(launcher: LauncherModel, view: View) {
        if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
            taskbarAppItemLongClickListener?.invoke(launcher, view)
        }
    }
}