package com.ezteam.windowslauncher.screen.windows

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemLauncherAdapter
import com.ezteam.windowslauncher.adapter.ItemLauncherRecentAdapter
import com.ezteam.windowslauncher.databinding.FragmentAppsSuggestBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.popup.AppMenuPopup
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.screen.base.BaseWindowsFragment
import com.ezteam.windowslauncher.utils.launcher.LauncherLockState
import com.ezteam.windowslauncher.viewmodel.AppWindowsState
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.android.ext.android.inject


class AppsSuggestFragment : BaseWindowsFragment<FragmentAppsSuggestBinding>() {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapterPined: ItemLauncherAdapter
    private lateinit var adapterRecent: ItemLauncherRecentAdapter

    override fun initView() {
        adapterPined = ItemLauncherAdapter(
            requireContext(),
            mutableListOf(),
            ::itemLauncherPress,
            ::itemLongClick
        )
        binding.rcvPined.adapter = adapterPined

        adapterRecent = ItemLauncherRecentAdapter(
            requireContext(),
            mutableListOf(),
            ::itemLauncherPress,
            ::itemLongClick
        )
        binding.rcvRecent.adapter = adapterRecent
    }

    override fun initData() {
        viewModel.getLauncherAppsPined().observe(this) {
            adapterPined.setList(it)
            adapterPined.notifyDataSetChanged()
        }

        viewModel.getLauncherAppsRecent().observe(this) {
            adapterRecent.setList(it)
            adapterRecent.notifyDataSetChanged()
        }
    }

    override fun initListener() {
        binding.headerPined.onHeaderClickListener = {
            viewModel.windowsFragmentState.postValue(AppWindowsState.ALL_APPS)
        }
    }

    private fun itemLauncherPress(launcher: LauncherModel) {
        viewModel.updateRecentApp(launcher)
        openApp(launcher)
    }

    private fun itemLongClick(view: View, launcher: LauncherModel) {
        val menuPopup = AppMenuPopup(requireContext(), launcher) {
            when (it) {
                R.id.item_uninstall_app -> {
                    uninstallApp(launcher) {
                        if (it) {
                            viewModel.removeApp(launcher)
                        }
                    }
                }

                R.id.item_create_shortcut -> {
                    viewModel.createShortCut(launcher, requireContext())
                    toast(resources.getString(R.string.create_shortcut_success))
                    Handler().postDelayed({
                        viewModel.windowsShowing.postValue(false)
                    }, 100)
                }

                R.id.item_properties -> {
                    openAppProperties(launcher)
                }

                R.id.item_pin_windows,
                R.id.item_unpin_windows -> {
                    viewModel.pinAndUnpinApp(launcher, success = {
                        if (it) {
                            viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
                            toast(resources.getString(R.string.pinned_app_success))
                        } else {
                            toast(resources.getString(R.string.unpinned_app_success))
                            viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
                        }
                    }, failure = {
                        toast(resources.getString(R.string.pined_app_limited))
                    })
                }

                R.id.item_pin_taskbar,
                R.id.item_unpin_taskbar -> {
                    viewModel.pinAndUnpinTaskbar(launcher) {
                        if (it) {
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.pinned_app_task_success))
                        } else {
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.unpinned_app_task_success))
                        }
                    }
                }

                R.id.item_lock_app,
                R.id.item_unlock_app -> {
                    showInputPassword {
                        viewModel.lockAndUnlockApp(launcher, it) {
                            when (it) {
                                LauncherLockState.LOCK -> {
                                    toast(resources.getString(R.string.create_password_success))
                                }
                                LauncherLockState.UNLOCK -> {
                                    toast(resources.getString(R.string.unlock_app_success))
                                }
                                LauncherLockState.WRONG -> {
                                    toast(resources.getString(R.string.incorrect_password))
                                }
                            }
                        }
                    }
                }
            }
        }
        menuPopup.show(view)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAppsSuggestBinding {
        return FragmentAppsSuggestBinding.inflate(inflater, container, false)
    }
}