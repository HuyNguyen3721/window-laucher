package com.ezteam.windowslauncher.screen.setting

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.databinding.ActivitySettingBinding
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.android.ext.android.inject

class SettingActivity: BaseActivity<ActivitySettingBinding>() {
    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val viewModel by inject<MainViewModel>()

    override fun initView() {
        binding.tvRows.text = LauncherAppUtils.getSizeGrid(this).rows.toString()
        binding.tvColumns.text = LauncherAppUtils.getSizeGrid(this).columns.toString()

        binding.root.postDelayed({
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.seekbarRows.max = LauncherAppUtils.PORTRAIT_ROWS_MAX - LauncherAppUtils.PORTRAIT_ROWS_MIN
                binding.seekbarColumns.max = LauncherAppUtils.PORTRAIT_COLUMNS_MAX - LauncherAppUtils.PORTRAIT_COLUMNS_MIN

                binding.seekbarColumns.progress = LauncherAppUtils.getSizeGrid(this).columns - LauncherAppUtils.PORTRAIT_COLUMNS_MIN
                binding.seekbarRows.progress = LauncherAppUtils.getSizeGrid(this).rows - LauncherAppUtils.PORTRAIT_ROWS_MIN
            } else {
                binding.seekbarRows.max = LauncherAppUtils.LANDSCAPE_ROWS_MAX - LauncherAppUtils.LANDSCAPE_ROWS_MIN
                binding.seekbarColumns.max = LauncherAppUtils.LANDSCAPE_COLUMNS_MAX - LauncherAppUtils.LANDSCAPE_COLUMNS_MIN

                binding.seekbarColumns.progress = LauncherAppUtils.getSizeGrid(this).columns - LauncherAppUtils.LANDSCAPE_COLUMNS_MIN
                binding.seekbarRows.progress = LauncherAppUtils.getSizeGrid(this).rows - LauncherAppUtils.LANDSCAPE_ROWS_MIN
            }
        }, 200)
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.gridSetting.setOnClickListener {
            binding.gridChild.isVisible = !binding.gridChild.isVisible
        }

        binding.seekbarColumns.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    PreferencesUtils.putInteger(PresKey.PORTRAIT_COLUMNS, progress + LauncherAppUtils.PORTRAIT_COLUMNS_MIN)
                } else {
                    PreferencesUtils.putInteger(PresKey.LANDSCAPE_COLUMNS, progress + LauncherAppUtils.LANDSCAPE_COLUMNS_MIN)
                }

                viewModel.resizeGridLauncher.postValue(true)
                binding.tvColumns.text = LauncherAppUtils.getSizeGrid(this@SettingActivity).columns.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.seekbarRows.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return

                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    PreferencesUtils.putInteger(PresKey.PORTRAIT_ROWS, progress + LauncherAppUtils.PORTRAIT_ROWS_MIN)
                } else {
                    PreferencesUtils.putInteger(PresKey.LANDSCAPE_ROWS, progress + LauncherAppUtils.LANDSCAPE_ROWS_MIN)
                }

                viewModel.resizeGridLauncher.postValue(true)
                binding.tvRows.text = LauncherAppUtils.getSizeGrid(this@SettingActivity).rows.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun viewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(LayoutInflater.from(this))
    }
}