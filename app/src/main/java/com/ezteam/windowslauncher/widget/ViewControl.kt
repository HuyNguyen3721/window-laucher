package com.ezteam.windowslauncher.widget

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.AdapterControl
import com.ezteam.windowslauncher.broadcast.*
import com.ezteam.windowslauncher.databinding.LayoutControlBinding
import com.ezteam.windowslauncher.model.ItemControl
import com.ezteam.windowslauncher.screen.MainActivity
import com.ezteam.windowslauncher.utils.center.*
import com.ezteam.windowslauncher.viewmodel.ControlViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ViewControl(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs),
    KoinComponent {
    lateinit var binding: LayoutControlBinding
    private var listControl = mutableListOf<ItemControl>()
    private val viewModel by inject<ControlViewModel>()
    var broadCastAirplane: BroadCastAirplane? = null
    var broadCastWifi: BroadCastWifi? = null
    var broadCastBluetooth: BroadCastBluetooth? = null
    var broadCastManual: BroadCastManual? = null
    var broadCastMobileData: BroadCastMobileData? = null
    var broadCastRotate: BroadCastRotate? = null
    var broadCastHotspot: BroadCastHotspot? = null
    var broadCastLocation: BroadCastLocation? = null
    var broadCastVibrate: BroadCastVibrate? = null
    var contentObserver: ContentObserver? = null
    private lateinit var adapter: AdapterControl

    init {
        initData()
        initView()
        initListener()
        initRegister()
    }

    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_control, this, true)
        binding = LayoutControlBinding.bind(view)
        adapter = AdapterControl(listControl)
        //
        binding.rclControlCenter.adapter = adapter
        unableAminRecycleView(binding.rclControlCenter)
    }

    private fun initListener() {
        adapter.listenerClickItem = { it, pos ->
            when (it) {
                context.resources.getString(R.string.airplane) -> {
                    viewModel.takePhotoLauncher(context as MainActivity)
                    Handler().postDelayed({
                        AirPlaneFilterUtil.clickFilter(context)
                    }, 300)
                }
                context.resources.getString(R.string.wifi) -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !WifiFilterUtil.detectWifi(
                            context
                        )
                    ) {
                        listControl[pos].isClickEnable = true
                        adapter.notifyItemChanged(pos)
                    }
                    viewModel.takePhotoLauncher(context as MainActivity)
                    Handler().postDelayed({
                        WifiFilterUtil.clickFilter(context)
                    }, 300)
                }
                context.resources.getString(R.string.bluetooth) -> {
                    if (!BluetoothFilterUtil.detectBluetooth(context)) {
                        listControl[pos].isClickEnable = true
                        adapter.notifyItemChanged(pos)
                    }
                    BluetoothFilterUtil.clickFilter(context)
                }
                context.resources.getString(R.string.flash) -> {
                    for (item in listControl) {
                        if (item.name == context.resources.getString(R.string.flash)) {
                            if (item.isEnable) {
                                item.isEnable = false
                                FlashFilterUtil.turnFlashlightOff(context)
                            } else {
                                item.isEnable = true
                                FlashFilterUtil.turnFlashlightOn(context)
                            }
                            adapter.notifyItemChanged(listControl.indexOf(item))
                            break
                        }
                    }
                }
                context.resources.getString(R.string.manual) -> {
                    ManualFilterUtil.clickFilter(context)
//                    detectChangeBrightness()
                }
                context.resources.getString(R.string.data) -> {
                    viewModel.takePhotoLauncher(context as MainActivity)
                    Handler().postDelayed({
                        MobileDataFilterUtil.clickFilter(context)
                    }, 300)
                }
                context.getString(R.string.rotate) -> {
                    RotateFilterUtil.clickFilter(context)
                }
                context.getString(R.string.hotspot) -> {
                    viewModel.takePhotoLauncher(context as MainActivity)
                    Handler().postDelayed({
                        HotspotFilterUtil.clickFilter(context)
                    }, 300)
                }
                context.getString(R.string.location) -> {
                    viewModel.takePhotoLauncher(context as MainActivity)
                    Handler().postDelayed({
                        LocationFilterUtil.clickFilter(context)
                    }, 300)
                }
                context.getString(R.string.setting) -> {
                    SettingFilerUtil.onClickFilter(context)
                }
                context.resources.getString(R.string.vibrate),
                context.resources.getString(R.string.mute),
                context.resources.getString(
                    R.string.ring
                ) -> {
                    VibrateFilterUtil.clickFilter(context)
                }
                context.getString(R.string.background) -> {
                    (context as BaseActivity<*>).let { activity ->
                        activity.requestPermission({
                            if (it) {
                                BackgroundFilterUtil.clickFilter(activity)
                            }
                        }, Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
        //
        adapter.listenerLongClickItem = {
            when (it) {
                context.resources.getString(R.string.airplane) -> {
                    AirPlaneFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.wifi) -> {
                    WifiFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.bluetooth) -> {
                    BluetoothFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.flash) -> {
                }
                context.resources.getString(R.string.manual) -> {
                    ManualFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.data) -> {
                    MobileDataFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.rotate) -> {
                    RotateFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.hotspot) -> {
                    HotspotFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.location) -> {
                    LocationFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.setting) -> {
                    SettingFilerUtil.onClickFilter(context)
                }
                context.resources.getString(R.string.vibrate),
                context.resources.getString(R.string.mute),
                context.resources.getString(
                    R.string.ring
                ) -> {
                    VibrateFilterUtil.longClickFilter(context)
                }
                context.resources.getString(R.string.background) -> {
                    BackgroundFilterUtil.clickFilter((context as AppCompatActivity))
                }
            }
        }
    }

    private fun initRegister() {
        registerBroadCastAirplane()
        registerBroadCastWifi()
        registerBroadCastBluetooth()
        registerBroadCastManual()
        registerBroadCastMobileData()
        registerBroadCastRotate()
        registerBroadCastHotspot()
        registerBroadCastLocation()
        registerBroadCastVibrate()
    }

    private fun initData() {
        FlashFilterUtil.turnFlashlightOff(context)
        listControl.apply {
            add(
                ItemControl(
                    R.drawable.ic_airplane,
                    context.resources.getString(R.string.airplane),
                    AirPlaneFilterUtil.detectAirPlane(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_wifi,
                    context.resources.getString(R.string.wifi),
                    WifiFilterUtil.detectWifi(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_bluetooth,
                    context.resources.getString(R.string.bluetooth),
                    BluetoothFilterUtil.detectBluetooth(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_flash,
                    context.resources.getString(R.string.flash),
                    false
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_manual,
                    context.resources.getString(R.string.manual),
                    ManualFilterUtil.detectManual(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_data,
                    context.resources.getString(R.string.data),
                    MobileDataFilterUtil.detectMobileData(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_rotate,
                    context.resources.getString(R.string.rotate),
                    RotateFilterUtil.detectRotate(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_hotspot,
                    context.resources.getString(R.string.hotspot),
                    HotspotFilterUtil.detectHotspot(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_location,
                    context.resources.getString(R.string.location),
                    LocationFilterUtil.detectLocation(context)
                )
            )
            add(ItemControl(R.drawable.ic_setting, context.resources.getString(R.string.setting)))
            add(
                ItemControl(
                    VibrateFilterUtil.getIconState(context),
                    VibrateFilterUtil.getNameState(context),
                    VibrateFilterUtil.detectVibrate(context)
                )
            )
            add(
                ItemControl(
                    R.drawable.ic_background,
                    context.resources.getString(R.string.background)
                )
            )
        }
    }

    private fun unableAminRecycleView(rel: RecyclerView) {
        val animator: RecyclerView.ItemAnimator? = rel.itemAnimator
        animator?.let {
            if (it is SimpleItemAnimator) {
                (it).supportsChangeAnimations = false
            }
        }//
    }

    private fun registerBroadCastAirplane() {
        broadCastAirplane = BroadCastAirplane()
        broadCastAirplane?.lisDataControl = listControl
        broadCastAirplane?.adapter = adapter
        val intent = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        context.registerReceiver(broadCastAirplane, intent)
    }

    private fun registerBroadCastWifi() {
        broadCastWifi = BroadCastWifi()
        broadCastWifi?.lisDataControl = listControl
        broadCastWifi?.adapter = adapter
        val intent = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(broadCastWifi, intent)
    }

    private fun registerBroadCastBluetooth() {
        broadCastBluetooth = BroadCastBluetooth()
        broadCastBluetooth?.lisDataControl = listControl
        broadCastBluetooth?.adapter = adapter
        val intent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(broadCastBluetooth, intent)
    }

    private fun registerBroadCastManual() {
        broadCastManual = BroadCastManual()
        broadCastManual?.lisDataControl = listControl
        broadCastManual?.adapter = adapter
        val intent = IntentFilter(context.resources.getString(R.string.ACTION_MAMUAL))
        context.registerReceiver(broadCastManual, intent)
    }

    private fun registerBroadCastMobileData() {
        broadCastMobileData = BroadCastMobileData()
        broadCastMobileData?.lisDataControl = listControl
        broadCastMobileData?.adapter = adapter
        val intent = IntentFilter(context.resources.getString(R.string.ACTION_MOBILE_DATA))
        context.registerReceiver(broadCastMobileData, intent)
    }

    private fun registerBroadCastRotate() {
        broadCastRotate = BroadCastRotate()
        broadCastRotate?.lisDataControl = listControl
        broadCastRotate?.adapter = adapter
        val intent = IntentFilter(context.resources.getString(R.string.ACTION_ROTATE))
        context.registerReceiver(broadCastRotate, intent)
    }

    private fun registerBroadCastHotspot() {
        broadCastHotspot = BroadCastHotspot()
        broadCastHotspot?.lisDataControl = listControl
        broadCastHotspot?.adapter = adapter
        val intent = IntentFilter(context.resources.getString(R.string.ACTION_HOTSPOT))
        context.registerReceiver(broadCastHotspot, intent)
    }

    private fun registerBroadCastLocation() {
        broadCastLocation = BroadCastLocation()
        broadCastLocation?.lisDataControl = listControl
        broadCastLocation?.adapter = adapter
        val intent = IntentFilter(context.resources.getString(R.string.ACTION_LOCATION))
        context.registerReceiver(broadCastLocation, intent)
    }

    private fun registerBroadCastVibrate() {
        broadCastVibrate = BroadCastVibrate()
        broadCastVibrate?.lisDataControl = listControl
        broadCastVibrate?.adapter = adapter
        val intent = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        context.registerReceiver(broadCastVibrate, intent)
    }

    fun clearBroadCast() {
        context?.unregisterReceiver(broadCastAirplane)
        context?.unregisterReceiver(broadCastWifi)
        context?.unregisterReceiver(broadCastBluetooth)
        context?.unregisterReceiver(broadCastManual)
        context?.unregisterReceiver(broadCastMobileData)
        context?.unregisterReceiver(broadCastRotate)
        context?.unregisterReceiver(broadCastHotspot)
        context?.unregisterReceiver(broadCastLocation)
        context?.unregisterReceiver(broadCastVibrate)
    }
}