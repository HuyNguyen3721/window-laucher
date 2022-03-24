package com.ezteam.windowslauncher.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.viewmodel.BaseViewModel
import com.ezteam.windowslauncher.R
import com.ezteam.windowslauncher.database.repository.LauncherRepository
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.SpecialPackage
import com.ezteam.windowslauncher.utils.Config
import com.ezteam.windowslauncher.utils.PresKey
import com.ezteam.windowslauncher.utils.launcher.DesktopLauncherSortState
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils
import com.ezteam.windowslauncher.utils.launcher.LauncherLockState
import com.ezteam.windowslauncher.utils.launcher.LauncherType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File

class MainViewModel(
    application: Application,
    private var repository: LauncherRepository
) : BaseViewModel(application) {
    val windowsFragmentState: MutableLiveData<AppWindowsState> =
        MutableLiveData(AppWindowsState.PINED)
    val windowsHeight: MutableLiveData<Int> = MutableLiveData(0)
    val windowsShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val notifyCenterShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val calendarShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val fastControlShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val widgetShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchTextLiveData: MutableLiveData<String> = MutableLiveData("")
    val orientationLiveData: MutableLiveData<Int> = MutableLiveData(1)
    val appsMoreTaskbarShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val appsRunning: MutableLiveData<MutableList<LauncherModel>> = MutableLiveData(mutableListOf())
    val windowManagerShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val settingShowing: MutableLiveData<Boolean> = MutableLiveData(false)
    val resizeGridLauncher: MutableLiveData<Boolean> = MutableLiveData(false)

    private val desktopLauncherSortState: MutableLiveData<DesktopLauncherSortState> =
        MutableLiveData(DesktopLauncherSortState.DATE)

    @SuppressLint("QueryPermissionsNeeded")
    fun loadAppData() {
        val sortStateValue =
            PreferencesUtils.getInteger(PresKey.SORT_STATE, DesktopLauncherSortState.DATE.value)
        desktopLauncherSortState.value = DesktopLauncherSortState.getSortState(sortStateValue)
        if (PreferencesUtils.getBoolean(PresKey.FIRST_POSITION, true)) {
            updateFirstPosition()
            PreferencesUtils.putBoolean(PresKey.FIRST_POSITION, false)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pkgAppsList: List<ResolveInfo> =
                getApplication<Application>().packageManager.queryIntentActivities(mainIntent, 0)

            var launchers: MutableList<LauncherModel> = mutableListOf()

            // Add desktop app
            launchers.addAll(desktopDefaultApps(getApplication()))

            // Add folder
            launchers.addAll(folderDesktop())

            val dialerApps = LauncherAppUtils.getPackagesOfDialerApps(getApplication())
            val cameraApps = LauncherAppUtils.getPackagesOfCameraApps(getApplication())
            val messageApps = LauncherAppUtils.getPackagesOfMessageApps(getApplication())
            // Add system app
            pkgAppsList.forEach {
                val launcher = LauncherModel(it, getApplication())
                if (mutableListOf(
                        "com.android.settings",
                        "com.android.vending"
                    ).contains(launcher.packageName)
                    || dialerApps.contains(launcher.packageName)
                    || cameraApps.contains(launcher.packageName)
                    || messageApps.contains(launcher.packageName)
                ) {
                    launcher.pinDesktop = true
                }

                launchers.add(launcher)
            }

            launchers = launchers.map {
                return@map repository.migrateLauncher(it)
            } as MutableList<LauncherModel>

            // First pin
            firstPinApps(launchers)

            repository.deleteAll()
            repository.insert(launchers)
        }
    }

    private fun firstPinApps(launchers: MutableList<LauncherModel>) {
        val isFirstPin = PreferencesUtils.getBoolean(PresKey.FIRST_PIN_APPS, true)
        if (launchers.size > 8 && isFirstPin) {
            LauncherAppUtils.rePrioritize(launchers, getApplication())
            launchers.sortedByDescending {
                it.prioritize
            }.subList(0, 8).forEach {
                it.pinHome = true
            }
            PreferencesUtils.putBoolean(PresKey.FIRST_PIN_APPS, false)
        }
    }

    fun getLauncherApps(): LiveData<List<LauncherModel>> {
        return repository.getAllApps()
    }

    fun getLauncherAppsPined(): LiveData<List<LauncherModel>> {
        return repository.getPinedApps()
    }

    fun getLauncherAppsRecent(): LiveData<List<LauncherModel>> {
        return repository.getSuggestApps(getApplication())
    }

    fun getAppsByFilter(): LiveData<List<LauncherModel>> {
        return Transformations.switchMap(searchTextLiveData) {
            return@switchMap repository.getAppsByFilter(it)
        }
    }

    fun getTaskbarItems(): LiveData<List<LauncherModel>> {
        return repository.getTaskbarApps()
    }

    fun getDesktopApps(context: Context): LiveData<List<LauncherModel>> {
        return Transformations.switchMap(desktopLauncherSortState) {
            if (PreferencesUtils.getInteger(PresKey.SORT_STATE) != DesktopLauncherSortState.POSITION.value) {
                updateFirstPosition()
            }
            return@switchMap Transformations.switchMap(
                repository.getDesktopApps(
                    PreferencesUtils.getInteger(
                        PresKey.SORT_STATE
                    )
                )
            ) {
                val orientation = context.resources.configuration.orientation
                val result: MutableLiveData<List<LauncherModel>> = MutableLiveData()
                result.postValue(getDesktopApps(context, it))
                return@switchMap result
            }
        }
    }

    fun renameApp(launcher: LauncherModel, newName: String) {
        if (newName.isNotEmpty()) {
            launcher.appName = newName
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launcher)
        }
    }

    fun getFrequentApps(): LiveData<List<LauncherModel>> {
        return Transformations.switchMap(orientationLiveData) { orientation ->
            return@switchMap Transformations.switchMap(repository.getFrequentApps()) {
                val frequentApps: MutableLiveData<List<LauncherModel>> = MutableLiveData()
                when (orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        frequentApps.value = if (it.size > 6) it.subList(0, 6) else it
                    }

                    Configuration.ORIENTATION_LANDSCAPE -> {
                        frequentApps.value = if (it.size > 8) it.subList(0, 8) else it
                    }
                }
                return@switchMap frequentApps
            }
        }
    }

    fun updateSortDesktop(sortState: DesktopLauncherSortState) {
        val sortStateValue =
            PreferencesUtils.getInteger(PresKey.SORT_STATE, DesktopLauncherSortState.DATE.value)
        var currentState = DesktopLauncherSortState.getSortState(sortStateValue)
        when (sortState) {
            DesktopLauncherSortState.NAME -> {
                when (currentState) {
                    DesktopLauncherSortState.NAME -> {
                        currentState = DesktopLauncherSortState.NAME_DESC
                    }
                    DesktopLauncherSortState.NAME_DESC -> {
                        currentState = DesktopLauncherSortState.NAME
                    }
                    else -> {
                        currentState = sortState
                    }
                }
            }

            DesktopLauncherSortState.TYPE -> {
                when (currentState) {
                    DesktopLauncherSortState.TYPE -> {
                        currentState = DesktopLauncherSortState.TYPE_DESC
                    }
                    DesktopLauncherSortState.TYPE_DESC -> {
                        currentState = DesktopLauncherSortState.TYPE
                    }
                    else -> {
                        currentState = sortState
                    }
                }
            }

            DesktopLauncherSortState.DATE -> {
                when (currentState) {
                    DesktopLauncherSortState.DATE -> {
                        currentState = DesktopLauncherSortState.DATE_DESC
                    }
                    DesktopLauncherSortState.DATE_DESC -> {
                        currentState = DesktopLauncherSortState.DATE
                    }
                    else -> {
                        currentState = sortState
                    }
                }
            }
        }
        PreferencesUtils.putInteger(PresKey.SORT_STATE, currentState.value)
        desktopLauncherSortState.value = currentState
    }

    private fun folderDesktop(): List<LauncherModel> {
        val launchers: MutableList<LauncherModel> = mutableListOf()
        val desktopFile = File(Config.FileManager.desktopPath(getApplication()))
        desktopFile.listFiles()?.let {
            it.filter {
                it.isDirectory
            }.forEach {
                val launcher = LauncherModel(
                    packageName = it.path,
                    logoResId = R.drawable.ic_folder_default,
                    appName = FilenameUtils.getName(it.path),
                    prioritize = 4,
                    launcherType = LauncherType.APP_FOLDER.value
                )
                launchers.add(launcher)
            }
        }
        return launchers
    }

    fun createShortCut(launcher: LauncherModel, context: Context) {
        if (!repository.launcherPinedDesktop(launcher)) {
            launcher.pinDesktop = true
            launcher.timeCreated = System.currentTimeMillis()
            insertToDesktop(launcher, context)
        }
    }

    fun pinAndUnpinTaskbar(launcher: LauncherModel, success: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            launcher.pinTaskbar = !launcher.pinTaskbar
            launcher.timePinTaskbar = System.currentTimeMillis()
            repository.insert(launcher)
            success?.invoke(launcher.pinTaskbar)
        }
    }

    fun removeShortCut(launcher: LauncherModel) {
        if (repository.launcherPinedDesktop(launcher)) {
            launcher.pinDesktop = false
            viewModelScope.launch(Dispatchers.IO) {
                repository.insert(launcher)
            }
        }
    }

    fun removeApp(launcher: LauncherModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(launcher)
        }
    }

    fun pinAndUnpinApp(
        launcher: LauncherModel,
        success: ((Boolean) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                launcher.pinHome -> {
                    launcher.pinHome = false
                    repository.insert(launcher)
                    success?.invoke(false)
                }
                repository.countPinedApps() < 8 -> {
                    launcher.pinHome = true
                    repository.insert(launcher)
                    success?.invoke(true)
                }
                else -> {
                    failure?.invoke()
                }
            }
        }
    }

    fun updateRecentApp(launcher: LauncherModel) {
        launcher.timeRecent = System.currentTimeMillis()
        launcher.recentCount += 1
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launcher)
        }

        val apps = appsRunning.value
        apps?.add(launcher)
        appsRunning.postValue(apps)
    }

    fun lockAndUnlockApp(
        launcher: LauncherModel,
        password: String,
        complete: (LauncherLockState) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (launcher.password.isEmpty()) {
                launcher.password = password
                repository.insert(launcher)
                complete(LauncherLockState.LOCK)
            } else if (checkPassword(launcher, password)) {
                launcher.password = ""
                repository.insert(launcher)
                complete(LauncherLockState.UNLOCK)
            } else {
                complete(LauncherLockState.WRONG)
            }
        }

    }

    private fun checkPassword(launcher: LauncherModel, password: String): Boolean {
        return launcher.password == password
    }

    private fun desktopDefaultApps(context: Context): List<LauncherModel> {
        return mutableListOf(
            LauncherModel(
                packageName = SpecialPackage.USER_PACKAGE.packageId,
                logoResId = R.drawable.ic_desktop_user,
                appName = context.resources.getString(R.string.user),
                prioritize = 5,
                launcherType = LauncherType.APP_DESKTOP.value,
                position = 1
            ),
            LauncherModel(
                packageName = SpecialPackage.THIS_PC_PACKAGE.packageId,
                logoResId = R.drawable.ic_this_pc,
                appName = context.resources.getString(R.string.this_pc),
                prioritize = 5,
                launcherType = LauncherType.APP_DESKTOP.value,
                position = 2
            ),
            LauncherModel(
                packageName = SpecialPackage.THEME_PACKAGE.packageId,
                logoResId = R.drawable.ic_theme,
                appName = context.resources.getString(R.string.theme),
                prioritize = 5,
                launcherType = LauncherType.APP_DESKTOP.value,
                position = 3
            )
        )
    }

    private fun updateFirstPosition() {
        val list = repository.getDesktopAppsList(PreferencesUtils.getInteger(PresKey.SORT_STATE))
        for (index in list.indices) {
            list[index].position = index
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launchers = list.toMutableList())
        }
    }

    private fun getDesktopApps(
        context: Context,
        apps: List<LauncherModel>
    ): MutableList<LauncherModel> {
        val desktopApps = mutableListOf<LauncherModel>()

        for (index in 0 until LauncherAppUtils.getSizeGrid(context).getSize()) {
            apps.find {
                it.position == index
            }?.let {
                desktopApps.add(it)
            } ?: kotlin.run {
                desktopApps.add(
                    LauncherModel(
                        position = index,
                        launcherType = LauncherType.APP_IDLE.value
                    )
                )
            }
        }

        return desktopApps
    }

    private fun insertToDesktop(launcher: LauncherModel, context: Context) {
        var position = -1
        val apps = repository.getDesktopAppsList(PreferencesUtils.getInteger(PresKey.SORT_STATE))
        for (index in 0 until LauncherAppUtils.getSizeGrid(context).getSize()) {
            apps.find {
                it.position == index
            }?.let {

            } ?: kotlin.run {
                position = index
            }
            if (position != -1) {
                break
            }
        }
        launcher.position = position
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launcher)
        }
    }

    fun updateDesktopLauncher(launchers: List<LauncherModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(launchers.toMutableList())
        }
    }
}