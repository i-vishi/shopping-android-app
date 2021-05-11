package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepository
import com.vishalgaur.shoppingapp.data.utils.LogInErrors
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import com.vishalgaur.shoppingapp.data.utils.UserType
import com.vishalgaur.shoppingapp.ui.LoginViewErrors
import com.vishalgaur.shoppingapp.ui.SignUpViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel(application: Application) : AndroidViewModel(application) {

	private val authRepository = AuthRepository.getRepository(application)
	private val productsRepository = ProductsRepository.getRepository(application)

	private val _userData = MutableLiveData<UserData>()
	val userData: LiveData<UserData> get() = _userData

	private val _signErrorStatus = MutableLiveData<SignUpErrors?>()
	val signErrorStatus: LiveData<SignUpErrors?> get() = _signErrorStatus

	private val _errorStatus = MutableLiveData<SignUpViewErrors>()
	val errorStatus: LiveData<SignUpViewErrors> get() = _errorStatus

	private val _errorStatusLoginFragment = MutableLiveData<LoginViewErrors>()
	val errorStatusLoginFragment: LiveData<LoginViewErrors> get() = _errorStatusLoginFragment

	private val _loginErrorStatus = MutableLiveData<LogInErrors?>()
	val loginErrorStatus: LiveData<LogInErrors?> get() = _loginErrorStatus

	init {
		_errorStatus.value = SignUpViewErrors.NONE
		_errorStatusLoginFragment.value = LoginViewErrors.NONE
		refreshStatus()
	}

	private fun refreshStatus() {
		viewModelScope.launch {
			getCurrUser()
			productsRepository.refreshProducts()
		}
	}

	fun signUpSubmitData(
		name: String,
		mobile: String,
		email: String,
		pwd1: String,
		pwd2: String,
		isAccepted: Boolean,
		isSeller: Boolean
	) {
		if (name.isBlank() || mobile.isBlank() || email.isBlank() || pwd1.isBlank() || pwd2.isBlank()) {
			_errorStatus.value = SignUpViewErrors.ERR_EMPTY
		} else {
			if (pwd1 != pwd2) {
				_errorStatus.value = SignUpViewErrors.ERR_PWD12NS
			} else {
				if (!isAccepted) {
					_errorStatus.value = SignUpViewErrors.ERR_NOT_ACC
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
							_errorStatus.value = SignUpViewErrors.NONE
							val uId = getRandomString(32, "91" + mobile.trim(), 6)
							val newData =
								UserData(
									uId,
									name.trim(),
									"+91" + mobile.trim(),
									email.trim(),
									pwd1.trim(),
									ArrayList(),
									ArrayList(),
									if (isSeller) UserType.SELLER.name else UserType.CUSTOMER.name
								)
							_userData.value = newData
							checkUniqueUser(newData)
						}
						(ERR_INIT + ERR_EMAIL) -> _errorStatus.value = SignUpViewErrors.ERR_EMAIL
						(ERR_INIT + ERR_MOBILE) -> _errorStatus.value = SignUpViewErrors.ERR_MOBILE
						(ERR_INIT + ERR_EMAIL + ERR_MOBILE) -> _errorStatus.value =
							SignUpViewErrors.ERR_EMAIL_MOBILE
					}
				}
			}
		}

	}

	private fun checkUniqueUser(uData: UserData) {
		viewModelScope.launch {
			val res = async { authRepository.checkEmailAndMobile(uData.email, uData.mobile) }
			_signErrorStatus.value = res.await()
		}
	}

	fun loginSubmitData(mobile: String, password: String) {
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

	private fun logIn(phoneNumber: String, pwd: String) {
		viewModelScope.launch {
			val res = async { authRepository.checkLogin(phoneNumber, pwd) }
			_userData.value = res.await()
			if (_userData.value != null) {
				_loginErrorStatus.value = LogInErrors.NONE
			} else {
				_loginErrorStatus.value = LogInErrors.LERR
			}
		}
	}

	private suspend fun getCurrUser() {
		Log.d(TAG, "refreshing data...")
		authRepository.refreshData()
	}
}