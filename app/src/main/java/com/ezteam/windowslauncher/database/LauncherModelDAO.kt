package com.ezteam.windowslauncher.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.QuickAccessModel

@Dao
interface LauncherModelDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(launcherModel: LauncherModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(launchers: MutableList<LauncherModel>)

    @Delete
    fun delete(launcherModel: LauncherModel)

    @Query("DELETE FROM launcher")
    fun deleteAll()

    @Query("SELECT * FROM launcher WHERE launcherType == 0 ORDER BY appName")
    fun getAllApps(): LiveData<List<LauncherModel>>

    @Query("SELECT * FROM launcher WHERE launcherType == 0 ORDER BY appName")
    fun getNormalAllApps(): List<LauncherModel>

    @Query(
        """SELECT * FROM launcher WHERE launcherType == 1 OR launcherType == 2 OR pinDesktop == 1
            ORDER BY
            CASE WHEN :sortState = 1 THEN appName END ASC,
            CASE WHEN :sortState = 2 THEN appName END DESC,
            CASE WHEN :sortState = 3 THEN launcherType END ASC,
            CASE WHEN :sortState = 4 THEN launcherType END DESC,
            CASE WHEN :sortState = 5 THEN timeCreated END ASC,
            CASE WHEN :sortState = 6 THEN timeCreated END DESC,
            CASE WHEN :sortState = 7 THEN position END ASC
            """
    )
    fun getDesktopApps(sortState: Int): LiveData<List<LauncherModel>>

    @Query(
        """SELECT * FROM launcher WHERE launcherType == 1 OR launcherType == 2 OR pinDesktop == 1
            ORDER BY
            CASE WHEN :sortState = 1 THEN appName END ASC,
            CASE WHEN :sortState = 2 THEN appName END DESC,
            CASE WHEN :sortState = 3 THEN launcherType END ASC,
            CASE WHEN :sortState = 4 THEN launcherType END DESC,
            CASE WHEN :sortState = 5 THEN timeCreated END ASC,
            CASE WHEN :sortState = 6 THEN timeCreated END DESC,
            CASE WHEN :sortState = 7 THEN position END ASC
            """
    )
    fun getDesktopAppsList(sortState: Int): List<LauncherModel>

    @Query("SELECT * FROM launcher WHERE pinTaskbar == 1 ORDER BY timePinTaskbar")
    fun getTaskbarApps(): LiveData<List<LauncherModel>>

    @Query("SELECT * FROM launcher WHERE pinHome == 1 AND launcherType == 0 ORDER BY appName")
    fun getPinedApps(): LiveData<List<LauncherModel>>

    @Query("SELECT * FROM launcher WHERE appName LIKE :searchText ORDER BY appName")
    fun getAppsByFilter(searchText: String): LiveData<List<LauncherModel>>

    @Query("SELECT * FROM launcher WHERE launcherType == 0 ORDER BY recentCount DESC")
    fun getFrequentApps(): LiveData<List<LauncherModel>>

    @Query("SELECT EXISTS (SELECT * FROM launcher WHERE packageName =:packageName AND pinDesktop == 1)")
    fun launcherPinedDesktop(packageName: String): Boolean

    @Query("SELECT * FROM launcher WHERE packageName =:packageName")
    fun launcherByPackage(packageName: String): LauncherModel?

    @Query("SELECT * FROM quickaccess")
    fun getQuickAccess(): LiveData<List<QuickAccessModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>)

    @Delete
    fun deleteQuickAccess(lstQuickAccess: MutableList<QuickAccessModel>)

    @Query("SELECT * FROM quickaccess WHERE path =:url")
    fun checkQuickAccessExist(url: String): QuickAccessModel?

    @Query("SELECT COUNT(*) FROM launcher WHERE pinHome == 1 AND launcherType == 0")
    fun getPinedAppsSize(): Int
}