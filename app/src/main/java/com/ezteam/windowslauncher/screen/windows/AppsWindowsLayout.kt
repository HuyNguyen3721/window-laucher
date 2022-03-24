package com.ezteam.windowslauncher.screen.windows

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutAppsWindowsBinding
import com.ezteam.windowslauncher.screen.setting.SettingActivity
import com.ezteam.windowslauncher.viewmodel.AppWindowsState
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AppsWindowsLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs),
    KoinComponent {
    private val binding = LayoutAppsWindowsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_apps_windows, this)
    )

    private lateinit var adapter: BasePagerAdapter
    private val viewModel by inject<MainViewModel>()
    private var adsView: AdmobNativeAdView? = null

    init {
        initViews()
        initData()
        initListener()
    }

    private fun initViews() {
        (context as AppCompatActivity).supportFragmentManager.let {
            adapter = BasePagerAdapter(it, 0)
            adapter.addFragment(AppsSuggestFragment(), "")
            adapter.addFragment(AllAppsFragment(), "")
            binding.viewPager.adapter = adapter

        }

        binding.tvAdmin.text = if (getDeviceName().isNotEmpty()) getDeviceName() else
            resources.getString(R.string.admin)
        loadAds()
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admob_item_apps,
            object : AdmobNativeAdView.NativeAdListener {
                override fun onError() {
                    binding.adsView?.isVisible = false
                }

                override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                    nativeAd?.let {
                        binding.adsView?.isVisible = true
                        if (it.parent != null) {
                            (it.parent as ViewGroup).removeView(it)
                        }
                        binding.adsView?.addView(it)

                        adsView = nativeAd
                    }
                }

                override fun onClickAd() {
                }
            })
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.windowsShowing.observe(it) {
                if (it) {
                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                    adsView?.loadAd()
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
                }
                viewModel.windowsFragmentState.value = AppWindowsState.PINED
            }

            viewModel.windowsFragmentState.observe(it) {
                when (it) {
                    AppWindowsState.PINED -> {
                        binding.viewPager.setCurrentItem(0, true)
                    }

                    AppWindowsState.ALL_APPS -> {
                        binding.viewPager.setCurrentItem(1, true)
                    }
                }
            }
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.windowsShowing.value = false
        }

        binding.ivShutdown.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(resources.getString(R.string.launcher_home_title))
            builder.setMessage(resources.getString(R.string.launcher_home_content))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                context.startActivity(intent)
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->

            }
            builder.show()

            /*val selector = Intent(Intent.ACTION_MAIN)
            selector.addCategory(Intent.CATEGORY_HOME)
            selector.component =
                ComponentName("android", "com.android.internal.app.ResolverActivity")
            startActivity(selector)*/

//            SettingActivity.start(context as AppCompatActivity)
        }
    }

    private fun getDeviceName(): String {
        val myDevice: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        return myDevice?.name ?: ""
    }
}