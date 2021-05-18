package com.vishalgaur.shoppingapp.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.ServiceLocator
import com.vishalgaur.shoppingapp.ShoppingApplication
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.FakeAuthRepository
import com.vishalgaur.shoppingapp.data.source.FakeProductsRepository
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepoInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProductDetailsFragmentTest {
	private lateinit var productDetailScenario: FragmentScenario<ProductDetailsFragment>
	private lateinit var navController: NavController
	private lateinit var sessionManager: ShoppingAppSessionManager
	private lateinit var productsRepository: ProductsRepoInterface
	private lateinit var authRepository: AuthRepoInterface
	private val context = ApplicationProvider.getApplicationContext<ShoppingApplication>()

	private val pro1 = Product(
		"pro-owner1-shoe-101",
		"Shoe Name 101",
		"user1234selller",
		"some description",
		"Shoes",
		250.0,
		300.0,
		listOf(5, 6, 7, 8),
		listOf("Red", "Blue"),
		listOf("http://image-ref-uri/shoe-101-01.jpg", "http://image-ref-uri/-shoe-101-02.jpg"),
		2.5
	)

	private val userCustomer = UserData(
		"sdjm43892yfh948ehod",
		"Vishal",
		"+919999988888",
		"vishal@somemail.com",
		"dh94328hd",
		ArrayList(),
		ArrayList(),
		ArrayList(),
		"CUSTOMER"
	)
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
		sessionManager = ShoppingAppSessionManager(context)
		authRepository = FakeAuthRepository(sessionManager)
		productsRepository = FakeProductsRepository()
		ServiceLocator.productsRepository = productsRepository
	}

	@After
	fun cleanUp() = runBlockingTest {
		authRepository.signOut()
		ServiceLocator.resetRepository()
	}

	private suspend fun insertProducts() {
		productsRepository.insertProduct(pro1)
	}
}
