package com.ezteam.windowslauncher.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.QuickAccessModel

interface LauncherRepository {
    suspend fun insert(launcherModel: LauncherModel)

    suspend fun insert(launchers: MutableList<LauncherModel>)

    suspend fun delete(launcherModel: LauncherModel)

    suspend fun deleteAll()

    fun getAllApps(): LiveData<List<LauncherModel>>

    fun getSuggestApps(context: Context): LiveData<List<LauncherModel>>

    fun getDesktopApps(sortState: Int): LiveData<List<LauncherModel>>

    fun getDesktopAppsList(sortState: Int): List<LauncherModel>

    fun getTaskbarApps(): LiveData<List<LauncherModel>>

    fun getPinedApps(): LiveData<List<LauncherModel>>

    fun getAppsByFilter(searchText: String): LiveData<List<LauncherModel>>

    fun getFrequentApps(): LiveData<List<LauncherModel>>

    fun launcherPinedDesktop(launcherModel: LauncherModel): Boolean

    fun launcherByPackage(packageName: String): LauncherModel?

    fun migrateLauncher(launcherModel: LauncherModel): LauncherModel

    fun getQuickAccess(): LiveData<List<QuickAccessModel>>

    fun checkQuickAccessExist(url: String): QuickAccessModel?

    suspend fun insertQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>)

    suspend fun deleteQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>)

    fun countPinedApps(): Int
}