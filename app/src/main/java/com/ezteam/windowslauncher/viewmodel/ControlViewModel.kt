package com.ezteam.windowslauncher.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.ezteam.baseproject.viewmodel.BaseViewModel
import com.ezteam.windowslauncher.screen.MainActivity

class ControlViewModel(application: Application) : BaseViewModel(application) {
    var bitmap = MutableLiveData<Bitmap>()

    fun takePhotoLauncher(mainActivity: MainActivity) {
        val v = (mainActivity).window.decorView.rootView
        v.isDrawingCacheEnabled = true
        bitmap.postValue(Bitmap.createBitmap(v.drawingCache))
        v.isDrawingCacheEnabled = false
    }
}