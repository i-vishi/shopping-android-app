package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.view.isVisible
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.RecyclerViewItemAction
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
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class HomeFragmentTest {
	private lateinit var homeScenario: FragmentScenario<HomeFragment>
	private lateinit var navController: NavController
	private lateinit var sessionManager: ShoppingAppSessionManager
	private lateinit var productsRepository: ProductsRepoInterface
	private lateinit var authRepository: AuthRepoInterface
	private val context = ApplicationProvider.getApplicationContext<ShoppingApplication>()

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
	private val pro2 = Product(
		"pro-owner1-slipper-101",
		"Slipper Name 101",
		"owner1",
		"some description",
		"Slippers",
		50.0,
		80.0,
		listOf(6, 7, 8),
		listOf("Black", "Blue"),
		listOf(
			"http://image-ref-uri/-slipper-101-01.jpg",
			"http://image-ref-uri/-slipper-101-02.jpg"
		),
		4.0
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

	@Test
	fun userCustomer_hideFABandEditDeleteButtons() = runBlockingTest {
		insertProducts()
		authRepository.login(userCustomer, true)
		ServiceLocator.authRepository = authRepository
		setScenarioAndNav()

		onView(withId(R.id.home_fab_add_product)).check(matches(withEffectiveVisibility(Visibility.GONE)))

		//testing recyclerview items
		onView(withId(R.id.products_recycler_view))
			.perform(
				RecyclerViewActions.actionOnItemAtPosition<ProductAdapter.ViewHolder>(
					0,
					object : RecyclerViewItemAction() {
						override fun perform(uiController: UiController?, view: View) {
							val editButton: ImageView = view.findViewById(R.id.product_edit_button)
							val deleteButton: ImageView =
								view.findViewById(R.id.product_delete_button)
							assertThat(editButton.isVisible, `is`(false))
							assertThat(deleteButton.isVisible, `is`(false))
						}
					})
			)
	}

	@Test
	fun userSeller_showFABandEditDeleteButtons() = runBlockingTest {
		insertProducts()
		authRepository.login(userSeller, true)
		ServiceLocator.authRepository = authRepository
		setScenarioAndNav()

		onView(withId(R.id.home_fab_add_product)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
		//testing recyclerview items
		onView(withId(R.id.products_recycler_view))
			.perform(
				RecyclerViewActions.actionOnItemAtPosition<ProductAdapter.ViewHolder>(
					0,
					object : RecyclerViewItemAction() {
						override fun perform(uiController: UiController?, view: View) {
							val editButton: ImageView = view.findViewById(R.id.product_edit_button)
							val deleteButton: ImageView =
								view.findViewById(R.id.product_delete_button)
							assertThat(editButton.isVisible, `is`(true))
							assertThat(deleteButton.isVisible, `is`(true))
						}
					})
			)
	}

	private fun setScenarioAndNav() {
		homeScenario = launchFragmentInContainer(Bundle(), R.style.Theme_ShoppingApp)
		navController = TestNavHostController(context)
		runOnUiThread {
			navController.setGraph(R.navigation.home_nav_graph)
			homeScenario.onFragment {
				Navigation.setViewNavController(it.requireView(), navController)
			}
		}
	}

	private suspend fun insertProducts() {
		productsRepository.insertProduct(pro1)
		productsRepository.insertProduct(pro2)
	}
}