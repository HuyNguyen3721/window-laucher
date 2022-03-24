package com.ezteam.windowslauncher.screen.widget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.daimajia.androidanimations.library.Techniques
import com.ezteam.baseproject.activity.BaseActivity
import com.ezteam.baseproject.extensions.getHeightStatusBar
import com.ezteam.baseproject.extensions.lifecycleOwner
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.ViewUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.databinding.LayoutWindowsWidgetBinding
import com.ezteam.windowslauncher.model.weather.WeatherInfoShowModel
import com.ezteam.windowslauncher.model.weather.WeatherInfoShowModelImpl
import com.ezteam.windowslauncher.utils.center.LocationFilterUtil
import com.ezteam.windowslauncher.utils.toC
import com.ezteam.windowslauncher.utils.toF
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.ezteam.windowslauncher.viewmodel.WeatherInfoViewModel
import com.google.android.gms.ads.ez.nativead.AdmobNativeAdView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*


class WindowsWidgetLayout(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs), KoinComponent {
    private val viewModel by inject<MainViewModel>()
    private val binding = LayoutWindowsWidgetBinding.bind(
        LayoutInflater.from(context).inflate(R.layout.layout_windows_widget, this)
    )

    private lateinit var modelWeather: WeatherInfoShowModel
    private val viewModelWeather by inject<WeatherInfoViewModel>()
    private var locationManager: LocationManager? = null
    private var myLocationListener: LocationListener? = null
    private var adsView: AdmobNativeAdView? = null

    companion object {
        private const val TYPE_WEATHER = "TYPE_WEATHER"
        private const val TIME_REQUEST = "TIME_REQUEST"
        private const val ICON = "ICON"
        private const val LOCATION = "LOCATION"
        private const val TEMPERATURE = "TEMPERATURE"
    }

    private var currentOrientation = Configuration.ORIENTATION_PORTRAIT

    private val countDownTimer = object : CountDownTimer(1000, 100) {
        override fun onTick(millisUntilFinished: Long) {
            val currentTime = System.currentTimeMillis()
            val time = DateUtils.longToDateString(currentTime, DateUtils.TIME_FORMAT_1)
            binding.tvTime.text = time
        }

        override fun onFinish() {
            start()
        }
    }

    init {
        initView()
        initData()
        initListener()
        overrideOrientation()
    }

    private fun initView() {
        val padding = resources.getDimensionPixelSize(R.dimen._10sdp)
        binding.parent.setPadding(
            padding,
            (context as AppCompatActivity).getHeightStatusBar() + padding,
            padding,
            padding
        )
        countDownTimer.start()
        // // initialize model
        modelWeather = WeatherInfoShowModelImpl(binding.tvTime.context)
        // initialize ViewModel

        //Add ads
        AdmobNativeAdView.getNativeAd(
            context,
            R.layout.native_admob_item_widget,
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
            viewModel.widgetShowing.observe(it) {
                if (it) {
                    // request API
                    (context as BaseActivity<*>).requestPermission(complete = { allow ->
                        if (allow) {
                            if (LocationFilterUtil.detectLocation(context)) {
                                requestIPA()
                            } else {
                                context.startActivity(Intent("android.settings.LOCATION_SOURCE_SETTINGS"))
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.find_location_here),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, Manifest.permission.ACCESS_FINE_LOCATION)

                    binding.calendarView.setDate(System.currentTimeMillis(), false, true)

                    ViewUtils.showViewBase(
                        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) Techniques.SlideInLeft
                        else Techniques.SlideInDown,
                        binding.root,
                        200
                    )

                    adsView?.loadAd()
                } else {
                    ViewUtils.hideViewBase(
                        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) Techniques.SlideOutLeft
                        else Techniques.SlideOutUp,
                        binding.root,
                        200
                    )
                }
            }
            // live data weather
            viewModelWeather.weatherInfoLiveData.observe(it, { data ->
                PreferencesUtils.putString(ICON, data.weatherConditionIconUrl)
                PreferencesUtils.putString(TEMPERATURE, data.temperature)
                PreferencesUtils.putString(LOCATION, data.cityAndCountry)
                bindWeather(data.weatherConditionIconUrl, data.temperature, data.cityAndCountry)
            })

            viewModelWeather.weatherInfoFailureLiveData.observe(it, { error ->
                Toast.makeText(context, "Load fail : $error", Toast.LENGTH_SHORT).show()
            })
        }

    }

    private fun requestIPA() {
        val calender = Calendar.getInstance()
        val time = calender.timeInMillis - PreferencesUtils.getLong(TIME_REQUEST, 0)
        if (PreferencesUtils.getLong(TIME_REQUEST, 0) == 0L || time >= 0) {
            requestAPI()
        } else {
            val iconUrl = PreferencesUtils.getString(ICON)
            val temperature = PreferencesUtils.getString(TEMPERATURE)
            val location = PreferencesUtils.getString(LOCATION)
            bindWeather(iconUrl, temperature, location)
        }
    }

    private fun bindWeather(iconUrl: String, temperature: String, location: String) {
        binding.layoutViewWeather.icWeather.setImageResource(getIconWeather(iconUrl))
        //
        if (PreferencesUtils.getString(
                TYPE_WEATHER,
                "C"
            ) == "C"
        ) {
            binding.layoutViewWeather.txtTemperature.text = temperature
            setColorC()
        } else {
            binding.layoutViewWeather.txtTemperature.text = try {
                temperature.toInt().toF().toString()
            } catch (e: NumberFormatException) {
                ""
            }

            setColorF()
        }
        //
        binding.layoutViewWeather.txtAddress.text = location
    }

    private fun initListener() {
        binding.layoutViewWeather.icC.setOnClickListener {
            setC()
            PreferencesUtils.putString(TYPE_WEATHER, "C")
            setColorC()
        }
        binding.layoutViewWeather.icF.setOnClickListener {
            setF()
            PreferencesUtils.putString(TYPE_WEATHER, "F")
            setColorF()
        }
    }

    private fun overrideOrientation() {
        currentOrientation = resources.configuration.orientation
        val padding = resources.getDimensionPixelSize(R.dimen._10sdp)
        when (currentOrientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.parent.setPadding(
                    padding,
                    (context as AppCompatActivity).getHeightStatusBar() + padding,
                    padding,
                    padding
                )
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                binding.parent.setPadding(padding, padding, padding, padding)
            }
        }

        viewModel.orientationLiveData.value = currentOrientation
    }


    private fun getIconWeather(iconUrl: String): Int {
        return when (iconUrl) {
            "01d" -> R.drawable.ic_sun
            "01n" -> R.drawable.ic_mon
            "02d" -> R.drawable.ic_cloudy_light
            "02n" -> R.drawable.ic_cloudy_dark
            "03d", "03n" -> R.drawable.ic_cloudy
            "04d", "04n" -> R.drawable.ic_mostly_cloudy_light
            "09d", "09n" -> R.drawable.ic_heavy_rain_light
            "10d" -> R.drawable.ic_rainyday_light
            "10n" -> R.drawable.ic_rainyday_dark
            "11d", "11n" -> R.drawable.ic_thunderstorm_light
            "13d", "13n" -> R.drawable.ic_snow
            "50d", "50n" -> R.drawable.ic_mist
            else -> 0
        }

    }

    private fun setC() {
        val temperature = try {
            binding.layoutViewWeather.txtTemperature.text.toString().toInt()
        } catch (e: NumberFormatException) {
            0
        }
        if (PreferencesUtils.getString(
                TYPE_WEATHER,
                "C"
            ) == "F"
        ) {
            binding.layoutViewWeather.txtTemperature.text = temperature.toC().toString()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestAPI() {
        // get location
        locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        myLocationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            }

            override fun onProviderEnabled(p0: String?) {
            }

            override fun onProviderDisabled(p0: String?) {
            }

        }
        if (isGPSEnabled(context)) {
            try {
                requestLocationUpdate(LocationManager.GPS_PROVIDER)
            } catch (e: IllegalArgumentException) {
                // not internet
            } catch (e: SecurityException) {
            }
        }
        if (isInternetConnected(context)) {
            try {
                requestLocationUpdate(LocationManager.NETWORK_PROVIDER)
            } catch (e: IllegalArgumentException) {
                // not internet
            } catch (e: SecurityException) {
            }
        }

        try {
            getLatLogFromLocation()
        } catch (e: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate(provider: String) {
        locationManager?.requestLocationUpdates(
            provider,
            1000,
            1F,
            myLocationListener!!
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLatLogFromLocation() {
        locationManager?.let { locationManager ->
            var location: Location? = null
            if (isGPSEnabled(context)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (location == null && isInternetConnected(context)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            location?.let { location ->
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                PreferencesUtils.putLong(TIME_REQUEST, calendar.timeInMillis)
                callAPIWeatherByLatLon(location.latitude, location.longitude)
            } ?: kotlin.run {
                Toast.makeText(
                    context,
                    context?.getString(R.string.ERROR_WEATHER),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isInternetConnected(ctx: Context): Boolean {
        val connectivityMgr: ConnectivityManager = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi: NetworkInfo? = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobile: NetworkInfo? = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        wifi?.let {
            if (it.isConnected) {
                return true
            }
        }

        mobile?.let {
            if (it.isConnected) {
                return true
            }
        }

        return false
    }

    private fun isGPSEnabled(mContext: Context): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun callAPIWeatherByLatLon(lat: Double, lon: Double) {
        viewModelWeather.getWeatherInfoByLatLon(
            lat, lon,
            modelWeather
        ) // fetch weather info
    }

    private fun setF() {
        val temperature = try {
            binding.layoutViewWeather.txtTemperature.text.toString().toInt()
        } catch (e: NumberFormatException) {
            0
        }
        if (PreferencesUtils.getString(
                TYPE_WEATHER,
                "C"
            ) == "C"
        ) {
            binding.layoutViewWeather.txtTemperature.text = temperature.toF().toString()
        }
    }

    private fun setColorC() {
        binding.layoutViewWeather.icC.setColorFilter(
            Color.WHITE,
            PorterDuff.Mode.SRC_IN
        )
        binding.layoutViewWeather.icF.setColorFilter(
            Color.parseColor("#99FFFFFF"),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun setColorF() {
        binding.layoutViewWeather.icF.setColorFilter(
            Color.WHITE,
            PorterDuff.Mode.SRC_IN
        )
        binding.layoutViewWeather.icC.setColorFilter(
            Color.parseColor("#99FFFFFF"),
            PorterDuff.Mode.SRC_IN
        )
    }
}