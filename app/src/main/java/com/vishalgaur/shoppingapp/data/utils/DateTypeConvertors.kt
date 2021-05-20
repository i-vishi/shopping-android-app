package com.vishalgaur.shoppingapp.data.utils

import androidx.room.TypeConverter
import java.util.*

class DateTypeConvertors {
	@TypeConverter
	fun toDate(dateLong: Long?): Date? {
		return dateLong?.let { Date(it) }
	}

	@TypeConverter
	fun fromDate(date: Date?): Long? {
		return date?.time
	}
}