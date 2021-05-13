package com.vishalgaur.shoppingapp.viewModels

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.getOrAwaitValue
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditProductViewModelTest {
	private lateinit var addEditProductViewModel: AddEditProductViewModel
	private lateinit var sessionManager: ShoppingAppSessionManager

	private val userSeller = UserData(
		"user1234selller",
		"Some Name",
		"+919999988888",
		"somemail123seller@somemail.com",
		"1234",
		emptyList(),
		emptyList(),
		emptyList(),
		"SELLER"
	)

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())
		sessionManager.createLoginSession(
			userSeller.userId,
			userSeller.name,
			userSeller.mobile,
			false,
			true
		)
		addEditProductViewModel =
			AddEditProductViewModel(ApplicationProvider.getApplicationContext())
	}

	@Test
	fun setCategory_Shoes() {
		addEditProductViewModel.setCategory("Shoes")
		val result = addEditProductViewModel.selectedCategory.getOrAwaitValue()
		assertThat(result, `is`("Shoes"))
	}

	@Test
	fun setIsEdit_true() {
		addEditProductViewModel.setIsEdit(true)
		val result = addEditProductViewModel.isEdit.getOrAwaitValue()
		assertThat(result, `is`(true))
	}

	@Test
	fun setIsEdit_false() {
		addEditProductViewModel.setIsEdit(false)
		val result = addEditProductViewModel.isEdit.getOrAwaitValue()
		assertThat(result, `is`(false))
	}


	@Test
	fun submitProduct_noData_returnsEmptyError() {
		addEditProductViewModel.setIsEdit(false)
		val name = ""
		val price = null
		val mrp = null
		val desc = ""
		val sizes = emptyList<Int>()
		val colors = emptyList<String>()
		val imgList = emptyList<Uri>()

		addEditProductViewModel.submitProduct(name, price, mrp, desc, sizes, colors, imgList)
		val result = addEditProductViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(AddProductViewErrors.EMPTY))
	}

	@Test
	fun submitProduct_invalidPrice_returnsPriceError() {
		addEditProductViewModel.setIsEdit(false)
		val name = "vwsf"
		val mrp = 125.0
		val price = 0.0
		val desc = "crw rewg"
		val sizes = listOf(5, 6)
		val colors = listOf("red", "blue")
		val imgList = listOf("ffsd".toUri(), "sws".toUri())

		addEditProductViewModel.submitProduct(name, price, mrp, desc, sizes, colors, imgList)
		val result = addEditProductViewModel.errorStatus.getOrAwaitValue()

		assertThat(result, `is`(AddProductViewErrors.ERR_PRICE_0))
	}

	@Test
	fun submitProduct_allValid_returnsNoError() {
		addEditProductViewModel.setIsEdit(false)
		addEditProductViewModel.setCategory("Shoes")
		val name = "vwsf"
		val mrp = 125.0
		val price = 100.0
		val desc = "crw rewg"
		val sizes = listOf(5, 6)
		val colors = listOf("red", "blue")
		val imgList = listOf("ffsd".toUri(), "sws".toUri())

		addEditProductViewModel.submitProduct(name, price, mrp, desc, sizes, colors, imgList)
		val result = addEditProductViewModel.errorStatus.getOrAwaitValue()
		val resultPro = addEditProductViewModel.newProductData.getOrAwaitValue()

		assertThat(result, `is`(AddProductViewErrors.NONE))
		assertThat(resultPro, `is`(notNullValue()))
		assertThat(resultPro.name, `is`("vwsf"))
	}
}