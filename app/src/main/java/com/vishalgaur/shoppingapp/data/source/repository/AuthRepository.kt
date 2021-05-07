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
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserLocalDataSource
import com.vishalgaur.shoppingapp.data.source.remote.AuthRemoteDataSource
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import com.vishalgaur.shoppingapp.data.utils.UserType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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

	fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, isUserLoggedIn: MutableLiveData<Boolean>) {
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
				userLocalDataSource.clearUser()
				if (phoneNumber != null) {
					val uData = authRemoteDataSource.getUserByMobile(phoneNumber)
					if (uData != null) {
						userLocalDataSource.addUser(uData)
					}
				}
			}
		}

	}
}