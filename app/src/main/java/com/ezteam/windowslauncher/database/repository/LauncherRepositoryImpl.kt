package com.ezteam.windowslauncher.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ezteam.windowslauncher.database.LauncherDatabase
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.QuickAccessModel
import com.ezteam.windowslauncher.utils.launcher.LauncherAppUtils

class LauncherRepositoryImpl(
    private val launcherDatabase: LauncherDatabase
) : LauncherRepository {
    override suspend fun insert(launcherModel: LauncherModel) {
        launcherDatabase.launcherDao().insert(launcherModel)
    }

    override suspend fun insert(launchers: MutableList<LauncherModel>) {
        launcherDatabase.launcherDao().insert(launchers)
    }

    override suspend fun delete(launcherModel: LauncherModel) {
        launcherDatabase.launcherDao().delete(launcherModel)
    }

    override suspend fun deleteAll() {
        launcherDatabase.launcherDao().deleteAll()
    }

    override fun getAllApps(): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getAllApps()
    }

    override fun getSuggestApps(context: Context): LiveData<List<LauncherModel>> {
        return Transformations.switchMap(launcherDatabase.launcherDao().getAllApps()) {
            val result: MutableLiveData<List<LauncherModel>> = MutableLiveData(mutableListOf())
            var allApps: List<LauncherModel> = it
            LauncherAppUtils.rePrioritize(allApps, context)
            allApps = allApps.sortedByDescending {
                it.timeRecent
            }
            result.postValue(if (allApps.size > 4) allApps.subList(0, 4) else allApps)
            return@switchMap result
        }
    }

    override fun getDesktopApps(sortState: Int): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getDesktopApps(sortState)
    }

    override fun getDesktopAppsList(sortState: Int): List<LauncherModel> {
        return launcherDatabase.launcherDao().getDesktopAppsList(sortState)
    }

    override fun getTaskbarApps(): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getTaskbarApps()
    }

    override fun getPinedApps(): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getPinedApps()
    }

    override fun getAppsByFilter(searchText: String): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getAppsByFilter("%" + searchText.trim() + "%")
    }

    override fun getFrequentApps(): LiveData<List<LauncherModel>> {
        return launcherDatabase.launcherDao().getFrequentApps()
    }

    override fun launcherPinedDesktop(launcherModel: LauncherModel): Boolean {
        return launcherDatabase.launcherDao().launcherPinedDesktop(launcherModel.packageName)
    }

    override fun launcherByPackage(packageName: String): LauncherModel? {
        return launcherDatabase.launcherDao().launcherByPackage(packageName)
    }

    override fun migrateLauncher(launcherModel: LauncherModel): LauncherModel {
        val launcherFromDb =
            launcherDatabase.launcherDao().launcherByPackage(launcherModel.packageName)
        return launcherFromDb?.let {
            it.logoResId = launcherModel.logoResId
            it
        } ?: launcherModel
    }

    override fun getQuickAccess(): LiveData<List<QuickAccessModel>> {
        return launcherDatabase.launcherDao().getQuickAccess()
    }

    override suspend fun insertQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>) {
        launcherDatabase.launcherDao().insertQuickAccess(lstQuickAccess)
    }

    override suspend fun deleteQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>) {
        launcherDatabase.launcherDao().deleteQuickAccess(lstQuickAccess)
    }

    override fun checkQuickAccessExist(url: String): QuickAccessModel? {
        return launcherDatabase.launcherDao().checkQuickAccessExist(url)
    }

    override fun countPinedApps(): Int {
        return launcherDatabase.launcherDao().getPinedAppsSize()
    }
}