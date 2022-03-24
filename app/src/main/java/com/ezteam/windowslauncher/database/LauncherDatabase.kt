package com.ezteam.windowslauncher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ezteam.windowslauncher.model.LauncherModel
import com.ezteam.windowslauncher.model.QuickAccessModel

@Database(
    entities = [LauncherModel::class, QuickAccessModel::class],
    version = 3,
    exportSchema = true,
    autoMigrations = []
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherModelDAO

    companion object {
        private const val DB_NAME = "app_launcher"

        fun getInstance(context: Context): LauncherDatabase {
            return Room
                .databaseBuilder(context, LauncherDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}