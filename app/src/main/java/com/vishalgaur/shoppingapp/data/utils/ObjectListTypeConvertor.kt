package com.vishalgaur.shoppingapp.data.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.vishalgaur.shoppingapp.data.UserData

class ObjectListTypeConvertor {
	@TypeConverter
	fun stringToAddressObjectList(data: String?): List<UserData.Address> {
		if (data == null) {
			return emptyList()
		}
		val listType = object : TypeToken<List<UserData.Address>>() {}.type
		val gson = Gson()
		return gson.fromJson(data, listType)
	}

	@TypeConverter
	fun addressObjectListToString(addressList: List<UserData.Address>): String {
		if (addressList.isEmpty()) {
			return ""
		}
		val gson = Gson()
		val listType = object : TypeToken<List<UserData.Address>>() {}.type
		return gson.toJson(addressList, listType)
	}

	@TypeConverter
	fun stringToCartObjectList(data: String?): List<UserData.CartItem> {
		if (data == null) {
			return emptyList()
		}
		val listType = object : TypeToken<List<UserData.CartItem>>() {}.type
		val gson = Gson()
		return gson.fromJson(data, listType)
	}

	@TypeConverter
	fun cartObjectListToString(cartList: List<UserData.CartItem>): String {
		if (cartList.isEmpty()) {
			return ""
		}
		val gson = Gson()
		val listType = object : TypeToken<List<UserData.CartItem>>() {}.type
		return gson.toJson(cartList, listType)
	}
}