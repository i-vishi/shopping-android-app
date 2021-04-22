package com.vishalgaur.shoppingapp.database

import androidx.room.TypeConverter

class ListTypeConverter {
    @TypeConverter
    fun fromStringToStringList(value: String): List<String> {
        return value.split(",").map { it }
    }

    @TypeConverter
    fun fromStringListToString(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun fromStringToIntegerList(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromIntegerListToString(value: List<Int>): String {
        return value.joinToString(separator = ",")
    }
}