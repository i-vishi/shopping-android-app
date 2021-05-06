package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.getOrAwaitValue
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductViewModelTest {
	private lateinit var productViewModel: ProductViewModel
	private lateinit var productId: String

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		productId = "pro-shoes-wofwopjf-1"
		productViewModel = ProductViewModel(productId, ApplicationProvider.getApplicationContext())
	}

	@Test
	fun toggleLikeProduct_false_true() {
		productViewModel.toggleLikeProduct()
		val result = productViewModel.isLiked.getOrAwaitValue()

		assertThat(result, `is`(true))
	}

	@Test
	fun toggleLikeProduct_true_false() {
		productViewModel.toggleLikeProduct()
		productViewModel.toggleLikeProduct()
		val result = productViewModel.isLiked.getOrAwaitValue()

		assertThat(result, `is`(false))
	}
}