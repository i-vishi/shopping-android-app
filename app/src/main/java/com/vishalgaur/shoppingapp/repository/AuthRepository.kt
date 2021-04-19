package com.vishalgaur.shoppingapp.repository

import android.app.Activity
import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.database.UserDatabase
import com.vishalgaur.shoppingapp.network.EmailMobileData
import com.vishalgaur.shoppingapp.network.SignUpErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.concurrent.TimeUnit

private const val TAG = "AuthRepository"
private const val USERS_COLLECTION = "users"

class AuthRepository(private val application: Application) {

    private var userDatabase: UserDatabase

    private var firebaseAuth: FirebaseAuth

    private var firebaseDb = Firebase.firestore

    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> get() = _firebaseUser

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _sErrStatus = MutableLiveData<SignUpErrors?>()
    val sErrStatus: LiveData<SignUpErrors?> get() = _sErrStatus

    private var verificationInProgress = false
    var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    init {
        firebaseAuth = Firebase.auth

        userDatabase = UserDatabase.getInstance(application)

        if (firebaseAuth.currentUser != null) {
            _firebaseUser.postValue(firebaseAuth.currentUser)
            _isLoggedIn.postValue(true)
        } else {
            _firebaseUser.value = null
            _isLoggedIn.value = false
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @RequiresApi(Build.VERSION_CODES.P)
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                Log.d(TAG, "store = $storedVerificationId")
                resendToken = token
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
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
            Log.d(TAG, "checking email and password")
            firebaseDb.collection(USERS_COLLECTION).document("emailAndMobiles")
                .get()
                .addOnSuccessListener { doc ->
                    Log.d(TAG, "mob = $mobile")
                    val emObj = doc.toObject(EmailMobileData::class.java)
                    val mob = emObj?.mobiles?.contains(mobile)
                    val em = emObj?.emails?.contains(email)
                    Log.d(TAG, "m = $mob, e = $em")
                    if (mob == false && em == false) {
                        _sErrStatus.value = SignUpErrors.NONE
                    } else {
                        _sErrStatus.value = SignUpErrors.SERR
                    }

                    when {
                        mob == false && em == true -> makeSignErrToast("Email is already registered!")
                        mob == true && em == false -> makeSignErrToast("Mobile is already registered!")
                        mob == true && em == true -> makeSignErrToast("Email and mobile is already registered!")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "exception: $e")
                }
        }
    }

    private fun makeSignErrToast(text: String) {
        Toast.makeText(application.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    fun verifyPhoneOTPStart(phoneNumber: String, activity: FragmentActivity) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        verificationInProgress = true
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun verifyPhoneWithCode(verCode: String, code: String) {
        Log.d(TAG, " id = $storedVerificationId")
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


    @RequiresApi(Build.VERSION_CODES.P)
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(application.mainExecutor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    _firebaseUser.postValue(user)
                    _isLoggedIn.postValue(true)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d(TAG, "createUserWithMobile:failure", task.exception)
                        Toast.makeText(
                            application.applicationContext,
                            "Authentication failed! Enter Correct OTP",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // Update UI
                    _isLoggedIn.postValue(false)
                }
            }
    }

}