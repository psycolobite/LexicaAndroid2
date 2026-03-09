package com.example.lexicaandroid2.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }
}

