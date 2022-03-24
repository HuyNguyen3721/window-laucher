package com.ezteam.windowslauncher.screen.windows

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemHeaderSearchAdapter
import com.ezteam.windowslauncher.adapter.ItemLauncherVerticalAdapter
import com.ezteam.windowslauncher.databinding.FragmentAllAppsBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.popup.AppMenuPopup
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.screen.base.BaseWindowsFragment
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.utils.launcher.LauncherLockState
import com.ezteam.windowslauncher.viewmodel.AppWindowsState
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.android.ext.android.inject

class AllAppsFragment : BaseWindowsFragment<FragmentAllAppsBinding>() {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: ItemLauncherVerticalAdapter
    private lateinit var headerAdapter: ItemHeaderSearchAdapter

    override fun initView() {
        adapter = ItemLauncherVerticalAdapter(
            requireContext(),
            mutableListOf(),
            this::itemLauncherPress,
            this::itemLongClick,
            this::itemHeaderPress
        )
        binding.rcvApps.adapter = adapter

        headerAdapter = ItemHeaderSearchAdapter(
            requireContext(),
            mutableListOf(),
            mutableListOf(),
            this::itemHeaderSearchPress
        )
        binding.rcvHeader.adapter = headerAdapter
    }

    override fun initData() {
        viewModel.getLauncherApps().observe(this) {
            LauncherAppUtils.configFlagShowHeader(it)
            adapter.setList(it)
            adapter.notifyDataSetChanged()

            headerAdapter.setList(LauncherAppUtils.listHeaderSearch())
            headerAdapter.headersEnable = LauncherAppUtils.listHeader(it)
            headerAdapter.notifyDataSetChanged()
        }

        viewModel.windowsHeight.observe(this) {
            binding.root.apply {
                layoutParams.height = it
            }
        }

        viewModel.windowsFragmentState.observe(this) {
            if (it == AppWindowsState.PINED) {
                ViewUtils.hideViewBase(Techniques.ZoomOut, binding.rcvHeader, 200)
                ViewUtils.showViewBase(Techniques.ZoomIn, binding.rcvApps, 200)
                binding.rcvApps.scrollToPosition(0)
            }
        }
    }

    override fun initListener() {
        binding.headerBack.onHeaderClickListener = {
            viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
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
//                        viewModel.windowsShowing.postValue(false)
                    }, 100)
                }

                R.id.item_pin_windows,
                R.id.item_unpin_windows -> {
                    viewModel.pinAndUnpinApp(launcher, success = {
                        if (it) {
                            toast(resources.getString(R.string.pinned_app_success))
                            binding.root.postDelayed({
                                viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
                            }, 100)
                        } else {
                            toast(resources.getString(R.string.unpinned_app_success))
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

                R.id.item_properties -> {
                    openAppProperties(launcher)
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

    private fun itemHeaderPress(launcher: LauncherModel) {
        ViewUtils.hideViewBase(Techniques.ZoomOut, binding.rcvApps, 200)
        ViewUtils.showViewBase(Techniques.ZoomIn, binding.rcvHeader, 200)
    }

    private fun itemHeaderSearchPress(header: String) {
        ViewUtils.hideViewBase(Techniques.ZoomOut, binding.rcvHeader, 200)
        ViewUtils.showViewBase(Techniques.ZoomIn, binding.rcvApps, 200)

        if (header == "#") {
            binding.rcvApps.scrollToPosition(0)
        } else {
            val position = adapter.list.indexOfFirst {
                it.appName.startsWith(header)
            }
            binding.rcvApps.apply {
                layoutManager as LinearLayoutManager
                (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAllAppsBinding {
        return FragmentAllAppsBinding.inflate(inflater, container, false)
    }
}