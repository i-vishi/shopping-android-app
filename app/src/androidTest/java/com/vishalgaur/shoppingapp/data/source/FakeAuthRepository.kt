package com.vishalgaur.shoppingapp.data.source

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
//
//class FakeAuthRepository : AuthRepoInterface{
//	override suspend fun refreshData() {
//		// no implementation
//	}
//
//	override suspend fun signUp(userData: UserData) {
//
//	}
//
//	override fun login(userData: UserData, rememberMe: Boolean) {
//		TODO("to be implemented")
//	}
//
//	override suspend fun checkEmailAndMobile(
//		email: String,
//		mobile: String,
//		context: Context
//	): SignUpErrors? {
//		TODO("to be implemented")
//	}
//
//	override suspend fun checkLogin(mobile: String, password: String): UserData? {
//		TODO("to be implemented")
//	}
//
//	override suspend fun signOut() {
//		TODO("to be implemented")
//	}
//
//	override suspend fun hardRefreshUserData() {
//		// no implementation
//	}
//
//	override suspend fun insertProductToLikes(productId: String, userId: String): Result<Boolean> {
//		TODO("N implemented")
//	}
//
//	override suspend fun removeProductFromLikes(
//		productId: String,
//		userId: String
//	): Result<Boolean> {
//		TODO("Nt implemented")
//	}
//
//	override suspend fun insertAddress(
//		newAddress: UserData.Address,
//		userId: String
//	): Result<Boolean> {
//		TODO("Nimplemented")
//	}
//
//	override suspend fun updateAddress(
//		newAddress: UserData.Address,
//		userId: String
//	): Result<Boolean> {
//		TODO("N implemented")
//	}
//
//	override suspend fun deleteAddressById(addressId: String, userId: String): Result<Boolean> {
//		TODO("N implemented")
//	}
//
//	override suspend fun insertCartItemByUserId(
//		cartItem: UserData.CartItem,
//		userId: String
//	): Result<Boolean> {
//		TODO("N implemented")
//	}
//
//	override suspend fun updateCartItemByUserId(
//		cartItem: UserData.CartItem,
//		userId: String
//	): Result<Boolean> {
//		TODO(" implemented")
//	}
//
//	override suspend fun deleteCartItemByUserId(itemId: String, userId: String): Result<Boolean> {
//		TODO(" implemented")
//	}
//
//	override suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?> {
//		TODO("N implemented")
//	}
//
//	override suspend fun getLikesByUserId(userId: String): Result<List<String>?> {
//		TODO("Ntimplemented")
//	}
//
//	override suspend fun getUserData(userId: String): Result<UserData?> {
//		TODO("N implemented")
//	}
//
//	override fun getFirebaseAuth(): FirebaseAuth {
//		// no implementation
//	}
//
//	override fun signInWithPhoneAuthCredential(
//		credential: PhoneAuthCredential,
//		isUserLoggedIn: MutableLiveData<Boolean>,
//		context: Context
//	) {
//		// no implementation
//	}
//
//	override fun isRememberMeOn(): Boolean {
//		// no implementation
//		return true
//	}
//}