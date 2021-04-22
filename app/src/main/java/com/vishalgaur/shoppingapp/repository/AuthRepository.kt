package com.vishalgaur.shoppingapp.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.database.ShoppingAppDb
import com.vishalgaur.shoppingapp.database.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.database.user.UserData
import com.vishalgaur.shoppingapp.network.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "AuthRepository"

class AuthRepository(private val application: Application) {

    private var userDatabase: ShoppingAppDb = ShoppingAppDb.getInstance(application)

    private var firebaseAuth: FirebaseAuth = Firebase.auth

    private var db = FirebaseDbUtils()

    private var sessionManager = ShoppingAppSessionManager(application.applicationContext)

    var isLoggedIn = MutableLiveData(false)

    suspend fun refreshData() {
        if (sessionManager.isLoggedIn()) {
            isLoggedIn.value = true
            updateUserInRoom(sessionManager.getPhoneNumber())
        } else {
            sessionManager.logoutFromSession()
            deleteUserDataFromRoom()
            isLoggedIn.value = false
        }
    }

    fun getFirebaseAuth(): FirebaseAuth = firebaseAuth

    fun signUp(uData: UserData) {
        val isSeller = uData.userType == UserType.SELLER.name
        sessionManager.createLoginSession(uData.userId, uData.name, uData.mobile, false, isSeller)
        Log.d(TAG, "updating user data on Room")
        userDatabase.userDao().clear()
        userDatabase.userDao().insert(uData)
        Log.d(TAG, "updating user data on Network...")

        db.addUser(uData.toHashMap())
            .addOnSuccessListener {
                Log.d(TAG, "Doc added")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "firestore error occurred: $e")
            }

        db.updateEmailsAndMobiles(uData.email, uData.mobile)
    }

    fun login(uData: UserData, rememberMe: Boolean) {
        val isSeller = uData.userType == UserType.SELLER.name
        sessionManager.createLoginSession(uData.userId, uData.name, uData.mobile, rememberMe, isSeller)
    }

    suspend fun checkEmailMobile(email: String, mobile: String): SignUpErrors? {
        Log.d(TAG, "checking email and mobile")
        val queryData = db.getEmailsAndMobiles().await().toObject(EmailMobileData::class.java)
        var sErr: SignUpErrors? = null
        if (queryData != null) {
            val mob = queryData.mobiles.contains(mobile)
            val em = queryData.emails.contains(email)
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

    private fun makeErrToast(text: String) {
        Toast.makeText(application.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    suspend fun checkLogin(mobile: String, password: String): UserData? {
        Log.d(TAG, "checking mobile and password")
        val queryData = db.getUserByMobileAndPassword(mobile, password).await().documents
        return if (queryData.size > 0) queryData[0].toObject(UserData::class.java) else null
    }

    fun verifyPhoneWithCode(verCode: String, code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verCode, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            Log.d(TAG, "Exception Occurred: ${e.message}")
        }

    }

    fun signOut() {
        sessionManager.logoutFromSession()
        firebaseAuth.signOut()
        isLoggedIn.value = false
        userDatabase.userDao().clear()
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

    private suspend fun updateUserInRoom(pNumber: String?) {
        userDatabase.userDao().clear()
        if (pNumber != null) {
            val uData =
                db.getUserByMobile(pNumber).await().documents[0].toObject(UserData::class.java)
            if (uData != null) {
                userDatabase.userDao().insert(uData)
            }
        }
    }

    private fun deleteUserDataFromRoom() {
        userDatabase.userDao().clear()
    }

}