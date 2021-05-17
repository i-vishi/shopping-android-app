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
import com.vishalgaur.shoppingapp.data.source.FakeProductsRepository
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepoInterface
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class OrderViewModelTest {

	private lateinit var orderViewModel: OrderViewModel
	private lateinit var productsRepository: ProductsRepoInterface
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

	val item1 = UserData.CartItem(
		"item2123",
		"pro123",
		"owner23",
		1,
		"Red",
		10
	)

	val item2 = UserData.CartItem(
		"item2123456347",
		"pro12345",
		"owner23",
		1,
		"Blue",
		9
	)

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		productsRepository = FakeProductsRepository()
		val context = ApplicationProvider.getApplicationContext<ShoppingApplication>()
		val sessionManager = ShoppingAppSessionManager(context)
		authRepository = FakeAuthRepository(sessionManager)
		authRepository.login(user, true)
		ServiceLocator.productsRepository = productsRepository
		ServiceLocator.authRepository = authRepository

		orderViewModel = OrderViewModel(context)
	}

	@After
	fun cleanUp() = runBlockingTest {
		ServiceLocator.resetRepository()
	}

	@Test
	fun getCartItems_loadsData() = runBlocking {
		orderViewModel.getCartItems()
		delay(200)
		val result = orderViewModel.dataStatus.getOrAwaitValue()
		assertThat(result, `is`(StoreDataStatus.DONE))
	}

	@Test
	fun getAddresses_noAddress_loadsData() = runBlocking {
		orderViewModel.getUserAddresses()
		delay(200)
		val result = orderViewModel.dataStatus.getOrAwaitValue()
		assertThat(result, `is`(StoreDataStatus.DONE))
		val resAdd = orderViewModel.userAddresses.getOrAwaitValue()
		assertThat(resAdd.size, `is`(0))
	}

	@Test
	fun getAddresses_hasAddress_loadsData() = runBlocking {
		authRepository.insertAddress(address, user.userId)
		delay(100)
		orderViewModel.getUserAddresses()
		delay(100)
		val result = orderViewModel.dataStatus.getOrAwaitValue()
		assertThat(result, `is`(StoreDataStatus.DONE))
		val resAdd = orderViewModel.userAddresses.getOrAwaitValue()
		assertThat(resAdd.size, `is`(1))
	}

	@Test
	fun deleteAddress_deletesAddress() = runBlocking{
		authRepository.insertAddress(address, user.userId)
		delay(100)
		orderViewModel.getUserAddresses()
		delay(100)
		val resAdd = orderViewModel.userAddresses.getOrAwaitValue()
		assertThat(resAdd.size, `is`(1))

		orderViewModel.deleteAddress(address.addressId)
		delay(100)
		val resAdd2 = orderViewModel.userAddresses.getOrAwaitValue()
		assertThat(resAdd2.size, `is`(0))
	}

	@Test
	fun getItemsPriceTotal_returnsTotal() {
		runBlocking {
			authRepository.insertCartItemByUserId(item1, user.userId)
			delay(100)
			val result = orderViewModel.getItemsPriceTotal()
			assertThat(result, `is`(0.0))
		}
	}

	@Test
	fun toggleLike() {
		runBlocking {
			val res1 = orderViewModel.userLikes.getOrAwaitValue()
			orderViewModel.toggleLikeProduct("pro-if2r3")
			delay(100)
			val res2 = orderViewModel.userLikes.getOrAwaitValue()
			assertThat(res1.size, not(res2.size))
		}
	}

	@Test
	fun setQuantity_setsQuantity() = runBlocking {
		authRepository.insertCartItemByUserId(item1, user.userId)
		delay(100)
		val res1 = orderViewModel.cartItems.getOrAwaitValue().find { it.itemId == item1.itemId }
		val size1 = res1?.quantity ?: 0
		orderViewModel.setQuantityOfItem(item1.itemId, 1)
		delay(100)
		val res2 = orderViewModel.cartItems.getOrAwaitValue().find { it.itemId == item1.itemId }
		val size2 = res2?.quantity ?: 0

		assertThat(size1, `is`(1))
		assertThat(size2, `is`(2))
	}

	@Test
	fun deleteItemFromCart_deletesItem() = runBlocking {
		authRepository.insertCartItemByUserId(item1, user.userId)
		delay(100)
		val res1 = orderViewModel.cartItems.getOrAwaitValue().find { it.itemId == item1.itemId }
		assertThat(res1, `is`(notNullValue()))

		orderViewModel.deleteItemFromCart(item1.itemId)
		delay(100)
		val res2 = orderViewModel.cartItems.getOrAwaitValue().find { it.itemId == item1.itemId }
		assertThat(res2, `is`(nullValue()))
	}
}