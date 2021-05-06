package com.vishalgaur.shoppingapp.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {
	private lateinit var homeViewModel: HomeViewModel

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun setUp() {
		homeViewModel = HomeViewModel(ApplicationProvider.getApplicationContext())
	}

//    @Test
//    fun setCategory_setsLiveData() {
//        homeViewModel.setCategory("Shoes")
//        val result = homeViewModel.selectedCategory.getOrAwaitValue()
//        assertThat(result, `is`("Shoes"))
//    }

//    @Test
//    fun submitProduct_noData_returnsEmptyError() {
//        val name = ""
//        val price = null
//        val desc = ""
//        val sizes = emptyList<Int>()
//        val colors = emptyList<String>()
//        val imgList = emptyList<Uri>()
//
//        homeViewModel.submitProduct(name, price, desc, sizes, colors, imgList)
//        val result = homeViewModel.errorStatus.getOrAwaitValue()
//
//        assertThat(result, `is`(AddProductViewErrors.EMPTY))
//    }

//    @Test
//    fun submitProduct_invalidPrice_returnsPriceError() {
//        val name = "vwsf"
//        val price = 0.0
//        val desc = "crw rewg"
//        val sizes = listOf(5,6)
//        val colors = listOf("red", "blue")
//        val imgList = listOf("ffsd".toUri(), "sws".toUri())
//
//        homeViewModel.submitProduct(name, price, desc, sizes, colors, imgList)
//        val result = homeViewModel.errorStatus.getOrAwaitValue()
//
//        assertThat(result, `is`(AddProductViewErrors.ERR_PRICE_0))
//    }

	//  current User has to be set to run the test
	//
//    @Test
//    fun submitProduct_allValid_returnsNoError() {
//        val name = "  vwsf 6hy  "
//        val price = 873.0
//        val desc = "crw rewg"
//        val sizes = listOf(5,6)
//        val colors = listOf("red", "blue")
//        val imgList = listOf("ffsd".toUri(), "sws".toUri())
//
//        homeViewModel.submitProduct(name, price, desc, sizes, colors, imgList)
//        val result = homeViewModel.errorStatus.getOrAwaitValue()
//        val resultPro = homeViewModel.productData.getOrAwaitValue()
//
//        assertThat(result, `is`(AddProductViewErrors.NONE))
//        assertThat(resultPro, `is`(notNullValue()))
//        assertThat(resultPro.name, `is`("vwsf 6hy"))
//    }
}