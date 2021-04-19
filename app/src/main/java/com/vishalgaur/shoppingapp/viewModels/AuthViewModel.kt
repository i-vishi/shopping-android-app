package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.vishalgaur.shoppingapp.*
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.isEmailValid
import com.vishalgaur.shoppingapp.network.LogInErrors
import com.vishalgaur.shoppingapp.network.SignUpErrors
import com.vishalgaur.shoppingapp.repository.AuthRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var currUser: LiveData<FirebaseUser?>

    private val authRepository = AuthRepository(application)

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> get() = _userData

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _signErrorStatus = MutableLiveData<SignUpErrors?>()
    val signErrorStatus: LiveData<SignUpErrors?> get() = _signErrorStatus

    private val _errorStatus = MutableLiveData<ViewErrors>()
    val errorStatus: LiveData<ViewErrors> get() = _errorStatus

    private val _errorStatusLoginFragment = MutableLiveData<LoginViewErrors>()
    val errorStatusLoginFragment: LiveData<LoginViewErrors> get() = _errorStatusLoginFragment

    private val _loginErrorStatus = MutableLiveData<LogInErrors?>()
    val loginErrorStatus: LiveData<LogInErrors?> get() = _loginErrorStatus

    init {
        _isLoggedIn.value = false
        currUser = MutableLiveData()
        _errorStatus.value = ViewErrors.NONE
        _errorStatusLoginFragment.value = LoginViewErrors.NONE
        refreshStatus()
    }

    private fun refreshStatus() {
        viewModelScope.launch {
            getCurrUser()
            _isLoggedIn.value = currUser.value != null
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun signUpSubmitData(
        name: String,
        mobile: String,
        email: String,
        pwd1: String,
        pwd2: String,
        isAccepted: Boolean
    ) {
        if (name.isBlank() || mobile.isBlank() || email.isBlank() || pwd1.isBlank() || pwd2.isBlank()) {
            _errorStatus.value = ViewErrors.ERR_EMPTY
        } else {
            if (pwd1 != pwd2) {
                _errorStatus.value = ViewErrors.ERR_PWD12NS
            } else {
                if (!isAccepted) {
                    _errorStatus.value = ViewErrors.ERR_NOT_ACC
                } else {
                    var err = ERR_INIT
                    if (!isEmailValid(email)) {
                        err += ERR_EMAIL
                    }
                    if (!isPhoneValid(mobile)) {
                        err += ERR_MOBILE
                    }
                    when (err) {
                        ERR_INIT -> {
                            _errorStatus.value = ViewErrors.NONE
                            val uId = getRandomString(32)
                            val newData =
                                UserData(
                                    uId,
                                    name.trim(),
                                    "+91" + mobile.trim(),
                                    email.trim(),
                                    pwd1.trim(),
                                )
                            _userData.value = newData
                            signUp(newData)
                        }
                        (ERR_INIT + ERR_EMAIL) -> _errorStatus.value = ViewErrors.ERR_EMAIL
                        (ERR_INIT + ERR_MOBILE) -> _errorStatus.value = ViewErrors.ERR_MOBILE
                        (ERR_INIT + ERR_EMAIL + ERR_MOBILE) -> _errorStatus.value =
                            ViewErrors.ERR_EMAIL_MOBILE
                    }
                }
            }
        }

    }

    private fun signUp(uData: UserData) {
        viewModelScope.launch {
            Log.d(TAG, "checking email and mobile")
            authRepository.checkEmailMobile(uData.email, uData.mobile)
            _signErrorStatus.value = authRepository.sErrStatus.value
        }
    }

    fun loginSubmitData(mobile: String, password: String, isRemOn: Boolean) {
        if (mobile.isBlank() || password.isBlank()) {
            _errorStatusLoginFragment.value = LoginViewErrors.ERR_EMPTY
        } else {
            if (!isPhoneValid(mobile)) {
                _errorStatusLoginFragment.value = LoginViewErrors.ERR_MOBILE
            } else {
                _errorStatusLoginFragment.value = LoginViewErrors.NONE
                logIn("+91" + mobile.trim(), password)
            }
        }


    }

    private fun logIn(
        phoneNumber: String,
        pwd: String
    ) {
        viewModelScope.launch {
            Log.d(TAG, "checking mobile")
            val res = async { authRepository.checkLogin(phoneNumber, pwd) }
            _userData.value = res.await()
            Log.d(TAG, "data = ${_userData.value}")
            if (_userData.value != null) {
                _loginErrorStatus.postValue(LogInErrors.NONE)
            } else {
                _loginErrorStatus.postValue(LogInErrors.LERR)
            }
        }
    }

    private fun getCurrUser() {
        currUser = authRepository.firebaseUser
    }
}