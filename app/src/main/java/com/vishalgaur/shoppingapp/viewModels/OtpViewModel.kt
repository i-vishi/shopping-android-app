package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.vishalgaur.shoppingapp.OTPStatus
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.repository.AuthRepository
import kotlinx.coroutines.launch

private const val TAG = "OtpViewModel"

class OtpViewModel(application: Application, private val uData: UserData) :
    AndroidViewModel(application) {

    private val _otpStatus = MutableLiveData<OTPStatus>()
    val otpStatus: LiveData<OTPStatus> get() = _otpStatus

    val authRepository = AuthRepository(application)

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _verId = MutableLiveData<String>()
    val verId: LiveData<String> get() = _verId

    init {
        _verId.value = authRepository.storedVerificationId
        Log.d(TAG, "ver id: ${_verId.value}")
    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun verifyOTP(otp: String) {
        Log.d(TAG, "OTP: $otp")
        _verId.value = authRepository.storedVerificationId
        viewModelScope.launch {
            if (authRepository.storedVerificationId != null) {
                _verId.value = authRepository.storedVerificationId
            }
            authRepository.verifyPhoneWithCode(verId.value!!, otp)
            signUp(uData)
            _isLoggedIn.postValue(authRepository.isLoggedIn.value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun signUp(newData: UserData) {
        viewModelScope.launch {
            authRepository.signUp(newData)
        }
    }
}