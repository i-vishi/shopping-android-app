package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getOrAwaitValue
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {
	private lateinit var homeViewModel: HomeViewModel
	private lateinit var authRepository: AuthRepository

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		homeViewModel = HomeViewModel(ApplicationProvider.getApplicationContext())
	}

    @Test
    fun setDataLoaded_setsValue() {
        homeViewModel.setDataLoaded()
        val result = homeViewModel.storeDataStatus.getOrAwaitValue()
        assertThat(result, `is`(StoreDataStatus.DONE))
    }

	@Test
	fun filterProducts_All() {
		homeViewModel.filterProducts("All")
		val result =  homeViewModel.filterCategory.getOrAwaitValue()
		assertThat(result, `is`("All"))
	}

	@Test
	fun filterProducts_Shoes() {
		homeViewModel.filterProducts("Shoes")
		val result =  homeViewModel.filterCategory.getOrAwaitValue()
		assertThat(result, `is`("Shoes"))
	}


}