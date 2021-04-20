package com.vishalgaur.shoppingapp.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.database.UserDatabase
import com.vishalgaur.shoppingapp.network.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "AuthRepository"

class AuthRepository(private val application: Application) {

    private var userDatabase: UserDatabase = UserDatabase.getInstance(application)

    private var firebaseAuth: FirebaseAuth = Firebase.auth

    private var db = FirebaseDbUtils()

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> get() = _firebaseUser

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    suspend fun refreshData() {
        if (firebaseAuth.currentUser != null) {
            _firebaseUser.postValue(firebaseAuth.currentUser)
            updateUserInRoom(firebaseAuth.currentUser.phoneNumber)
            _isLoggedIn.postValue(true)
        } else {
            _firebaseUser.value = null
            _isLoggedIn.value = false
        }
    }

    fun getFirebaseAuth(): FirebaseAuth = firebaseAuth

    fun signUp(uData: UserData) {
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
        firebaseAuth.signOut()
        _isLoggedIn.postValue(false)
        userDatabase.userDao().clear()
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    _firebaseUser.postValue(user)
                    _isLoggedIn.postValue(true)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.d(TAG, "createUserWithMobile:failure", task.exception)
                        _isLoggedIn.postValue(false)
                        _firebaseUser.postValue(null)
                        makeErrToast("Wrong OTP!")
                    }
                }
            }
    }

    private suspend fun updateUserInRoom(pNumber: String?) {
        if (pNumber != null) {
            val uData =
                db.getUserByMobile(pNumber).await().documents[0].toObject(UserData::class.java)
            if (uData != null) {
                userDatabase.userDao().clear()
                userDatabase.userDao().insert(uData)
            }
        }
    }

}