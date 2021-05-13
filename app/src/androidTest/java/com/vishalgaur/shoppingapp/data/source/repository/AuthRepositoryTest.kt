package com.vishalgaur.shoppingapp.data.source.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.google.firebase.FirebaseApp
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.FakeUserDataSource
import com.vishalgaur.shoppingapp.data.utils.SignUpErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthRepositoryTest {
	private val userSeller = UserData(
		"weoifhwenf29385",
		"Seller Name",
		"+919999990000",
		"somemail@mail.com",
		"12345",
		emptyList(),
		emptyList(),
		emptyList(),
		"SELLER",
	)
	private val userCustomer = UserData(
		"dwoeihwjklvn48329752",
		"Customer Name",
		"+919090909090",
		"somemail1232@mail.com",
		"12345",
		emptyList(),
		emptyList(),
		emptyList(),
		"CUSTOMER",
	)

	private lateinit var userLocalDataSource: FakeUserDataSource
	private lateinit var authRemoteDataSource: FakeUserDataSource
	private lateinit var sessionManager: ShoppingAppSessionManager

	// class under test
	private lateinit var authRepository: AuthRepository

	@Before
	fun createRepository() {
		FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
		userLocalDataSource = FakeUserDataSource(userSeller)
		authRemoteDataSource = FakeUserDataSource(userCustomer)
		sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())

		authRepository = AuthRepository(
			userLocalDataSource,
			authRemoteDataSource,
			ApplicationProvider.getApplicationContext()
		)
	}

	@Test
	fun login_getUserDetailFromSession() = runBlockingTest {
		authRepository.login(userSeller, true)
		val result = sessionManager.getUserDataFromSession()

		assertThat(result["userName"], `is`(userSeller.name))
		assertThat(result["userId"], `is`(userSeller.userId))
		assertThat(result["userMobile"], `is`(userSeller.mobile))
	}

	@Test
	fun singUp_addsUserToSources() = runBlockingTest {
		authRepository.signUp(userCustomer)

		val resultSession = sessionManager.getUserDataFromSession()
		assertThat(resultSession["userName"], `is`(userCustomer.name))
		assertThat(resultSession["userId"], `is`(userCustomer.userId))
		assertThat(resultSession["userMobile"], `is`(userCustomer.mobile))

		val localRes = userLocalDataSource.getUserById(userCustomer.userId)
		assertThat(localRes, `is`(Result.Success(userCustomer)))

		val remoteRes = authRemoteDataSource.getUserById(userCustomer.userId)
		assertThat(remoteRes, `is`(Result.Success(userCustomer)))
	}

	@Test
	fun checkEmailAndMobile_existingEmail_returnsError() {
		authRemoteDataSource.updateEmailsAndMobiles("mail123@mail.com", "+919999988888")
		runOnUiThread {
			runBlockingTest {
				val result = authRepository.checkEmailAndMobile("mail123@mail.com", "+919685")
				assertThat(result, `is`(SignUpErrors.SERR))
			}
		}
	}

	@Test
	fun checkEmailAndMobile_existingMobile_returnsError() {
		authRemoteDataSource.updateEmailsAndMobiles("mail123@mail.com", "+919999988888")
		runOnUiThread {
			runBlockingTest {
				val result = authRepository.checkEmailAndMobile("mail999@mail.com", "+919999988888")
				assertThat(result, `is`(SignUpErrors.SERR))
			}
		}
	}

	@Test
	fun checkEmailAndMobile_existingMobileAndEmail_returnsError() {
		authRemoteDataSource.updateEmailsAndMobiles("mail123@mail.com", "+919999988888")
		runOnUiThread {
			runBlockingTest {
				val result = authRepository.checkEmailAndMobile("mail123@mail.com", "+919999988888")
				assertThat(result, `is`(SignUpErrors.SERR))
			}
		}
	}

	@Test
	fun checkEmailAndMobile_newData_returnsError() {
		authRemoteDataSource.updateEmailsAndMobiles("mail123@mail.com", "+919999988888")
		runOnUiThread {
			runBlockingTest {
				val result =
					authRepository.checkEmailAndMobile("somemail123@mail.com", "+919999977777")
				assertThat(result, `is`(SignUpErrors.NONE))
			}
		}
	}

	@Test
	fun checkLogin_existingUser_returnsData() = runBlockingTest {
		val result = authRepository.checkLogin(userCustomer.mobile, userCustomer.password)
		assertThat(result, `is`(userCustomer))
	}

	@Test
	fun checkLogin_newCredentials_returnsNull() = runBlockingTest {
		val result = authRepository.checkLogin("+919879879879", "sdygt4")
		assertThat(result, `is`(nullValue()))
	}

	@Test
	fun signOut_clearsSessionAndData() = runBlockingTest {
		authRepository.signOut()

		val sessionRes = sessionManager.isLoggedIn()
		val localRes = userLocalDataSource.getUserById(userSeller.userId)

		assertThat(sessionRes, `is`(false))
		if (localRes is Result.Success)
			assert(false)
		else if (localRes is Result.Error) {
			assertEquals(localRes.exception.message, "User Not Found")
		}
	}

}