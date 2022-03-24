package com.ezteam.windowslauncher.windowmanager

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import com.bumptech.glide.Glide
import com.ezteam.windowslauncher.databinding.LayoutWindowManagerBinding
import com.ezteam.windowslauncher.screen.MainActivity
import com.ezteam.windowslauncher.viewmodel.ControlViewModel
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.InvocationTargetException

@KoinApiExtension
class MyWindowManager(val context: Context) : KoinComponent {

    private lateinit var binding: LayoutWindowManagerBinding
    lateinit var windowManager: WindowManager
    private val viewModel by inject<ControlViewModel>()
    private val viewModelMain by inject<MainViewModel>()

    init {
        initView()
        initListener()
        initData()
    }

    private fun initView() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        binding = LayoutWindowManagerBinding.inflate(LayoutInflater.from(context), null, false)
    }

    private fun initListener() {
    }

    private fun initData() {
        viewModel.bitmap.observeForever {
            if (binding.imgBackground.visibility == View.VISIBLE) {
                MainActivity.isAutoClicking = true
                viewModelMain.windowManagerShowing.value = true
                Glide.with(context)
                    .load(it)
                    .into(binding.imgBackground)
                windowManager.addView(binding.root, setupLayout())
                Handler().postDelayed({
                    MainActivity.isAutoClicking = false
                    viewModelMain.windowManagerShowing.value = false
                    windowManager.removeViewImmediate(binding.root)
                }, 1500)
            } else {
                binding.imgBackground.visibility = View.VISIBLE
            }
        }
    }

    private fun setupLayout(): WindowManager.LayoutParams {
        val mLayoutParams: WindowManager.LayoutParams
        val flag = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_ERROR

        //

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val he = displayMetrics.heightPixels
        val height = if (getNavigationBarSize(context).y > 0) {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                WindowManager.LayoutParams.MATCH_PARENT
            } else {
                he + (getNavigationBarSize(context).y)
            }
        } else {
            WindowManager.LayoutParams.MATCH_PARENT
        }



        mLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            type,
            flag,
            PixelFormat.TRANSLUCENT
        )
        mLayoutParams.gravity = Gravity.TOP
        mLayoutParams.alpha = 1F
//
        return mLayoutParams
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        } else {
            result = 80
        }
        return result
    }

    private fun getNavigationBarSize(context: Context): Point {
        val appUsableSize = getAppUsableScreenSize(context)
        val realScreenSize = getRealScreenSize(context)

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
        }

        // navigation bar at the bottom
        return if (appUsableSize.y < realScreenSize.y) {
            Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
        } else Point()

        // navigation bar is not present
    }

    private fun getAppUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    private fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size)
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Display::class.java.getMethod("getRawWidth").invoke(display) as Int)
                size.y = (Display::class.java.getMethod("getRawHeight").invoke(display) as Int)
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            } catch (e: NoSuchMethodException) {
            }
        }
        return size
    }
}