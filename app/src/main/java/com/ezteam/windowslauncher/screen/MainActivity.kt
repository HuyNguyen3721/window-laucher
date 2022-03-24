package com.ezteam.windowslauncher.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.*
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezteam.baseproject.extensions.getDisplayMetrics
import com.ezteam.baseproject.extensions.getHeightStatusBar
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.adapter.ItemLauncherDragAdapter
import com.ezteam.windowslauncher.broadcast.BroadCastChangeBackGround
import com.ezteam.windowslauncher.databinding.ActivityMainBinding
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.SpecialPackage
import com.ezteam.windowslauncher.popup.AppDesktopMenuPopup
import com.ezteam.windowslauncher.popup.DesktopMenuPopup
import com.ezteam.windowslauncher.popup.SortMenuPopup
import com.ezteam.windowslauncher.popup.TaskbarMenuPopup
import com.ezteam.windowslauncher.screen.base.BaseWindowsActivity
import com.ezteam.windowslauncher.service.NotificationListener
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.center.BackgroundFilterUtil
import com.ezteam.windowslauncher.utils.launcher.DesktopLauncherSortState
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.utils.launcher.LauncherLockState
import com.ezteam.windowslauncher.utils.launcher.LauncherType
import com.ezteam.windowslauncher.utils.swiftp.FTPServerService
import com.ezteam.windowslauncher.viewmodel.FileManagerViewModel
import com.ezteam.windowslauncher.viewmodel.MainViewModel
import com.ezteam.windowslauncher.widget.pcView.MyPcView
import com.google.android.gms.ads.ez.EzAdControl
import com.google.android.gms.ads.ez.analytics.FirebaseAnalTool
import com.google.android.gms.ads.ez.analytics.FlurryAnalytics
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension
import java.io.File


class MainActivity : BaseWindowsActivity<ActivityMainBinding>() {
    companion object {
        var isNotifyPermission = false
        var isAutoClicking = false
        private const val ACTION_CHANGE_BACKGROUND = "ACTION_CHANGE_BACKGROUND"
        private const val TAG = "MAIN_ACTIVITY"
    }

    private val viewModel by inject<MainViewModel>()
    private val fileManagerViewModel by inject<FileManagerViewModel>()

    private lateinit var launcherAdapter: ItemLauncherDragAdapter
    private var filePath: String? = null
    private var broadCastBackGround: BroadCastChangeBackGround? = null
    private lateinit var myPcView: MyPcView
    private var desktopMenuPopup: DesktopMenuPopup? = null
    private var appDesktopMenuPopup: AppDesktopMenuPopup? = null

    override fun initView() {
        isNotifyPermission = false
        // Screen full
        setAppActivityFullScreenOver(this)
        binding.parent.setPadding(0, getHeightStatusBar(), 0, 0)

        // Set background
        changeBackground()
        //register broadcast ivBackground
        broadCastBackGround = BroadCastChangeBackGround()
        broadCastBackGround?.mainActivity = this
        val intentFilter = IntentFilter(ACTION_CHANGE_BACKGROUND)
        registerReceiver(broadCastBackGround, intentFilter)
        // Launcher
        launcherAdapter =
            ItemLauncherDragAdapter(
                this,
                mutableListOf(),
                this::itemLauncherPress,
                this::itemLauncherLongPress,
                this::itemDragAndDrop,
                this::itemOnMove
            )
        binding.viewPagerWindows.adapter = launcherAdapter

        launcherAdapter.itemTouchHelper.attachToRecyclerView(binding.viewPagerWindows)
        binding.viewPagerWindows.setHasFixedSize(true)
        binding.viewPagerWindows.adapter = launcherAdapter
    }

    @SuppressLint("WrongConstant")
    private fun resizeScreen() {
        binding.viewPagerWindows.layoutManager = GridLayoutManager(
            this, LauncherAppUtils.getSizeGrid(this).rows,
            LinearLayout.HORIZONTAL, false
        )
    }

    override fun initData() {
        viewModel.getDesktopApps(this).observe(this) {
            if (it.isEmpty()) return@observe
            launcherAdapter.setList(it)
            launcherAdapter.notifyDataSetChanged()
        }

        viewModel.resizeGridLauncher.observe(this) {
            resizeScreen()
        }
    }

    private fun initPcView() {
        myPcView = MyPcView(this)
        myPcView.closeListener = {
            binding.fragmentContainer.removeAllViews()
            initPcView()
        }
    }

    @KoinApiExtension
    override fun initListener() {
        binding.taskbar.taskbarItemClickListener = {
            when (it) {
                SpecialPackage.CENTER_PACKAGE.packageId -> {
                    if (isNotificationServiceRunning(this)) {
                        showHideScreen(it)
                    } else {
                        isNotifyPermission = true
                        startNotifyPermission()
                    }
                }

                SpecialPackage.SEARCH_PACKAGE.packageId,
                SpecialPackage.CALENDAR_PACKAGE.packageId,
                SpecialPackage.WINDOWS_PACKAGE.packageId,
                SpecialPackage.WIDGETS_PACKAGE.packageId,
                SpecialPackage.TASKBAR_GROUP_APPS.packageId,
                SpecialPackage.FAST_CONTROL_PACKAGE.packageId -> {
                    showHideScreen(it)
                }

            }
        }

        binding.taskbar.taskbarAppItemClickListener = { launcherModel, view ->
            when (launcherModel.packageName) {
                SpecialPackage.SEARCH_PACKAGE.packageId,
                SpecialPackage.WINDOWS_PACKAGE.packageId,
                SpecialPackage.WIDGETS_PACKAGE.packageId -> {
                    showHideScreen(launcherModel.packageName)
                }
                else -> {
                    openApp(launcherModel)
                }
            }
        }

        binding.taskbar.taskbarAppItemLongClickListener = { launcherModel, view ->
            val taskbarPopup = TaskbarMenuPopup(this) { popupWindow, view ->
                when (view.id) {
                    R.id.item_open -> {
                        openApp(launcherModel)
                    }
                    R.id.item_unpin_taskbar -> {
                        viewModel.pinAndUnpinTaskbar(launcherModel) {
                            if (it) {
                                toast(resources.getString(R.string.pinned_app_task_success))
                            } else {
                                toast(resources.getString(R.string.unpinned_app_task_success))
                            }
                        }
                    }
                }
            }

            taskbarPopup.show(binding.taskbar, view.x.toInt(), view.height)
        }

        binding.viewPagerWindows.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                Log.d("Event", e.action.toString())
                if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
                    return false
                }
                val child: View? =
                    rv.findChildViewUnder(e.x, e.y)
                return if (child != null) {
                    // tapped on child
                    false
                } else {
                    gestureDetector.onTouchEvent(e)
                    return true
                }
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                if (e.action == MotionEvent.ACTION_UP) {
                    gestureDetector.onTouchEvent(e)
                }
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }

        })
    }

    val gestureDetector: GestureDetector =
        GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) {
                binding.root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                val desktopMenu = createDesktopMenuPopup()

                desktopMenu.showAtLocation(
                    binding.viewPagerWindows,
                    e?.x?.toInt() ?: 0,
                    e?.y?.toInt() ?: 0
                )
            }
        })

    private fun showHideScreen(packageId: String) {
        showHideWindowsFragment(packageId != SpecialPackage.WINDOWS_PACKAGE.packageId)
        showHideSearchFragment(packageId != SpecialPackage.SEARCH_PACKAGE.packageId)
        showHideFragmentCenter(packageId != SpecialPackage.CENTER_PACKAGE.packageId)
        showHideCalendarView(packageId != SpecialPackage.CALENDAR_PACKAGE.packageId)
        showHideFastControlView(packageId != SpecialPackage.FAST_CONTROL_PACKAGE.packageId)
        showHideWidgetFragment(packageId != SpecialPackage.WIDGETS_PACKAGE.packageId)
        showHideTaskbarAppsFragment(packageId != SpecialPackage.TASKBAR_GROUP_APPS.packageId)
        viewModel.settingShowing.value = false
    }

    private fun itemLauncherPress(launcher: LauncherModel) {
        when (launcher.packageName) {
            SpecialPackage.THIS_PC_PACKAGE.packageId -> {
                showHideFolderFragment()
            }

            SpecialPackage.USER_PACKAGE.packageId -> {
                showHideFolderFragment(Config.FileManager.thisUserPath)
            }

            SpecialPackage.THEME_PACKAGE.packageId -> {
                openWallpaperSetting()
            }

            else -> {
                if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                    viewModel.updateRecentApp(launcher)
                    openApp(launcher)
                } else if (launcher.launcherType == LauncherType.APP_FOLDER.value) {
                    showHideFolderFragment(launcher.packageName)
                }
            }
        }
//        toast(launcher.packageName)
    }

    private fun itemLauncherLongPress(view: View, launcher: LauncherModel) {
        if (launcher.launcherType == LauncherType.APP_IDLE.value) {
            desktopMenuPopup = createDesktopMenuPopup()
            desktopMenuPopup?.show(view, view.width / 2)
        } else {
            appDesktopMenuPopup = AppDesktopMenuPopup(this, launcher) {
                when (it) {
                    R.id.item_open -> {
                        itemLauncherPress(launcher)
                    }

                    R.id.item_remove -> {
                        if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                            viewModel.removeShortCut(launcher)
                        } else if (launcher.launcherType == LauncherType.APP_FOLDER.value) {
                            fileManagerViewModel.deleteFile(launcher.packageName)
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

                    R.id.item_properties -> {
                        if (launcher.launcherType == LauncherType.APP_SYSTEM.value) {
                            openAppProperties(launcher)
                        } else if (launcher.launcherType == LauncherType.APP_FOLDER.value) {
                            showPropertiesFile(File(launcher.packageName))
                        }
                    }

                    R.id.item_rename -> {
                        showInputFolderName(false) {
                            if (launcher.launcherType == LauncherType.APP_FOLDER.value) {
                                fileManagerViewModel.renameFile(File(launcher.packageName), it)
                            } else {
                                if (it.isNotEmpty()) {
                                    viewModel.renameApp(launcher, it)
                                    toast(resources.getString(R.string.rename_success))
                                }
                            }
                        }
                    }
                }
            }
            appDesktopMenuPopup?.show(view, view.width / 2)
        }
    }

    private fun itemDragAndDrop(apps: MutableList<LauncherModel>) {
        apps.filter {
            it.launcherType != LauncherType.APP_IDLE.value
        }.let {
            viewModel.updateDesktopLauncher(it)
        }
        PreferencesUtils.putInteger(PresKey.SORT_STATE, DesktopLauncherSortState.POSITION.value)
    }

    private fun itemOnMove() {
        appDesktopMenuPopup?.dismiss()

        desktopMenuPopup?.dismiss()
    }

    private fun showHideFolderFragment(folderPath: String? = null) {
        requestPermission(
            {
                if (it) {
                    if (binding.fragmentContainer.childCount == 0) {
                        initPcView()
                        binding.fragmentContainer.addView(
                            myPcView,
                            ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    } else {
                        myPcView.showHideView(true)
                    }

                    folderPath?.let {


                        if (it == Config.FileManager.thisUserPath) {
                            myPcView.openUserFolder()
                        } else {
                            myPcView.openFolderByPath(folderPath)
                        }
                    } ?: kotlin.run {
                        myPcView.openPcFolder()
                    }
                    EzAdControl.getInstance(this).showAds()
                }
            },
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun showHideWindowsFragment(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.windowsShowing.value = false
        } else {
            viewModel.windowsShowing.value?.let {
                viewModel.windowsShowing.value = !it
            }
        }
    }

    private fun showHideSearchFragment(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.searchShowing.value = false
        } else {
            viewModel.searchShowing.value?.let {
                viewModel.searchShowing.value = !it
            }
        }
    }

    private fun showHideFragmentCenter(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.notifyCenterShowing.value = false
        } else {
            viewModel.notifyCenterShowing.value?.let {
                viewModel.notifyCenterShowing.value = !it
            }
        }
    }

    private fun showHideCalendarView(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.calendarShowing.value = false
        } else {
            viewModel.calendarShowing.value?.let {
                viewModel.calendarShowing.value = !it
            }
        }
    }

    private fun showHideFastControlView(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.fastControlShowing.value = false
        } else {
            viewModel.fastControlShowing.value?.let {
                viewModel.fastControlShowing.value = !it
            }
            requestPermission(complete = { allow ->
                if (allow) {
                }
            }, Manifest.permission.READ_PHONE_STATE)
        }
    }

    private fun showHideWidgetFragment(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.widgetShowing.value = false
        } else {
            viewModel.widgetShowing.value?.let {
                viewModel.widgetShowing.value = !it
            }
        }
    }

    private fun showHideTaskbarAppsFragment(hardHide: Boolean = false) {
        if (hardHide) {
            viewModel.appsMoreTaskbarShowing.value = false
        } else {
            viewModel.appsMoreTaskbarShowing.value?.let {
                viewModel.appsMoreTaskbarShowing.value = !it
            }
        }
    }

    private inline fun <reified T : Fragment> fragmentIsAdded(): Boolean {
        supportFragmentManager.fragments.forEach {
            if (it is T) {
                return true
            }
        }

        return false
    }

    fun changeBackground() {
        requestPermission({
            if (it) {
                try {
                    val wallpaper = WallpaperManager.getInstance(this).drawable
                    binding.ivBackground.setImageDrawable(wallpaper)
                } catch (e: SecurityException) {
                    binding.ivBackground.setImageResource(R.drawable.background_normal)
                }
            } else {
                binding.ivBackground.setImageResource(R.drawable.background_normal)
            }
            showChooseDefaultLauncher()
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun showChooseDefaultLauncher() {
        if (PreferencesUtils.getBoolean(PresKey.FIRST_INSTALL, true)) {
            try {
                val selector = Intent(Intent.ACTION_MAIN)
                selector.addCategory(Intent.CATEGORY_HOME)
                selector.component =
                    ComponentName("android", "com.android.internal.app.ResolverActivity")
                startActivity(selector)
                PreferencesUtils.putBoolean(PresKey.FIRST_INSTALL, false)
            } catch (e: ActivityNotFoundException) {}
        }
    }

    override fun viewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    @SuppressLint("NewApi")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setAppActivityFullScreenOver(this)
            if (!isAutoClicking) {
                showHideFragmentCenter(true)
            }
            showHideWidgetFragment(true)
        }
    }

    private fun isNotificationServiceRunning(context: Context): Boolean {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val enabledNotificationListeners: String =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName: String = context.packageName
            return enabledNotificationListeners.contains(
                packageName
            )
        } catch (e: Exception) {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BackgroundFilterUtil.CODE_SELECT_PICKTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = data?.data
                    try {
                        // OI FILE Manager
                        val fileManagerString = selectedImageUri?.path
                        // MEDIA GALLERY
                        val selectedImagePath = PathUtils.getPath(this, selectedImageUri)
                        when {
                            selectedImagePath != null -> {
                                filePath = selectedImagePath
                            }
                            fileManagerString != null -> {
                                filePath = fileManagerString
                            }
                            else -> {
                                Toast.makeText(
                                    this, "Don't find image",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        }
                        //
                        if (filePath != null) {
                            val yourSelectedImage = BitmapFactory.decodeFile(filePath)
                            WallpaperManager.getInstance(this).setBitmap(yourSelectedImage)
                            sendBroadcast(Intent(ACTION_CHANGE_BACKGROUND))
                        }
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    private fun trackEvent(action: String) {
        FirebaseAnalTool.getInstance(this).trackEvent(TAG, action)
        FlurryAnalytics.logEvent(TAG, action)
    }

    private fun startNotifyPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        val bundle = Bundle()
        val str = packageName + "/" + NotificationListener::class.java.getName()
        bundle.putString(":settings:fragment_args_key", str)
        intent.putExtra(":settings:fragment_args_key", str)
        intent.putExtra(":settings:show_fragment_args", bundle)
        try {
            startActivity(intent)
            Toast.makeText(
                applicationContext,
                resources.getString(R.string.find_app_here, resources.getString(R.string.app_name)),
                Toast.LENGTH_SHORT
            ).show()
        } catch (unused: java.lang.Exception) {
        }
    }

    private fun createDesktopMenuPopup(): DesktopMenuPopup {
        return DesktopMenuPopup(this@MainActivity) { popup, view, id ->
            when (id) {
                R.id.item_sort_by -> {
                    val sortMenu = SortMenuPopup(this@MainActivity) {
                        when (it) {
                            R.id.item_sort_by_name -> {
                                viewModel.updateSortDesktop(DesktopLauncherSortState.NAME)
                            }

                            R.id.item_sort_by_type -> {
                                viewModel.updateSortDesktop(DesktopLauncherSortState.TYPE)
                            }

                            R.id.item_sort_by_date -> {
                                viewModel.updateSortDesktop(DesktopLauncherSortState.DATE)
                            }
                        }
                        popup.dismiss()
                    }
                    sortMenu.show(view, view.width - 10)
                }

                R.id.item_view -> {
                    viewModel.settingShowing.postValue(true)
                }

                R.id.item_new_folder -> {
                    showInputFolderName {
                        fileManagerViewModel.createFolder(
                            this,
                            it,
                            Config.FileManager.desktopPath(this@MainActivity)
                        )
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        showHideFragmentCenter(true)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastBackGround)
        FTPServerService.isRunning().apply {
            if (this) {
                stopService(Intent(this@MainActivity, FTPServerService::class.java))
            }
        }
        binding.fastControlView.unregister()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onRestart() {
        super.onRestart()
        isNotifyPermission = false
    }
}