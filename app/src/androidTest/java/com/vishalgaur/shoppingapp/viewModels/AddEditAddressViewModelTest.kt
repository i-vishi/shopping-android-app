package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.ServiceLocator
import com.vishalgaur.shoppingapp.ShoppingApplication
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.FakeAuthRepository
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.getOrAwaitValue
import com.vishalgaur.shoppingapp.ui.AddAddressViewErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class AddEditAddressViewModelTest {
	private lateinit var addEditAddressViewModel: AddEditAddressViewModel
	private lateinit var authRepository: AuthRepoInterface

	val user = UserData(
		"sdjm43892yfh948ehod",
		"Vishal",
		"+919999988888",
		"vishal@somemail.com",
		"dh94328hd",
		ArrayList(),
		ArrayList(),
		ArrayList()
	)

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		val context = ApplicationProvider.getApplicationContext<ShoppingApplication>()
		val sessionManager = ShoppingAppSessionManager(context)
		authRepository = FakeAuthRepository(sessionManager)
		authRepository.login(user, true)
		ServiceLocator.authRepository = authRepository
		addEditAddressViewModel = AddEditAddressViewModel(context)

	}

	@After
	fun cleanUp() = runBlockingTest {
		ServiceLocator.resetRepository()
	}

	@Test
	fun setIsEdit_setsValue() {
		runBlockingTest {
			addEditAddressViewModel.setIsEdit(false)
			val res = addEditAddressViewModel.isEdit.getOrAwaitValue()
			assertThat(res, `is`(false))
		}
	}

	@Test
	fun setAddressData_setsData() = runBlockingTest {
		val address = UserData.Address(
			"add-id-121",
			"adg",
			"shgd",
			"IN",
			"sfhg45eyh",
			"",
			"kanpuit",
			"up",
			"309890",
			"9999988558"
		)
		authRepository.insertAddress(address, user.userId)
		addEditAddressViewModel.setAddressData(address.addressId)
		val result = addEditAddressViewModel.addressData.getOrAwaitValue()
		assertThat(result, `is`(address))
	}

	@Test
	fun submitAddress_emptyForm_returnsError() {
		val fname = "adg"
		val lname = ""
		val code = "IN"
		val streetAdd = "sfhg45eyh"
		val streetAdd2 = ""
		val city = ""
		val state = "up"
		val zip = "309890"
		val phone = "9999988558"
		addEditAddressViewModel.submitAddress(
			code,
			fname,
			lname,
			streetAdd,
			streetAdd2,
			city,
			state,
			zip,
			phone
		)
		val result = addEditAddressViewModel.errorStatus.getOrAwaitValue()

		assertThat(result.size, `is`(greaterThan(0)))
		assertThat(result.contains(AddAddressViewErrors.ERR_CITY_EMPTY), `is`(true))
	}

	@Test
	fun submitAddress_invalidZipcode_returnsError() {
		val fname = "adg"
		val lname = "serdg"
		val code = "IN"
		val streetAdd = "sfhg45eyh"
		val streetAdd2 = ""
		val city = "sfhg"
		val state = "up"
		val zip = "30990"
		val phone = "9999988558"
		addEditAddressViewModel.submitAddress(
			code,
			fname,
			lname,
			streetAdd,
			streetAdd2,
			city,
			state,
			zip,
			phone
		)
		val result = addEditAddressViewModel.errorStatus.getOrAwaitValue()

		assertThat(result.size, `is`(greaterThan(0)))
		assertThat(result.contains(AddAddressViewErrors.ERR_ZIP_INVALID), `is`(true))
	}

	@Test
	fun submitAddress_invalidPhone_returnsError() {
		val fname = "adg"
		val lname = "serdg"
		val code = "IN"
		val streetAdd = "sfhg45eyh"
		val streetAdd2 = ""
		val city = "sfhg"
		val state = "up"
		val zip = "309903"
		val phone = "9999988efg558"
		addEditAddressViewModel.submitAddress(
			code,
			fname,
			lname,
			streetAdd,
			streetAdd2,
			city,
			state,
			zip,
			phone
		)
		val result = addEditAddressViewModel.errorStatus.getOrAwaitValue()

		assertThat(result.size, `is`(greaterThan(0)))
		assertThat(result.contains(AddAddressViewErrors.ERR_PHONE_INVALID), `is`(true))
	}

	@Test
	fun submitAddress_validData_returnsNoError() {
		val fname = "adg"
		val lname = "serdg"
		val code = "IN"
		val streetAdd = "sfhg45eyh"
		val streetAdd2 = ""
		val city = "sfhg"
		val state = "up"
		val zip = "302203"
		val phone = "9879988558"
		addEditAddressViewModel.submitAddress(
			code,
			fname,
			lname,
			streetAdd,
			streetAdd2,
			city,
			state,
			zip,
			phone
		)
		val result = addEditAddressViewModel.errorStatus.getOrAwaitValue()
		assertThat(result.size, `is`(0))

		val resData = addEditAddressViewModel.newAddressData.getOrAwaitValue()
		assertThat(resData, `is`(notNullValue()))
	}
}