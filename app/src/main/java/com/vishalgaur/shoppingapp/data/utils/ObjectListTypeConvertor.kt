package com.vishalgaur.shoppingapp.data.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.vishalgaur.shoppingapp.data.UserData

class ObjectListTypeConvertor {
	@TypeConverter
	fun stringToSomeObjectList(data: String?): List<UserData.Address> {
		if (data == null) {
			return emptyList()
		}
		val listType = object : TypeToken<List<UserData.Address>>() {}.type
		val gson = Gson()
		return gson.fromJson(data, listType)
	}

	@TypeConverter
	fun someObjectListToString(addressList: List<UserData.Address>): String {
		if (addressList.isEmpty()) {
			return ""
		}
		val gson = Gson()
		val listType = object : TypeToken<List<UserData.Address>>() {}.type
		return gson.toJson(addressList, listType)
	}
}