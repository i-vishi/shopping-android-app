package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.vishalgaur.shoppingapp.*
import com.vishalgaur.shoppingapp.database.UserData
import com.vishalgaur.shoppingapp.isEmailValid
import com.vishalgaur.shoppingapp.repository.AuthRepository
import com.vishalgaur.shoppingapp.ui.loginSignup.LoginSignupActivity
import com.vishalgaur.shoppingapp.ui.loginSignup.SignupFragment
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    var currUser: LiveData<FirebaseUser?>

    private val authRepository = AuthRepository(application)

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _errorStatus = MutableLiveData<ViewErrors>()
    val errorStatus: LiveData<ViewErrors> get() = _errorStatus

    init {
        _isLoggedIn.value = false
        currUser = MutableLiveData()
        _errorStatus.value = ViewErrors.NONE
        refreshStatus()
    }

    private fun refreshStatus() {
        viewModelScope.launch {
            getCurrUser()
            _isLoggedIn.value = currUser.value != null
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun submitData(
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
                                    mobile.trim(),
                                    email.trim(),
                                    pwd1.trim(),
                                )
                            signUp(email, pwd1)
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

    private fun verifyMobile(mobile: String) {

    }

    private fun login() {}

    @RequiresApi(Build.VERSION_CODES.P)
    private fun signUp(email: String, password: String) {
        authRepository.signUp(email, password)
    }

    private fun getCurrUser() {
        currUser = authRepository.firebaseUser
    }
}