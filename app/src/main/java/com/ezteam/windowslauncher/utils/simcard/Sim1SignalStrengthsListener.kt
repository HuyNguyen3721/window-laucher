package com.ezteam.windowslauncher.utils.simcard

import android.content.Context
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.view.View
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutFastControlBinding

class Sim1SignalStrengthsListener(subId: Int) : PhoneStateListener() {
    lateinit var binding: LayoutFastControlBinding
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        super.onSignalStrengthsChanged(signalStrength)
        binding.ivMobileData.visibility = View.VISIBLE
        if (!checkAirplaneMode(binding.ivMobileData.context)) {
            when (getSignalStrengthsLevel(signalStrength)) {
                0 -> binding.ivMobileData.setImageResource(R.drawable.ic_not_signal_sim)
                1 -> binding.ivMobileData.setImageResource(R.drawable.ic_signal_sim_1)
                2 -> binding.ivMobileData.setImageResource(R.drawable.ic_signal_sim_2)
                3 -> binding.ivMobileData.setImageResource(R.drawable.ic_signal_sim_3)
                4 -> binding.ivMobileData.setImageResource(R.drawable.ic_signal_sim_full)
            }
        }
    }

    private fun getSignalStrengthsLevel(signalStrength: SignalStrength): Int {
        var level = -1
        try {
            val levelMethod = SignalStrength::class.java.getDeclaredMethod("getLevel")
            level = levelMethod.invoke(signalStrength) as Int
        } catch (e: Exception) {
        }
        return level
    }

    init {
        ReflectUtil.setFieldValue(this, "mSubId", subId)
    }

    private fun checkAirplaneMode(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}