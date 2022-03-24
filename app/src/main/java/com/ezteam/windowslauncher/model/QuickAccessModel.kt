package com.ezteam.windowslauncher.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "quickaccess")
data class QuickAccessModel(
    @PrimaryKey
    @ColumnInfo(name = "path")
    var path: String
)