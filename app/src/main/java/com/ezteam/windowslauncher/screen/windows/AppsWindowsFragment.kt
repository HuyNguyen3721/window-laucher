package com.ezteam.windowslauncher.screen.windows

import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.adapter.BasePagerAdapter
import com.ezteam.baseproject.fragment.BaseFragment
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.broadcast.DeviceAdmin
import com.ezteam.windowslauncher.databinding.FragmentAppsWindowsBinding
import com.ezteam.windowslauncher.viewmodel.AppWindowsState
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.android.ext.android.inject

class AppsWindowsFragment : BaseFragment<FragmentAppsWindowsBinding>() {
    companion object {
        const val RESULT_ENABLE = 10001
    }

    private lateinit var adapter: BasePagerAdapter
    private val viewModel by inject<MainViewModel>()
    private var adsView: AdmobNativeAdView? = null

    override fun initView() {
        adapter = BasePagerAdapter(childFragmentManager, 0)
        adapter.addFragment(AppsSuggestFragment(), "")
        adapter.addFragment(AllAppsFragment(), "")
        binding.viewPagerWindows.adapter = adapter

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

    override fun initData() {
        viewModel.windowsShowing.observe(this) {
            if (it) {
                ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                adsView?.loadAd()
            } else {
                ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
            }
            viewModel.windowsFragmentState.value = AppWindowsState.PINED
        }

        viewModel.windowsFragmentState.observe(this) {
            when (it) {
                AppWindowsState.PINED -> {
                    binding.viewPagerWindows.setCurrentItem(0, true)
                }

                AppWindowsState.ALL_APPS -> {
                    binding.viewPagerWindows.setCurrentItem(1, true)
                }
            }
        }
    }

    override fun initListener() {
        binding.root.setOnClickListener {
            viewModel.windowsShowing.value = false
        }

        binding.ivShutdown.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(resources.getString(R.string.launcher_home_title))
            builder.setMessage(resources.getString(R.string.launcher_home_content))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(intent)
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->

            }
            builder.show()

            /*val selector = Intent(Intent.ACTION_MAIN)
            selector.addCategory(Intent.CATEGORY_HOME)
            selector.component =
                ComponentName("android", "com.android.internal.app.ResolverActivity")
            startActivity(selector)*/
        }
    }

    private fun shutDown() {
        val deviceManger: DevicePolicyManager =
            requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(requireActivity(), DeviceAdmin::class.java)
        if (deviceManger.isAdminActive(compName)) {
            deviceManger.lockNow()
        } else {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "You should enable the app!"
            )
            startActivityForResult(intent, RESULT_ENABLE)
        }
    }

    private fun getDeviceName(): String {
        val myDevice: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        return myDevice?.name ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_ENABLE) {
            shutDown()
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAppsWindowsBinding {
        return FragmentAppsWindowsBinding.inflate(inflater, container, false)
    }
}