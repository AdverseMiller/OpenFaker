package com.android1500.androidfaker.data.app.datasource.disk.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "logs", indices = [Index(value = ["hookName", "packageName", "timestamp"], unique = true)])
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val hookName: String,
    val packageName: String,
    val originalValue: String,
    val spoofedValue: String,
) : Parcelable
