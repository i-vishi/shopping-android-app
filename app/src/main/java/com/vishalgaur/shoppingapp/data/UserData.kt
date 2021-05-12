package com.vishalgaur.shoppingapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vishalgaur.shoppingapp.data.utils.ObjectListTypeConvertor
import com.vishalgaur.shoppingapp.data.utils.UserType
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "users")
data class UserData(
	@PrimaryKey
	var userId: String = "",
	var name: String = "",
	var mobile: String = "",
	var email: String = "",
	var password: String = "",
	var likes: List<String> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var addresses: List<Address> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var cart: List<CartItem> = ArrayList(),
	var userType: String = UserType.CUSTOMER.name
) : Parcelable {
	fun toHashMap(): HashMap<String, Any> {
		return hashMapOf(
			"userId" to userId,
			"name" to name,
			"email" to email,
			"mobile" to mobile,
			"password" to password,
			"likes" to likes,
			"addresses" to addresses.map { it.toHashMap() },
			"userType" to userType
		)
	}


	@Parcelize
	data class Address(
		var addressId: String = "",
		var fName: String = "",
		var lName: String = "",
		var countryISOCode: String = "",
		var streetAddress: String = "",
		var streetAddress2: String = "",
		var city: String = "",
		var state: String = "",
		var zipCode: String = "",
		var phoneNumber: String = ""
	) : Parcelable {
		fun toHashMap(): HashMap<String, String> {
			return hashMapOf(
				"addressId" to addressId,
				"fName" to fName,
				"lName" to lName,
				"countryISOCode" to countryISOCode,
				"streetAddress" to streetAddress,
				"streetAddress2" to streetAddress2,
				"city" to city,
				"state" to state,
				"zipCode" to zipCode,
				"phoneNumber" to phoneNumber
			)
		}
	}

	@Parcelize
	data class CartItem(
		var itemId: String = "",
		var productId: String = "",
		var ownerId: String = "",
		var quantity: Int = 0,
		var color: String?,
		var size: Int?
	) : Parcelable {
		fun toHashMap(): HashMap<String, Any> {
			val hashMap = hashMapOf<String, Any>()
			hashMap["itemId"] = itemId
			hashMap["productId"] = productId
			hashMap["ownerId"] = ownerId
			hashMap["quantity"] = quantity
			if (color != null)
				hashMap["color"] = color!!
			if (size != null)
				hashMap["size"] = size!!
			return hashMap
		}
	}
}