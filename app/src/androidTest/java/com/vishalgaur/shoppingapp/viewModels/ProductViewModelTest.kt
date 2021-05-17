package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.ServiceLocator
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.FakeAuthRepository
import com.vishalgaur.shoppingapp.data.source.FakeProductsRepository
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepoInterface
import com.vishalgaur.shoppingapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProductViewModelTest {
	private lateinit var productViewModel: ProductViewModel
	private lateinit var productId: String
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

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		productsRepository = FakeProductsRepository()
		val sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())
		authRepository = FakeAuthRepository(sessionManager)
		authRepository.login(user, true)
		ServiceLocator.productsRepository = productsRepository
		ServiceLocator.authRepository = authRepository
		productId = "pro-shoes-wofwopjf-1"
		productViewModel = ProductViewModel(productId, ApplicationProvider.getApplicationContext())
	}

	@After
	fun cleanUp() = runBlockingTest {
		ServiceLocator.resetRepository()
	}

	@Test
	fun toggleLikeProduct_false_true() {
		val result1 = productViewModel.isLiked.value
		runBlocking {
			productViewModel.toggleLikeProduct()
			delay(1000)
			val result2 = productViewModel.isLiked.getOrAwaitValue()
			assertThat(result1, not(result2))
		}

	}
}