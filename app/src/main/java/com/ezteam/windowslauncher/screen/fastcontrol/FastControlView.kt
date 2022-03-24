package com.ezteam.windowslauncher.screen.fastcontrol

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.broadcast.*
import com.ezteam.windowslauncher.databinding.LayoutFastControlBinding
import com.ezteam.windowslauncher.utils.center.BluetoothFilterUtil
import com.ezteam.windowslauncher.utils.center.VibrateFilterUtil
import com.ezteam.windowslauncher.utils.center.WifiFilterUtil
import com.ezteam.windowslauncher.utils.simcard.Sim1SignalStrengthsListener
import com.ezteam.windowslauncher.utils.simcard.Sim2SignalStrengthsListener
import com.ezteam.windowslauncher.utils.simcard.TelephonyInfo
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Method

class FastControlView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val binding = LayoutFastControlBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_fast_control, this)
    )

    private val viewModel by inject<MainViewModel>()
    private lateinit var telephonyManager: TelephonyManager
    private var telephonyInfo: TelephonyInfo? = null
    private val broadCastAirPlane = BroadCastAirplane()
    private val broadCastSimChange = BroadcastSimChange()
    private val broadCastWifi = BroadCastWifi()
    private val broadCastBluetooth = BroadCastBluetooth()
    private val broadCastVibrate = BroadCastVibrate()
    private val broadCastBattery = BroadCastChangeBattery()
    var mSim2SignalStrengthsListener: Sim2SignalStrengthsListener? = null
    var mSim1SignalStrengthsListener: Sim1SignalStrengthsListener? = null

    init {
        initViews()
        initData()
        initListener()
        initRegister()
    }

    private fun initViews() {
        binding.root.isVisible = false
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyInfo = TelephonyInfo.getInstance(context)
        // sate signal sim
        signalSimState()
        // state signal wifi
        signalWifi()
        // state bluetooth
        stateBluetooth()
        // mute
        stateVolume()
        // battery
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        setPowerBattery(batteryPct, false)
    }

    private fun initData() {
        context.lifecycleOwner()?.let {
            viewModel.fastControlShowing.observe(it) { isShowing ->
                if (isShowing) {
                    ViewUtils.showViewBase(Techniques.SlideInUp, binding.root, 300)
                    signalSimState()
                    signalWifi()
                } else {
                    ViewUtils.hideViewBase(Techniques.SlideOutDown, binding.root, 300)
                }
            }
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener {
            viewModel.fastControlShowing.value = false
        }

        binding.ivMobileData.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val mSubscriptionManager = SubscriptionManager.from(context)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val subsInfoList: List<SubscriptionInfo>? =
                        mSubscriptionManager.activeSubscriptionInfoList
                    if (subsInfoList?.isNotEmpty() == true) {
                        val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.no_sim_card),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.ivWifi.setOnClickListener {
            WifiFilterUtil.longClickFilter(context)
        }

        binding.ivPin.setOnClickListener {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
            context.startActivity(intent)
        }

        binding.ivBluetooth.setOnClickListener {
            BluetoothFilterUtil.longClickFilter(context)
        }

        binding.ivVolume.setOnClickListener {
            // Declare an audio manager
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun initRegister() {
        // airplane
        broadCastAirPlane.binding = binding
        val intentAirplane = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        context.registerReceiver(broadCastAirPlane, intentAirplane)
        // sim sate change
        broadCastSimChange.changeListener = {
            signalSimState()
        }
        val intentSimChange = IntentFilter("android.intent.action.SIM_STATE_CHANGED")
        context.registerReceiver(broadCastSimChange, intentSimChange)
        // wifi
        val intentWifi = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentWifi.addAction(WifiManager.RSSI_CHANGED_ACTION)
        broadCastWifi.listenerConnected = {
            signalWifi()
        }
        context.registerReceiver(broadCastWifi, intentWifi)
        // bluetooth
        broadCastBluetooth.listenerChangeAction = {
            stateBluetooth()
        }
        val intentBluetooth = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(broadCastBluetooth, intentBluetooth)
        // Volume
        broadCastVibrate.listenerChangeAction = {
            stateVolume()
        }
        val intentVolume = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        context.registerReceiver(broadCastVibrate, intentVolume)
        // battery
        broadCastBattery.listenerChangeAction = { i, b ->
            setPowerBattery(i, b)
        }
        val intentBattery = IntentFilter()
        intentBattery.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentBattery.addAction(Intent.ACTION_POWER_CONNECTED)
        intentBattery.addAction(Intent.ACTION_POWER_DISCONNECTED)
        context.registerReceiver(broadCastBattery, intentBattery)
        val statusBattery: Int = context.registerReceiver(broadCastBattery, intentBattery)!!
            .getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (statusBattery == BatteryManager.BATTERY_STATUS_CHARGING || statusBattery == BatteryManager.BATTERY_STATUS_FULL) {
            broadCastBattery.isPowerConnected = true
        } else if (statusBattery == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            broadCastBattery.isPowerConnected = false
        }
    }

    private fun signalSimState() {
        if (checkPhonePermission()) {
            stateSignalSim(telephonyManager)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stateSignalSim(telephonyManager: TelephonyManager) {
        val isEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        if (isEnabled) {
            binding.ivMobileData.setImageResource(R.drawable.ic_airplane)
        } else {
            if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
                if (Build.VERSION.SDK_INT >= 22) {
                    val mSubscriptionManager = SubscriptionManager.from(context)
                    if (mSubscriptionManager.activeSubscriptionInfoList != null) {
                        val subsInfoList: List<SubscriptionInfo> =
                            mSubscriptionManager.activeSubscriptionInfoList
                        when {
                            subsInfoList.size > 1 -> {
                                if (!sim1(telephonyManager)) {
                                    sim2(telephonyManager)
                                }
                            }
                            subsInfoList.size == 1 -> {
                                if (!sim1(telephonyManager)) {
                                    binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                                }
                            }
                            else -> {
                                binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                            }
                        }
                    } else {
                        binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                    }

                } else {
                    if (!sim1(telephonyManager)) {
                        sim2(telephonyManager)
                    }
                }
            } else {
                binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
            }
        }
    }

    private fun checkPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun sim1(mTelephonyManager: TelephonyManager): Boolean {
        if (Build.VERSION.SDK_INT >= 22) {
            val mSubscriptionManager = SubscriptionManager.from(context)
            //
            // check permission
            val sub0 = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0)
            } else {
                return false
            }

            //
            if (sub0 != null) {
                // unregister
                unRegisterPhoneState1()
                mSim1SignalStrengthsListener = Sim1SignalStrengthsListener(sub0.subscriptionId)
                mSim1SignalStrengthsListener?.binding = binding
                mSim1SignalStrengthsListener?.let {
                    mTelephonyManager.listen(it, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                }
                //
                try {
                    telephonyInfo?.let {
                        return it.isSIM1Ready
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                return false
            }
        } else {
            // api < 22
            return when (mTelephonyManager.simState) {
                TelephonyManager.SIM_STATE_READY -> {
                    true
                }
                else -> {
                    false
                }
            }
        }
        return false
    }

    private fun sim2(mTelephonyManager: TelephonyManager) {
        if (Build.VERSION.SDK_INT >= 22) {
            val mSubscriptionManager = SubscriptionManager.from(context)
            val sub0 = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1)
            } else {
                return
            }

            //
            if (sub0 != null) {
                // unregister
                unRegisterPhoneState2()
                mSim2SignalStrengthsListener =
                    Sim2SignalStrengthsListener(sub0.subscriptionId)
                mSim2SignalStrengthsListener?.binding = binding
                mSim2SignalStrengthsListener?.let {
                    mTelephonyManager.listen(it, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                }

                if (Build.VERSION.SDK_INT >= 26) {
                    if (telephonyManager.getSimState(1) != TelephonyManager.SIM_STATE_READY) {
                        binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                    }
                }
            } else {
                binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
            }

        } else {
            when (mTelephonyManager.simState) {
                TelephonyManager.SIM_STATE_READY -> {
                    try {
                        if (getOutput(context, "getCarrierName", 1) == null) {
                            binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                        }
                    } catch (e: Exception) {
                        binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                    }
                }
                else -> {
                    binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                }
            }
        }

    }

    private fun getOutput(context: Context, methodName: String, slotId: Int): String? {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val telephonyClass: Class<*>
        var reflectionMethod: String? = null
        var output: String? = null
        try {
            telephonyClass = Class.forName(telephony.javaClass.name)
            for (method in telephonyClass.methods) {
                val name = method.name
                if (name.contains(methodName)) {
                    val params = method.parameterTypes
                    if (params.size == 1 && params[0].name == "int") {
                        reflectionMethod = name
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        if (reflectionMethod != null) {
            try {
                output = getOpByReflection(telephony, reflectionMethod, slotId, false)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return output
    }

    private fun getOpByReflection(
        telephony: TelephonyManager,
        predictedMethodName: String,
        slotID: Int,
        isPrivate: Boolean
    ): String? {

        var result: String? = null
        try {
            val telephonyClass = Class.forName(telephony.javaClass.name)
            val parameter = arrayOfNulls<Class<*>?>(1)
            parameter[0] = Int::class.javaPrimitiveType
            val getSimID: Method? = if (slotID != -1) {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName, *parameter)
                } else {
                    telephonyClass.getMethod(predictedMethodName, *parameter)
                }
            } else {
                if (isPrivate) {
                    telephonyClass.getDeclaredMethod(predictedMethodName)
                } else {
                    telephonyClass.getMethod(predictedMethodName)
                }
            }
            val ob_phone: Any?
            val obParameter = arrayOfNulls<Any>(1)
            obParameter[0] = slotID
            if (getSimID != null) {
                ob_phone = if (slotID != -1) {
                    getSimID.invoke(telephony, obParameter)
                } else {
                    getSimID.invoke(telephony)
                }
                if (ob_phone != null) {
                    result = ob_phone.toString()
                }
            }
        } catch (e: java.lang.Exception) {
            //e.printStackTrace();
            return null
        }
        return result
    }

    //
    private fun signalWifi() {
        if (WifiFilterUtil.detectWifi(context)) {
            when (getWifiLevel()) {
                in 0..25 -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_signal_wifi_1)
                }
                in 26..50 -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_signal_wifi_2)
                }
                in 51..75 -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_signal_wifi_3)
                }
                in 75..100 -> {
                    binding.ivWifi.setImageResource(R.drawable.ic_signal_wifi_full)
                }
            }
        } else {
            binding.ivWifi.setImageResource(R.drawable.ic_not_signal_wifi)
        }

    }

    private fun getWifiLevel(): Int {
        val MIN_RSSI = -100
        val MAX_RSSI = -55
        val numLevels = 101
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val linkSpeed = wifiManager.connectionInfo.rssi
        return when {
            linkSpeed <= MIN_RSSI -> {
                0
            }
            linkSpeed >= MAX_RSSI -> {
                numLevels - 1
            }
            else -> {
                val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
                val outputRange: Float = numLevels - 1F
                return ((linkSpeed - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
            }
        }
    }

    //
    private fun stateBluetooth() {
        if (BluetoothFilterUtil.detectBluetooth(context)) {
            binding.ivBluetooth.setImageResource(R.drawable.ic_bluetooth)
        } else {
            binding.ivBluetooth.setImageResource(R.drawable.ic_bluetooth_off)
        }
    }

    private fun stateVolume() {
        if (VibrateFilterUtil.detectVibrate(context)) {
            binding.ivVolume.setImageResource(VibrateFilterUtil.getIconState(context))
        } else {
            binding.ivVolume.setImageResource(R.drawable.ic_mute)
        }
    }

    private fun setPowerBattery(batteryPct: Int, isPowerConnected: Boolean) {
        binding.ivPinPercent.text = "$batteryPct%"
        when (batteryPct) {
            in 0..20 -> {
                binding.ivPin.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_20
                    else R.drawable.ic_battery_status_20_percent
                )
            }
            in 20..40 -> {
                binding.ivPin.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_40
                    else R.drawable.ic_battery_status_40percent

                )
            }
            in 40..60 -> {
                binding.ivPin.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_60
                    else R.drawable.ic_battery_status_60percent
                )
            }
            in 60..80 -> {
                binding.ivPin.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_battery_80
                    else R.drawable.ic_battery_status_80_percent
                )
            }
            else -> {
                binding.ivPin.setImageResource(
                    if (isPowerConnected) R.drawable.ic_charging_full
                    else R.drawable.ic_battery_status_full
                )
            }
        }
    }

    fun unregister() {
        context.unregisterReceiver(broadCastAirPlane)
        context.unregisterReceiver(broadCastSimChange)
        context.unregisterReceiver(broadCastWifi)
        context.unregisterReceiver(broadCastBluetooth)
        context.unregisterReceiver(broadCastVibrate)
        context.unregisterReceiver(broadCastBattery)
    }

    private fun unRegisterPhoneState1() {
        mSim1SignalStrengthsListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            mSim1SignalStrengthsListener = null
        }
    }

    private fun unRegisterPhoneState2() {
        mSim2SignalStrengthsListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
            mSim2SignalStrengthsListener = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unRegisterPhoneState1()
        unRegisterPhoneState2()
    }
}