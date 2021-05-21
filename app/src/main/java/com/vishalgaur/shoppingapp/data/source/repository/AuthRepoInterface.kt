package com.vishalgaur.shoppingapp.data.source.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors

interface AuthRepoInterface {
	suspend fun refreshData()
	suspend fun signUp(userData: UserData)
	fun login(userData: UserData, rememberMe: Boolean)
	suspend fun checkEmailAndMobile(email: String, mobile: String, context: Context): SignUpErrors?
	suspend fun checkLogin(mobile: String, password: String): UserData?
	suspend fun signOut()
	suspend fun hardRefreshUserData()
	suspend fun insertProductToLikes(productId: String, userId: String): Result<Boolean>
	suspend fun removeProductFromLikes(productId: String, userId: String): Result<Boolean>
	suspend fun insertAddress(newAddress: UserData.Address, userId: String): Result<Boolean>
	suspend fun updateAddress(newAddress: UserData.Address, userId: String): Result<Boolean>
	suspend fun deleteAddressById(addressId: String, userId: String): Result<Boolean>
	suspend fun insertCartItemByUserId(cartItem: UserData.CartItem, userId: String): Result<Boolean>
	suspend fun updateCartItemByUserId(cartItem: UserData.CartItem, userId: String): Result<Boolean>
	suspend fun deleteCartItemByUserId(itemId: String, userId: String): Result<Boolean>
	suspend fun placeOrder(newOrder: UserData.OrderItem, userId: String): Result<Boolean>
	suspend fun getOrdersByUserId(userId: String): Result<List<UserData.OrderItem>?>
	suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?>
	suspend fun getLikesByUserId(userId: String): Result<List<String>?>
	suspend fun getUserData(userId: String): Result<UserData?>
	fun getFirebaseAuth(): FirebaseAuth
	fun signInWithPhoneAuthCredential(
		credential: PhoneAuthCredential,
		isUserLoggedIn: MutableLiveData<Boolean>,
		context: Context
	)

	fun isRememberMeOn(): Boolean
}
