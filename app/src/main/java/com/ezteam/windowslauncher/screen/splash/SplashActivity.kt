package com.ezteam.windowslauncher.screen.splash

import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.extensions.launchActivity
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.ActivitySplashBinding
import com.ezteam.windowslauncher.screen.MainActivity
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.AdFactoryListener
import com.google.android.gms.ads.ez.LogUtils
import com.google.android.gms.ads.ez.admob.AdmobOpenAdUtils
import org.koin.android.ext.android.inject

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val viewModel by inject<MainViewModel>()

    override fun initView() {
        Glide.with(this)
            .load(R.drawable.splash_background)
            .into(binding.ivBackground)

        // Screen full
        setAppActivityFullScreenOver(this)

        loadOpenAds()
    }

    private fun loadOpenAds() {
        AdmobOpenAdUtils.getInstance(this).setAdListener(object : AdFactoryListener() {
            override fun onError() {
                LogUtils.logString(SplashActivity::class.java, "onError")
                openMain()
            }

            override fun onLoaded() {
                LogUtils.logString(SplashActivity::class.java, "onLoaded")
                // show ads ngay khi loaded
                AdmobOpenAdUtils.getInstance(this@SplashActivity).showAdIfAvailable()
            }

            override fun onDisplay() {
                super.onDisplay()
                LogUtils.logString(SplashActivity::class.java, "onDisplay")
            }

            override fun onDisplayFaild() {
                super.onDisplayFaild()
                LogUtils.logString(SplashActivity::class.java, "onDisplayFaild")
                openMain()
            }

            override fun onClosed() {
                super.onClosed()
                // tam thoi bo viec load lai ads thi dismis
                LogUtils.logString(SplashActivity::class.java, "onClosed")
                openMain()
            }
        }).loadAd()

    }

    private fun openMain() {
        launchActivity<MainActivity> { }
        finish()
    }

    override fun initData() {
        viewModel.loadAppData()
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }
}