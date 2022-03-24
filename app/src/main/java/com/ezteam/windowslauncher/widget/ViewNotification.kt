package com.ezteam.windowslauncher.widget

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.extensions.getHeightStatusBar
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.BackGroundActivity
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterNotification
import com.ezteam.windowslauncher.broadcast.BroadCastListenerNotification
import com.ezteam.windowslauncher.databinding.LayoutCenterBinding
import com.ezteam.windowslauncher.model.ItemNotification
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.center.*
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ViewNotification(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs),
    KoinComponent {

    val binding: LayoutCenterBinding =
        LayoutCenterBinding.bind(LayoutInflater.from(context).inflate(R.layout.layout_center, this))
    private var listNotification = mutableListOf<ItemNotification>()
    val broadCastListener = BroadCastListenerNotification()
    private var contentObserver: ContentObserver? = null

    private lateinit var adapter: AdapterNotification
    private val viewModel by inject<MainViewModel>()
    private var adsView: AdmobNativeAdView? = null

    init {
        initView()
        initData()
        initListener()
        initObserver()
    }

    @SuppressLint("SimpleDateFormat")
    private fun initData() {
        registerContentObserver()

        binding.root.postDelayed({
            context?.sendBroadcast(
                Intent(
                    context.resources.getString(
                        R.string.ACTION_NOTIFICATION_SERVICE
                    )
                )
            )
        }, 350)
    }

    private fun initView() {
        setPadding()
        detectChangeBrightness()

        adapter = AdapterNotification(listNotification, context)
        adapter.listenerMore = {
            listNotification[it].isMore = !listNotification[it].isMore
            adapter.notifyItemChanged(it)
        }
        adapter.listener = {
            listenerNotification(listNotification)
        }
        adapter.listenerRemoveItem = {
            var position = listNotification.indexOf(it)
            val view =
                binding.rclNotification.findViewHolderForAdapterPosition(position)?.itemView
            val animation =
                AnimationUtils.loadAnimation(context, R.anim.anim_remove_item)
            view?.startAnimation(animation)
            Handler().postDelayed({
                position = listNotification.indexOf(it)
                if (position != -1) {
                    if (listNotification[position].isParent && position + 1 < listNotification.size &&
                        listNotification[position].packageName == listNotification[position + 1].packageName
                    ) {
                        listNotification[position + 1].isParent = true
                        adapter.notifyItemChanged(position + 1)
                    }
                    listNotification.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }, 300)
        }
        binding.rclNotification.itemAnimator = SlideInRightAnimator()
        binding.rclNotification.adapter = adapter
        binding.rclNotification.layoutManager = LinearLayoutManager(context)
        broadCastListener.adapter = adapter
        broadCastListener.listDataNotification = listNotification
        broadCastListener.listener = {
            listenerNotification(listNotification)
            binding.rclNotification.scheduleLayoutAnimation()
        }
        val intentFilter = IntentFilter(context.resources.getString(R.string.ACTION_NOTIFICATION))
        context.registerReceiver(broadCastListener, intentFilter)
        binding.txtClearAll.setOnClickListener {
            context.sendBroadcast(Intent(resources.getString(R.string.ACTION_NOTIFICATION_CLEAR_ALL)))
            listenerNotification(listNotification)
        }
        listenerNotification(listNotification)
        loadAds()
    }

    private fun initObserver() {
        context.lifecycleOwner()?.let {
            viewModel.notifyCenterShowing.observe(it) {
                if (it) {
                    context.sendBroadcast(Intent(Config.ACTION_RELOAD))
                    reloadControlBottom()
                    ViewUtils.showViewBase(Techniques.SlideInRight, binding.root, 300)

                    adsView?.loadAd()
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutRight, binding.root, 300)
                }
            }
        }
        //
        context.lifecycleOwner()?.let {
            viewModel.windowManagerShowing.observe(it) { b ->
                Log.d("Huy", "initObserver: $b")
                if (b) {
                    binding.backgroundHide?.visibility = View.VISIBLE
                } else {
                    binding.backgroundHide?.visibility = View.GONE
                }
            }
        }

    }

    private fun reloadControlBottom() {
        ManualFilterUtil.sendIntentManual(context)
        MobileDataFilterUtil.sendIntentMobileData(context)
        RotateFilterUtil.sendIntentRotate(context)
        HotspotFilterUtil.sendIntentHotspot(context)
        LocationFilterUtil.sendIntentLocation(context)
    }

    private fun loadAds() {
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admob_item_notification,
            object : AdmobNativeAdView.NativeAdListener {
                override fun onError() {
                }

                override fun onLoaded(nativeAd: AdmobNativeAdView?) {
                    adsView = nativeAd
                    adapter.adsView = nativeAd
                    adapter.addAds(ItemNotification(isAds = true), 1)
                    Log.d("LoadAds", "Main")
                }

                override fun onClickAd() {
                }
            })
    }

    private fun listenerNotification(listDataNotification: MutableList<ItemNotification>) {
        if (listDataNotification.size == 0) {
            binding.txtNoNotifications.visibility = View.VISIBLE
        } else {
            binding.txtNoNotifications.visibility = View.GONE
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.notifyCenterShowing.value = false
        }

        binding.tvCollapse.setOnClickListener {
            if (binding.layoutViewControl.isShown) {
                ViewUtils.hideView(false, binding.layoutViewControl, 200)
                binding.tvCollapse.text = resources.getString(R.string.expand)
            } else {
                ViewUtils.showView(false, binding.layoutViewControl, 200)
                binding.tvCollapse.text = resources.getString(R.string.collapse)
            }
        }

        binding.seekbarBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            var count = 0
            override fun onProgressChanged(p0: SeekBar?, process: Int, fromUse: Boolean) {
                count++
                Log.d("huy", "Change")
                if (fromUse) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.System.canWrite(context)) {

                            if (count == 1) {
                                showExplanation(
                                    resources.getString(R.string.app_name),
                                    context.getString(R.string.DESCRIBE_REQUEST_WRITE_SETTING)
                                )
                                detectChangeBrightness()
                            }
                        } else {
                            if (process % 5 == 0) {
                                Settings.System.putInt(
                                    context.contentResolver,
                                    Settings.System.SCREEN_BRIGHTNESS,
                                    (process * 255) / 100
                                )
                            }

                        }
                    } else {
                        if (process % 5 == 0) {
                            Settings.System.putInt(
                                context.contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS,
                                (process * 255) / 100
                            )
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                count = 0
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                count = 0
            }
        })
    }

    private fun showExplanation(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.allow) { _, _ ->
                val intent = Intent(context, BackGroundActivity::class.java)
                intent.putExtra("PERMISSION", "WRITE_SETTING")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            .create()
        alertDialog.show()
    }

    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                detectChangeBrightness()
                // do something...
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            false, contentObserver as ContentObserver
        )
    }

    private fun detectChangeBrightness() {
        binding.seekbarBrightness.progress =
            ((Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                255
            ) / 255F) * 100).toInt()
    }

    private fun setPadding() {
        val currentOrientation = resources.configuration.orientation
        val padding = resources.getDimensionPixelSize(R.dimen._10sdp)
        when (currentOrientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.container.setPadding(
                    0,
                    (context as BaseActivity<*>).getHeightStatusBar(),
                    0,
                    0
                )
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                binding.container.setPadding(0, padding, 0, 0)
            }
        }
    }

    fun clearObserver() {
        contentObserver?.let {
            context?.contentResolver?.unregisterContentObserver(
                it
            )
        }
    }
}