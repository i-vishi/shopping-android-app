package com.vishalgaur.shoppingapp.data.source.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserLocalDataSource
import com.vishalgaur.shoppingapp.data.source.remote.AuthRemoteDataSource
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import com.vishalgaur.shoppingapp.data.utils.UserType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class AuthRepository(
	private val userLocalDataSource: UserDataSource,
	private val authRemoteDataSource: UserDataSource,
	private val application: Application
) {

	private var firebaseAuth: FirebaseAuth = Firebase.auth
	private var sessionManager = ShoppingAppSessionManager(application.applicationContext)

	companion object {
		private const val TAG = "AuthRepository"

		@Volatile
		private var INSTANCE: AuthRepository? = null

		fun getRepository(app: Application): AuthRepository {
			return INSTANCE ?: synchronized(this) {
				val database = ShoppingAppDatabase.getInstance(app)
				AuthRepository(
					UserLocalDataSource(database.userDao()),
					AuthRemoteDataSource(),
					app
				).also {
					INSTANCE = it
				}
			}
		}
	}

	fun getFirebaseAuth() = firebaseAuth

	fun isRememberMeOn() = sessionManager.isRememberMeOn()

	suspend fun refreshData() {
		Log.d(TAG, "refreshing userdata")
		if (sessionManager.isLoggedIn()) {
			updateUserInLocalSource(sessionManager.getPhoneNumber())
		} else {
			sessionManager.logoutFromSession()
			deleteUserFromLocalSource()
		}
	}

	suspend fun signUp(userData: UserData) {
		val isSeller = userData.userType == UserType.SELLER.name
		sessionManager.createLoginSession(
			userData.userId,
			userData.name,
			userData.mobile,
			false,
			isSeller
		)
		Log.d(TAG, "on SignUp: Updating user in Local Source")
		userLocalDataSource.addUser(userData)
		Log.d(TAG, "on SignUp: Updating userdata on Remote Source")
		authRemoteDataSource.addUser(userData)
		authRemoteDataSource.updateEmailsAndMobiles(userData.email, userData.mobile)
	}

	fun login(userData: UserData, rememberMe: Boolean) {
		val isSeller = userData.userType == UserType.SELLER.name
		sessionManager.createLoginSession(
			userData.userId,
			userData.name,
			userData.mobile,
			rememberMe,
			isSeller
		)
	}

	suspend fun checkEmailAndMobile(email: String, mobile: String): SignUpErrors? {
		Log.d(TAG, "on SignUp: Checking email and mobile")
		var sErr: SignUpErrors? = null
		val queryResult = authRemoteDataSource.getEmailsAndMobiles()
		if (queryResult != null) {
			val mob = queryResult.mobiles.contains(mobile)
			val em = queryResult.emails.contains(email)
			if (!mob && !em) {
				sErr = SignUpErrors.NONE
			} else {
				sErr = SignUpErrors.SERR
				when {
					!mob && em -> makeErrToast("Email is already registered!")
					mob && !em -> makeErrToast("Mobile is already registered!")
					mob && em -> makeErrToast("Email and mobile is already registered!")
				}
			}
		}
		return sErr
	}

	suspend fun checkLogin(mobile: String, password: String): UserData? {
		Log.d(TAG, "on Login: checking mobile and password")
		val queryResult =
			authRemoteDataSource.getUserByMobileAndPassword(mobile, password)
		return if (queryResult.size > 0) {
			queryResult[0]
		} else {
			null
		}
	}

	fun signInWithPhoneAuthCredential(
		credential: PhoneAuthCredential,
		isUserLoggedIn: MutableLiveData<Boolean>
	) {
		firebaseAuth.signInWithCredential(credential)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Log.d(TAG, "signInWithCredential:success")
					val user = task.result?.user
					if (user != null) {
						isUserLoggedIn.postValue(true)
					}

				} else {
					Log.w(TAG, "signInWithCredential:failure", task.exception)
					if (task.exception is FirebaseAuthInvalidCredentialsException) {
						Log.d(TAG, "createUserWithMobile:failure", task.exception)
						isUserLoggedIn.postValue(false)
						makeErrToast("Wrong OTP!")
					}

				}
			}
	}

	suspend fun signOut() {
		sessionManager.logoutFromSession()
		firebaseAuth.signOut()
		userLocalDataSource.clearUser()
	}

	private fun makeErrToast(text: String) {
		Toast.makeText(application.applicationContext, text, Toast.LENGTH_LONG).show()
	}

	private suspend fun deleteUserFromLocalSource() {
		userLocalDataSource.clearUser()
	}

	private suspend fun updateUserInLocalSource(phoneNumber: String?) {
		coroutineScope {
			launch {
				if (phoneNumber != null) {
					val getUser = userLocalDataSource.getUserByMobile(phoneNumber)
					if (getUser == null) {
						userLocalDataSource.clearUser()
						val uData = authRemoteDataSource.getUserByMobile(phoneNumber)
						if (uData != null) {
							userLocalDataSource.addUser(uData)
						}
					}
				}
			}
		}
	}

	suspend fun hardRefreshUserData() {
		userLocalDataSource.clearUser()
		val mobile = sessionManager.getPhoneNumber()
		if (mobile != null) {
			val uData = authRemoteDataSource.getUserByMobile(mobile)
			if (uData != null) {
				userLocalDataSource.addUser(uData)
			}
		}
	}

	suspend fun insertProductToLikes(productId: String, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onLikeProduct: adding product to remote source")
				authRemoteDataSource.likeProduct(productId, userId)
			}
			val localRes = async {
				Log.d(TAG, "onLikeProduct: updating product to local source")
				val userRes = authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun removeProductFromLikes(productId: String, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onDislikeProduct: deleting product from remote source")
				authRemoteDataSource.dislikeProduct(productId, userId)
			}
			val localRes = async {
				Log.d(TAG, "onDislikeProduct: deleting product from local source")
				val userRes =
					authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun insertAddress(newAddress: UserData.Address, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onInsertAddress: adding address to remote source")
				authRemoteDataSource.insertAddress(newAddress, userId)
			}
			val localRes = async {
				Log.d(TAG, "onInsertAddress: updating address to local source")
				val userRes = authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun updateAddress(newAddress: UserData.Address, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onUpdateAddress: updating address on remote source")
				authRemoteDataSource.updateAddress(newAddress, userId)
			}
			val localRes = async {
				Log.d(TAG, "onUpdateAddress: updating address on local source")
				val userRes =
					authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun deleteAddressById(addressId: String, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onDelete: deleting address from remote source")
				authRemoteDataSource.deleteAddress(addressId, userId)
			}
			val localRes = async {
				Log.d(TAG, "onDelete: deleting address from local source")
				val userRes =
					authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun insertCartItemByUserId(
		cartItem: UserData.CartItem,
		userId: String
	): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onInsertCartItem: adding item to remote source")
				authRemoteDataSource.insertCartItem(cartItem, userId)
			}
			val localRes = async {
				Log.d(TAG, "onInsertCartItem: updating item to local source")
				val userRes = authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun updateCartItemByUserId(
		cartItem: UserData.CartItem,
		userId: String
	): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onUpdateCartItem: updating cart item on remote source")
				authRemoteDataSource.updateCartItem(cartItem, userId)
			}
			val localRes = async {
				Log.d(TAG, "onUpdateCartItem: updating cart item on local source")
				val userRes =
					authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun deleteCartItemByUserId(itemId: String, userId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onDelete: deleting cart item from remote source")
				authRemoteDataSource.deleteCartItem(itemId, userId)
			}
			val localRes = async {
				Log.d(TAG, "onDelete: deleting cart item from local source")
				val userRes =
					authRemoteDataSource.getUserById(userId)
				if (userRes is Success) {
					userLocalDataSource.clearUser()
					userLocalDataSource.addUser(userRes.data!!)
				} else if (userRes is Error) {
					throw userRes.exception
				}
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?> {
		return userLocalDataSource.getAddressesByUserId(userId)
	}

	suspend fun getLikesByUserId(userId: String): Result<List<String>?> {
		return userLocalDataSource.getLikesByUserId(userId)
	}

	suspend fun getUserData(userId: String): Result<UserData?> {
		return userLocalDataSource.getUserById(userId)
	}

}