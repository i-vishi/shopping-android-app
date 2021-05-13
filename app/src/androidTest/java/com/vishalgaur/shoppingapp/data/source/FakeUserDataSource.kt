package com.vishalgaur.shoppingapp.data.source

import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData

class FakeUserDataSource(private var uData: UserData?) : UserDataSource {

	private var emailMobileData = EmailMobileData()

	override suspend fun addUser(userData: UserData) {
		uData = userData
	}

	override suspend fun getUserById(userId: String): Result<UserData?> {
		uData?.let {
			if (it.userId == userId) {
				return Success(it)
			}
		}
		return Error(Exception("User Not Found"))
	}

	override suspend fun getEmailsAndMobiles(): EmailMobileData {
		return emailMobileData
	}

	override suspend fun getUserByMobileAndPassword(
		mobile: String,
		password: String
	): MutableList<UserData> {
		val res = mutableListOf<UserData>()
		uData?.let {
			if (it.mobile == mobile && it.password == password) {
				res.add(it)
			}
		}
		return res
	}

	override suspend fun clearUser() {
		uData = null
	}

	override suspend fun getUserByMobile(phoneNumber: String): UserData? {
		return super.getUserByMobile(phoneNumber)
	}

	override fun updateEmailsAndMobiles(email: String, mobile: String) {
		emailMobileData.emails.add(email)
		emailMobileData.mobiles.add(mobile)
	}

	override suspend fun likeProduct(productId: String, userId: String) {
		uData?.let {
			if (it.userId == userId) {
				val likes = it.likes.toMutableList()
				likes.add(productId)
				it.likes = likes
			}
		}
	}

	override suspend fun dislikeProduct(productId: String, userId: String) {
		uData?.let {
			if (it.userId == userId) {
				val likes = it.likes.toMutableList()
				likes.remove(productId)
				it.likes = likes
			}
		}
	}

	override suspend fun insertAddress(newAddress: UserData.Address, userId: String) {
		uData?.let {
			if (it.userId == userId) {
				val addresses = it.addresses.toMutableList()
				addresses.add(newAddress)
				it.addresses = addresses
			}
		}
	}

	override suspend fun updateAddress(newAddress: UserData.Address, userId: String) {
		uData?.let { data ->
			if (data.userId == userId) {
				val addresses = data.addresses.toMutableList()
				val pos = data.addresses.indexOfFirst { it.addressId == newAddress.addressId }
				if (pos >= 0) {
					addresses[pos] = newAddress
				}
				data.addresses = addresses
			}
		}
	}

	override suspend fun deleteAddress(addressId: String, userId: String) {
		uData?.let { data ->
			if (data.userId == userId) {
				val addresses = data.addresses.toMutableList()
				val pos = data.addresses.indexOfFirst { it.addressId == addressId }
				if (pos >= 0) {
					addresses.removeAt(pos)
				}
				data.addresses = addresses
			}
		}
	}

	override suspend fun insertCartItem(newItem: UserData.CartItem, userId: String) {
		uData?.let {
			if (it.userId == userId) {
				val cart = it.cart.toMutableList()
				cart.add(newItem)
				it.cart = cart
			}
		}
	}

	override suspend fun updateCartItem(item: UserData.CartItem, userId: String) {
		uData?.let { data ->
			if (data.userId == userId) {
				val cart = data.cart.toMutableList()
				val pos = data.cart.indexOfFirst { it.itemId == item.itemId }
				if (pos >= 0) {
					cart[pos] = item
				}
				data.cart = cart
			}
		}
	}

	override suspend fun deleteCartItem(itemId: String, userId: String) {
		uData?.let { data ->
			if (data.userId == userId) {
				val cart = data.cart.toMutableList()
				val pos = data.cart.indexOfFirst { it.itemId == itemId }
				if (pos >= 0) {
					cart.removeAt(pos)
				}
				data.cart = cart
			}
		}
	}

	override suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?> {
		uData?.let {
			if (it.userId == userId) {
				return Success(it.addresses)
			}
		}
		return Error(Exception("User Not Found"))
	}

	override suspend fun getLikesByUserId(userId: String): Result<List<String>?> {
		uData?.let {
			if (it.userId == userId) {
				return Success(it.likes)
			}
		}
		return Error(Exception("User Not Found"))
	}
}