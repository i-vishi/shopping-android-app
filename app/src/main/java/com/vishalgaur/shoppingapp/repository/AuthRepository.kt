package com.vishalgaur.shoppingapp.repository

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.database.UserDatabase
import com.vishalgaur.shoppingapp.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

private const val TAG = "AuthRepository"

class AuthRepository(private val application: Application) {

    private var userDatabase: UserDatabase = UserDatabase.getInstance(application)

    private var firebaseAuth: FirebaseAuth = Firebase.auth

    private var firebaseDb = Firebase.firestore

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> get() = _firebaseUser

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _sErrStatus = MutableLiveData<SignUpErrors?>()
    val sErrStatus: LiveData<SignUpErrors?> get() = _sErrStatus

    init {
        if (firebaseAuth.currentUser != null) {
            _firebaseUser.postValue(firebaseAuth.currentUser)
//            _isLoggedIn.postValue(true)
        } else {
            _firebaseUser.value = null
            _isLoggedIn.value = false
        }
    }

    fun getFirebaseAuth(): FirebaseAuth = firebaseAuth

    suspend fun signUp(uData: UserData) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "updating data on Room")
            userDatabase.userDao().clear()
            userDatabase.userDao().insert(uData)
            Log.d(TAG, "updating data on Network...")

            firebaseDb.collection(USERS_COLLECTION).add(uData.toHashMap())
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "Doc added")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "firestore error occurred: $e")
                }
        }

    }

    suspend fun checkEmailMobile(email: String, mobile: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "checking email and mobile")
            firebaseDb.collection(USERS_COLLECTION).document(EMAIL_MOBILE_DOC)
                .get()
                .addOnSuccessListener { doc ->
                    val emObj = doc.toObject(EmailMobileData::class.java)
                    val mob = emObj?.mobiles?.contains(mobile)
                    val em = emObj?.emails?.contains(email)
                    Log.d(TAG, "m = $mob, e = $em")
                    if (mob == false && em == false) {
                        _sErrStatus.value = SignUpErrors.NONE
                    } else {
                        _sErrStatus.value = SignUpErrors.SERR
                        when {
                            mob == false && em == true -> makeErrToast("Email is already registered!")
                            mob == true && em == false -> makeErrToast("Mobile is already registered!")
                            mob == true && em == true -> makeErrToast("Email and mobile is already registered!")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "exception: $e")
                }
        }
    }

    private fun makeErrToast(text: String) {
        Toast.makeText(application.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    suspend fun checkLogin(mobile: String, password: String): UserData? {
        Log.d(TAG, "checking mobile and password")
        val queryData = firebaseDb.collection(USERS_COLLECTION).whereEqualTo("mobile", mobile)
            .whereEqualTo("password", password)
            .get().await().documents
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
                        makeErrToast("Wrong OTP!")
                    }
                }
            }
    }

}