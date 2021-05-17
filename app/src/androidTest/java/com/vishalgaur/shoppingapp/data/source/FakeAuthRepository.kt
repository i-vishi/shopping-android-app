package com.vishalgaur.shoppingapp.data.source

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import com.vishalgaur.shoppingapp.data.utils.UserType

class FakeAuthRepository(private val sessionManager: ShoppingAppSessionManager) :
	AuthRepoInterface {

	private var emailMobileData = EmailMobileData()
	private var uData: UserData? = null

	override suspend fun refreshData() {
		// no implementation
	}

	override suspend fun signUp(userData: UserData) {
		uData = userData
		sessionManager.createLoginSession(
			userData.userId,
			userData.name,
			userData.mobile,
			false,
			userData.userType == UserType.SELLER.name
		)
	}

	override fun login(userData: UserData, rememberMe: Boolean) {
		uData = userData
		sessionManager.createLoginSession(
			userData.userId,
			userData.name,
			userData.mobile,
			rememberMe,
			userData.userType == UserType.SELLER.name
		)
	}

	override suspend fun checkEmailAndMobile(
		email: String,
		mobile: String,
		context: Context
	): SignUpErrors {
		// no implementation
		return SignUpErrors.NONE
	}

	override suspend fun checkLogin(mobile: String, password: String): UserData? {
		uData?.let {
			if (it.mobile == mobile && it.password == password) {
				return it
			}
		}
		return null
	}

	override suspend fun signOut() {
		uData = null
		sessionManager.logoutFromSession()
	}

	override suspend fun hardRefreshUserData() {
		// no implementation
	}

	override suspend fun insertProductToLikes(productId: String, userId: String): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val likes = it.likes.toMutableList()
				likes.add(productId)
				it.likes = likes
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun removeProductFromLikes(
		productId: String,
		userId: String
	): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val likes = it.likes.toMutableList()
				likes.remove(productId)
				it.likes = likes
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun insertAddress(
		newAddress: UserData.Address,
		userId: String
	): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val addresses = it.addresses.toMutableList()
				addresses.add(newAddress)
				it.addresses = addresses
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun updateAddress(
		newAddress: UserData.Address,
		userId: String
	): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val addresses = it.addresses.toMutableList()
				addresses.add(newAddress)
				val pos =
					it.addresses.indexOfFirst { address -> address.addressId == newAddress.addressId }
				if (pos >= 0) {
					addresses[pos] = newAddress
				}
				it.addresses = addresses
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun deleteAddressById(addressId: String, userId: String): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val addresses = it.addresses.toMutableList()
				val pos = it.addresses.indexOfFirst { address -> address.addressId == addressId }
				if (pos >= 0) {
					addresses.removeAt(pos)
				}
				it.addresses = addresses
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun insertCartItemByUserId(
		cartItem: UserData.CartItem,
		userId: String
	): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val cart = it.cart.toMutableList()
				cart.add(cartItem)
				it.cart = cart
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun updateCartItemByUserId(
		cartItem: UserData.CartItem,
		userId: String
	): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val cart = it.cart.toMutableList()
				val pos = it.cart.indexOfFirst { item -> item.itemId == cartItem.itemId }
				if (pos >= 0) {
					cart[pos] = cartItem
				}
				it.cart = cart
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun deleteCartItemByUserId(itemId: String, userId: String): Result<Boolean> {
		uData?.let {
			if (it.userId == userId) {
				val cart = it.cart.toMutableList()
				val pos = it.cart.indexOfFirst { item -> item.itemId == itemId }
				if (pos >= 0) {
					cart.removeAt(pos)
				}
				it.cart = cart
				return Result.Success(true)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?> {
		uData?.let {
			if (it.userId == userId) {
				return Result.Success(it.addresses)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun getLikesByUserId(userId: String): Result<List<String>?> {
		uData?.let {
			if (it.userId == userId) {
				return Result.Success(it.likes)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override suspend fun getUserData(userId: String): Result<UserData?> {
		uData?.let {
			if (it.userId == userId) {
				return Result.Success(it)
			}
		}
		return Result.Error(Exception("User Not Found"))
	}

	override fun getFirebaseAuth(): FirebaseAuth {
		return Firebase.auth
	}

	override fun signInWithPhoneAuthCredential(
		credential: PhoneAuthCredential,
		isUserLoggedIn: MutableLiveData<Boolean>,
		context: Context
	) {
		// no implementation
	}

	override fun isRememberMeOn(): Boolean {
		// no implementation
		return true
	}
}