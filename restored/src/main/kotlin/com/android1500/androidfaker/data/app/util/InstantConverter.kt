package com.android1500.androidfaker.data.app.util

import androidx.room.TypeConverter
import kotlin.time.Instant

class InstantConverter {
    @TypeConverter fun toEpochMilliseconds(value: Instant?): Long? = value?.toEpochMilliseconds()
    @TypeConverter fun fromEpochMilliseconds(value: Long?): Instant? = value?.let(Instant::fromEpochMilliseconds)
}
