package com.vishalgaur.shoppingapp.data.source.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserLocalDataSource
import com.vishalgaur.shoppingapp.data.source.remote.AuthRemoteDataSource
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import com.vishalgaur.shoppingapp.data.utils.UserType
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository(private val application: Application) {
    private val userLocalDataSource: UserLocalDataSource
    private val authRemoteDataSource: AuthRemoteDataSource

    private var firebaseAuth: FirebaseAuth = Firebase.auth
    private var sessionManager = ShoppingAppSessionManager(application.applicationContext)

    var isLoggedIn = MutableLiveData(false)

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getRepository(app: Application): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                AuthRepository(app).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {
        val database = ShoppingAppDatabase.getInstance(application)
        userLocalDataSource = UserLocalDataSource(database.userDao())
        authRemoteDataSource = AuthRemoteDataSource()
    }

    fun getFirebaseAuth() = firebaseAuth

    fun isRememberMeOn() = sessionManager.isRememberMeOn()

    suspend fun refreshData() {
        if (sessionManager.isLoggedIn()) {
            isLoggedIn.value = true
            updateUserInLocalSource(sessionManager.getPhoneNumber())
        } else {
            isLoggedIn.value = false
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
        val queryResult =
            authRemoteDataSource.getEmailsAndMobiles().await().toObject(EmailMobileData::class.java)
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
            authRemoteDataSource.getUserByMobileAndPassword(mobile, password).await().documents
        return if (queryResult.size > 0) {
            queryResult[0].toObject(UserData::class.java)
        } else {
            null
        }
    }

    fun verifyPhoneWithCode(verificationId: String, code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            Log.d(TAG, "onVerifyWithCode: Exception Occurred: ${e.message}")
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    if (user != null) {
                        isLoggedIn.value = true
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d(TAG, "createUserWithMobile:failure", task.exception)
                        isLoggedIn.value = false
                        makeErrToast("Wrong OTP!")
                    }
                }
            }
    }

    suspend fun signOut() {
        sessionManager.logoutFromSession()
        firebaseAuth.signOut()
        isLoggedIn.value = false
        userLocalDataSource.clearUser()
    }

    private fun makeErrToast(text: String) {
        Toast.makeText(application.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private suspend fun deleteUserFromLocalSource() {
        userLocalDataSource.clearUser()
    }

    private suspend fun updateUserInLocalSource(phoneNumber: String?) {
        userLocalDataSource.clearUser()
        if (phoneNumber != null) {
            val uData = authRemoteDataSource.getUserByMobile(phoneNumber)
                .await()
                .documents[0]
                .toObject(UserData::class.java)
            if (uData != null) {
                userLocalDataSource.addUser(uData)
            }
        }
    }
}