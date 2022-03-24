package com.ezteam.windowslauncher.screen.notification

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutFragmentCenterBinding
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.center.*
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.android.ext.android.inject


class FragmentCenter : BaseFragment<LayoutFragmentCenterBinding>() {
    private var animExitLayoutNotification: Animation? = null

    private val viewModel by inject<MainViewModel>()

    companion object {
        private const val CAMERA_PIC_REQUEST = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val REQUEST_STORAGE_PERMISSION = 3
    }

    override fun initView() {

    }

    override fun initData() {
        animExitLayoutNotification = AnimationUtils.loadAnimation(context, R.anim.enter)
        reloadControlBottom()
        binding.layoutCenter.postDelayed({
            context?.sendBroadcast(
                Intent(
                    binding.layoutCenter.context.resources.getString(
                        R.string.ACTION_NOTIFICATION_SERVICE
                    )
                )
            )
        }, 350)

        viewModel.notifyCenterShowing.observe(this) {
            if (it) {
                requireContext().sendBroadcast(Intent(Config.ACTION_RELOAD))
                reloadControlBottom()
                ViewUtils.showViewBase(Techniques.SlideInRight, binding.root, 300)
            } else {
                ViewUtils.hideViewBase(Techniques.SlideOutRight, binding.root, 300)
            }
        }
    }

    private fun reloadControlBottom() {
        ManualFilterUtil.sendIntentManual(requireContext())
        MobileDataFilterUtil.sendIntentMobileData(requireContext())
        RotateFilterUtil.sendIntentRotate(requireContext())
        HotspotFilterUtil.sendIntentHotspot(requireContext())
        LocationFilterUtil.sendIntentLocation(requireContext())
    }

    override fun initListener() {
        binding.root.setOnClickListener {
            viewModel.notifyCenterShowing.value = false
        }
    }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentCenterBinding {
        return LayoutFragmentCenterBinding.inflate(inflater, container, false)
    }

    private fun unRegisterBroadCast() {
        context?.unregisterReceiver(binding.layoutCenter.broadCastListener)
        binding.layoutCenter.binding.layoutViewControl.clearBroadCast()
        binding.layoutCenter.clearObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterBroadCast()
    }
}