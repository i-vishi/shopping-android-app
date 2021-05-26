package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.vishalgaur.shoppingapp.ShoppingApplication
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.ui.OTPStatus
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "OtpViewModel"

class OtpViewModel(application: Application, private val uData: UserData) :
	AndroidViewModel(application) {

	private val _otpStatus = MutableLiveData<OTPStatus>()
	val otpStatus: LiveData<OTPStatus> get() = _otpStatus

	private val _isOTPSent = MutableLiveData<Boolean>()
	val isOTPSent: LiveData<Boolean> get() = _isOTPSent

	private val authRepository = (application as ShoppingApplication).authRepository

	var isUserLoggedIn = MutableLiveData(false)
	var storedVerificationId: String? = ""
	private var verificationInProgress = false
	private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

	init {
		_isOTPSent.value = false
	}

	fun verifyOTP(otp: String) {
		viewModelScope.launch {
			verifyPhoneWithCode(storedVerificationId!!, otp, isUserLoggedIn)
		}
	}

	fun signUp() {
		viewModelScope.launch {
			authRepository.signUp(uData)
		}
	}

	fun login(rememberMe: Boolean) {
		viewModelScope.launch {
			authRepository.login(uData, rememberMe)
		}
	}

	fun verifyPhoneOTPStart(phoneNumber: String, activity: FragmentActivity) {
		val options = PhoneAuthOptions.newBuilder(authRepository.getFirebaseAuth())
			.setPhoneNumber(phoneNumber)       // Phone number to verify
			.setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
			.setActivity(activity)                 // Activity (for callback binding)
			.setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
			.build()
		PhoneAuthProvider.verifyPhoneNumber(options)

		verificationInProgress = true
	}

	private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

		override fun onVerificationCompleted(credential: PhoneAuthCredential) {
			Log.d(TAG, "onVerificationCompleted:$credential")
			authRepository.signInWithPhoneAuthCredential(credential, isUserLoggedIn, application.applicationContext)
		}

		override fun onVerificationFailed(e: FirebaseException) {
			Log.w(TAG, "onVerificationFailed", e)

			if (e is FirebaseAuthInvalidCredentialsException) {
				Log.w(TAG, "onVerificationFailed, invalid request, ", e)
			} else if (e is FirebaseTooManyRequestsException) {
				Log.w(TAG, "onVerificationFailed, sms quota exceeded, ", e)
			}
		}

		override fun onCodeSent(
			verificationId: String,
			token: PhoneAuthProvider.ForceResendingToken
		) {
			// Save verification ID and resending token so we can use them later
			storedVerificationId = verificationId
			resendToken = token
			Log.w(TAG, "OTP SENT")
			_isOTPSent.value = true
		}
	}

	private fun verifyPhoneWithCode(verificationId: String, code: String, isUserLoggedIn: MutableLiveData<Boolean>) {
		try {
			val credential = PhoneAuthProvider.getCredential(verificationId, code)
			authRepository.signInWithPhoneAuthCredential(credential, isUserLoggedIn, getApplication<ShoppingApplication>().applicationContext)
		} catch (e: Exception) {
			Log.d(TAG, "onVerifyWithCode: Exception Occurred: ${e.message}")
		}
	}
}