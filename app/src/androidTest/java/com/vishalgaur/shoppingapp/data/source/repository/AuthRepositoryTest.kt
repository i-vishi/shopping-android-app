package com.vishalgaur.shoppingapp.data.source.repository

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.FakeUserDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
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
		"SELLER",
	)
	private val userCustomer = UserData(
		"dwoeihwjklvn48329752",
		"Customer Name",
		"+919090909090",
		"somemail1232@mail.com",
		"12345",
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
	fun getUserDetail() = runBlockingTest {
		authRepository.login(userSeller, true)
		authRepository.refreshData()
		val result = sessionManager.getUserDataFromSession()

		assertThat(result, `is`(userSeller.toHashMap()))
	}

}