package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.getOrAwaitValue
import com.vishalgaur.shoppingapp.ui.LoginViewErrors
import com.vishalgaur.shoppingapp.ui.SignUpViewErrors
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthViewModelTest {
	private lateinit var authViewModel: AuthViewModel

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		authViewModel = AuthViewModel(ApplicationProvider.getApplicationContext())
	}

	@Test
	fun signUpSubmitData_noData_returnsEmptyError() {
		val name = ""
		val mobile = ""
		val email = ""
		val pwd1 = ""
		val pwd2 = ""
		val isAccepted = false
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_EMPTY))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_notAccepted_returnsNotAccError() {
		val name = "uigbivs ihgfdsg"
		val mobile = "9988665555"
		val email = "owhfoi@oihw.cro"
		val pwd1 = "1234"
		val pwd2 = "1234"
		val isAccepted = false
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_NOT_ACC))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_pwdNotEq_returnsPwdError() {
		val name = "uigbivs ihgfdsg"
		val mobile = "9988665555"
		val email = "owhfoi@oihw.cro"
		val pwd1 = "12345"
		val pwd2 = "1234"
		val isAccepted = false
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_PWD12NS))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_invalidEmail_returnsEmailError() {
		val name = "uigbivs ihgfdsg"
		val mobile = "9988665555"
		val email = "owhfoi@oihwo"
		val pwd1 = "1234"
		val pwd2 = "1234"
		val isAccepted = true
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_EMAIL))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_invalidMobile_returnsMobError() {
		val name = "uigbivs ihgfdsg"
		val mobile = "9988665fng55"
		val email = "owhfoi@oihw.coo"
		val pwd1 = "1234"
		val pwd2 = "1234"
		val isAccepted = true
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_MOBILE))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_invalidEmailMobile_returnsEmailMobError() {
		val name = "uigbivs ihgfdsg"
		val mobile = "9988665fng55"
		val email = "owhfoi@oihwoo"
		val pwd1 = "1234"
		val pwd2 = "1234"
		val isAccepted = true
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(SignUpViewErrors.ERR_EMAIL_MOBILE))
		assertThat(authViewModel.userData.value, `is`(nullValue()))
	}

	@Test
	fun signUpSubmitData_validData_returnsNoError() {
		val name = "   uigbivs ihgfdsg"
		val mobile = "   9988665755"
		val email = "owhfoi@oihwoo.cwdo    "
		val pwd1 = "1234"
		val pwd2 = "1234"
		val isAccepted = true
		val isSeller = false
		authViewModel.signUpSubmitData(name, mobile, email, pwd1, pwd2, isAccepted, isSeller)
		val result = authViewModel.errorStatus.getOrAwaitValue()
		val dataRes = authViewModel.userData.getOrAwaitValue()
		assertThat(result, `is`(SignUpViewErrors.NONE))
		assertThat(dataRes, `is`(notNullValue()))
		assertThat(dataRes.name, `is`("uigbivs ihgfdsg"))
	}

	@Test
	fun loginSubmitData_noData_returnsEmptyError() {
		val mobile = ""
		val pwd = ""
		authViewModel.loginSubmitData(mobile, pwd)
		val result = authViewModel.errorStatusLoginFragment.getOrAwaitValue()

		assertThat(result, `is`(LoginViewErrors.ERR_EMPTY))
	}

	@Test
	fun loginSubmitData_invalidMobile_returnsMobileError() {
		val mobile = "9fwd988556699"
		val pwd = "123"
		authViewModel.loginSubmitData(mobile, pwd)
		val result = authViewModel.errorStatusLoginFragment.getOrAwaitValue()

		assertThat(result, `is`(LoginViewErrors.ERR_MOBILE))
	}

	@Test
	fun loginSubmitData_validData_returnsNoError() {
		val mobile = "9988556699"
		val pwd = "123"
		authViewModel.loginSubmitData(mobile, pwd)
		val result = authViewModel.errorStatusLoginFragment.getOrAwaitValue()

		assertThat(result, `is`(LoginViewErrors.NONE))
	}
}