package com.ezteam.windowslauncher.screen.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.KeyboardUtils
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemLauncherAdapter
import com.ezteam.windowslauncher.databinding.LayoutSearchAppsBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.popup.AppMenuPopup
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.utils.launcher.LauncherLockState
import com.ezteam.windowslauncher.viewmodel.AppWindowsState
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SearchAppsLayout(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val viewModel by inject<MainViewModel>()
    private lateinit var adapter: ItemLauncherAdapter
    private val binding = LayoutSearchAppsBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_search_apps, this)
    )
    private var adsView: AdmobNativeAdView? = null

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
            viewModel.searchShowing.observe(it) {
                if (it) {
                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                    adsView?.loadAd()
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
                    KeyboardUtils.hideSoftKeyboard((context as AppCompatActivity))
                }
            }

            viewModel.getAppsByFilter().observe(it) {
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun initListener() {
        binding.edtSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.searchTextLiveData.value = text.toString()
        })

        binding.root.setOnClickListener {
            viewModel.searchShowing.value = false
        }

        context.lifecycleOwner()?.let {
            setEventListener(
                (context as AppCompatActivity),
                it,
                KeyboardVisibilityEventListener {
                    val paddingNormal = resources.getDimensionPixelOffset(R.dimen._15sdp)
                    val paddingLarge = resources.getDimensionPixelOffset(R.dimen._150sdp)
                    if (it) {
                        binding.root.setPadding(
                            paddingNormal,
                            paddingNormal,
                            paddingNormal,
                            paddingLarge
                        )
                    } else {
                        binding.root.setPadding(paddingNormal)
                    }
                    Log.d("Keyboard", it.toString())
                })
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
        val menuPopup = AppMenuPopup(context, launcher) {
            when (it) {
                R.id.item_create_shortcut -> {
                    Toast.makeText(context, resources.getString(R.string.create_shortcut_success), Toast.LENGTH_SHORT).show()
                    viewModel.createShortCut(launcher, context)
                }

                R.id.item_properties -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", launcher.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }

                R.id.item_uninstall_app -> {
                    (context as BaseWindowsActivity<*>).uninstallApp(launcher) {
                        if (it) {
                            viewModel.removeApp(launcher)
                        }
                    }
                }

                R.id.item_pin_windows,
                R.id.item_unpin_windows -> {
                    viewModel.pinAndUnpinApp(launcher, success = {
                        if (it) {
                            viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.pinned_app_success))
                        } else {
                            (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.unpinned_app_success))
                            viewModel.windowsFragmentState.postValue(AppWindowsState.PINED)
                        }
                    }, failure = {
                        (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.pined_app_limited))
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
                    (context as BaseWindowsActivity<*>).showInputPassword {
                        viewModel.lockAndUnlockApp(launcher, it) {
                            when (it) {
                                LauncherLockState.LOCK -> {
                                    (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.create_password_success))
                                }
                                LauncherLockState.UNLOCK -> {
                                    (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.unlock_app_success))
                                }
                                LauncherLockState.WRONG -> {
                                    (context as BaseWindowsActivity<*>).toast(resources.getString(R.string.incorrect_password))
                                }
                            }
                        }
                    }
                }
            }
        }
        menuPopup.show(view)
    }
}