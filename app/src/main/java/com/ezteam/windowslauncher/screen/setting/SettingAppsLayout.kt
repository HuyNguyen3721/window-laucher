package com.ezteam.windowslauncher.screen.setting

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.KeyboardUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutSettingAppsBinding
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import com.google.android.gms.ads.nativead.NativeAdView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingAppsLayout(
    context: Context,
    attributeSet: AttributeSet?
) : ConstraintLayout(context, attributeSet), KoinComponent {
    private val viewModel by inject<MainViewModel>()
    private var adsView: AdmobNativeAdView? = null

    private val binding = LayoutSettingAppsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_setting_apps, this)
    )

    init {
        initViews()
        initData()
        initListener()
        loadAds()
    }

    private fun initViews() {

        binding.tvRows.text = LauncherAppUtils.getSizeGrid(context).rows.toString()
        binding.tvColumns.text = LauncherAppUtils.getSizeGrid(context).columns.toString()
        binding.root.postDelayed({
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.seekbarRows.max = LauncherAppUtils.PORTRAIT_ROWS_MAX - LauncherAppUtils.PORTRAIT_ROWS_MIN
                binding.seekbarColumns.max = LauncherAppUtils.PORTRAIT_COLUMNS_MAX - LauncherAppUtils.PORTRAIT_COLUMNS_MIN

                binding.seekbarColumns.progress = LauncherAppUtils.getSizeGrid(context).columns - LauncherAppUtils.PORTRAIT_COLUMNS_MIN
                binding.seekbarRows.progress = LauncherAppUtils.getSizeGrid(context).rows - LauncherAppUtils.PORTRAIT_ROWS_MIN
            } else {
                binding.seekbarRows.max = LauncherAppUtils.LANDSCAPE_ROWS_MAX - LauncherAppUtils.LANDSCAPE_ROWS_MIN
                binding.seekbarColumns.max = LauncherAppUtils.LANDSCAPE_COLUMNS_MAX - LauncherAppUtils.LANDSCAPE_COLUMNS_MIN

                binding.seekbarColumns.progress = LauncherAppUtils.getSizeGrid(context).columns - LauncherAppUtils.LANDSCAPE_COLUMNS_MIN
                binding.seekbarRows.progress = LauncherAppUtils.getSizeGrid(context).rows - LauncherAppUtils.LANDSCAPE_ROWS_MIN
            }
        }, 200)
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.settingShowing.observe(it) {
                if (it) {
                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                    adsView?.loadAd()
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
                    KeyboardUtils.hideSoftKeyboard((context as AppCompatActivity))
                }
                initViews()
            }
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.settingShowing.postValue(false)
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
                binding.tvColumns.text = LauncherAppUtils.getSizeGrid(context).columns.toString()
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
                binding.tvRows.text = LauncherAppUtils.getSizeGrid(context).rows.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admob_item_apps,
            object : AdmobNativeAdView.NativeAdListener {
                override fun onError() {
                    binding.adsView.isVisible = false
                }

                override fun onLoaded(nativeAd: AdmobNativeAdView?) {

                    nativeAd?.let {
                        binding.adsView.isVisible = true
                        if (it.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        binding.adsView.addView(it)

                        adsView = nativeAd
                    }

                }

                override fun onClickAd() {

                }
            })
    }
}