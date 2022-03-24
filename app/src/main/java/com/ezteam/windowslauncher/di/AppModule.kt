package com.ezteam.windowslauncher.di

import com.ezteam.windowslauncher.database.LauncherDatabase
import com.ezteam.windowslauncher.database.repository.LauncherRepository
import com.ezteam.windowslauncher.database.repository.LauncherRepositoryImpl
import com.ezteam.windowslauncher.viewmodel.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { LauncherDatabase.getInstance(androidApplication()) }
    single { LauncherRepositoryImpl(get()) as LauncherRepository }
    single { MainViewModel(androidApplication(), get()) }
    single { FileManagerViewModel(androidApplication(), get()) }
    single { NewspaperViewModel(androidApplication()) }
    single { WeatherInfoViewModel(androidApplication()) }
    single { ControlViewModel(androidApplication()) }
}