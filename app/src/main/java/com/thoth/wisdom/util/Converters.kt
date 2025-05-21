package com.thoth.wisdom.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thoth.wisdom.data.model.Source
import java.util.Date

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromSourceList(value: List<Source>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSourceList(value: String): List<Source> {
        val listType = object : TypeToken<List<Source>>() {}.type
        return gson.fromJson(value, listType)
    }
}
