package com.ezteam.windowslauncher.screen.taskbar

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.KeyboardUtils
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemLauncherAdapter
import com.ezteam.windowslauncher.databinding.LayoutTaskbarAppsBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.popup.TaskbarMenuPopup
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AppsMoreTaskbarLayout(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: ItemLauncherAdapter
    private val binding = LayoutTaskbarAppsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_taskbar_apps, this)
    )

    init {
        initView()
        initData()
        initListener()
    }

    private fun initView() {
        adapter = ItemLauncherAdapter(
            context,
            mutableListOf(),
            ::itemLauncherPress,
            ::itemLongClick
        )
        binding.rcvApps.adapter = adapter
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
                    }

                }

                override fun onClickAd() {
                }

            })
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.appsMoreTaskbarShowing.observe(it) {
                if (it) {
                    ViewUtils.showViewBase(Techniques.FadeIn, binding.root, 300)
//                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                } else {
                    ViewUtils.hideViewBase(Techniques.FadeOut, binding.root, 300)
                    KeyboardUtils.hideSoftKeyboard((context as AppCompatActivity))
                }
            }

            viewModel.appsRunning.observe(it) {
                adapter.setList(it.distinctBy { it.packageName })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.appsMoreTaskbarShowing.value = false
        }

        binding.buttonView.setOnClickListener {
            viewModel.appsRunning.postValue(mutableListOf())
            viewModel.appsMoreTaskbarShowing.value = false
        }
    }

    private fun itemLauncherPress(launcher: LauncherModel) {
        val launchIntent: Intent? =
            context.packageManager.getLaunchIntentForPackage(launcher.packageName)
        launchIntent?.let {
            context.startActivity(it)
        }
    }

    private fun itemLongClick(view: View, launcher: LauncherModel) {
        val taskbarPopup = TaskbarMenuPopup(context) { popupWindow, view ->
            when (view.id) {
                R.id.item_open -> {
                    (context as BaseWindowsActivity<*>).openApp(launcher)
                }
                R.id.item_unpin_taskbar -> {
                    viewModel.pinAndUnpinTaskbar(launcher) {
                        if (it) {
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.pinned_app_task_success))
                        } else {
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.unpinned_app_task_success))
                        }
                    }
                }
            }
        }

        taskbarPopup.show(view)
    }
}