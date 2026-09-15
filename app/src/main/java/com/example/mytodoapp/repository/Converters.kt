package com.example.mytodoapp.repository

import androidx.room.TypeConverter
import com.example.mytodoapp.model.RecurrenceType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType?): String {
        return value?.name ?: RecurrenceType.NONE.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String?): RecurrenceType {
        if (value.isNullOrBlank()) return RecurrenceType.NONE
        return try {
            RecurrenceType.valueOf(value)
        } catch (_: Exception) {
            RecurrenceType.NONE
        }
    }
}

