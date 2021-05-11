package com.vishalgaur.shoppingapp.data.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.vishalgaur.shoppingapp.data.UserData

class ObjectListTypeConvertor {
	private val gson: Gson = Gson()

	@TypeConverter
	fun stringToSomeObjectList(data: String?): List<UserData.Address> {
		if (data == null) {
			return emptyList()
		}
		val listType = object : TypeToken<List<UserData.Address>>() {}.type

		return gson.fromJson(data, listType)
	}

	@TypeConverter
	fun someObjectListToString(addressList: List<UserData.Address>): String {
		return gson.toJson(addressList)
	}
}