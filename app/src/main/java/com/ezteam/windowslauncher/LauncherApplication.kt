package com.ezteam.windowslauncher

import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.di.appModule
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.EzApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LauncherApplication : EzApplication() {
    override fun onCreate() {
        super.onCreate()
        PreferencesUtils.init(this)
        setupKoin()
        EzAdControl.initFlurry(this, "XJ5VZS52ZF3DX8S87CJH")
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@LauncherApplication)
            modules(
                appModule
            )
        }
    }
}