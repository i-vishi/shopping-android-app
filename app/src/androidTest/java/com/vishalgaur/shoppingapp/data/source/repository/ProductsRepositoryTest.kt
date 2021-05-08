package com.vishalgaur.shoppingapp.data.source.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toUri
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.vishalgaur.shoppingapp.ERR_UPLOAD
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.source.FakeProductsDataSource
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProductsRepositoryTest {
	private val pro1 = Product(
		"pro-owner1-shoe-101",
		"Shoe Name 101",
		"owner1",
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
	private val pro3 = Product(
		"pro-owner1-shoe-102",
		"Shoe Name 102",
		"owner2",
		"some description",
		"Shoes",
		450.0,
		600.0,
		listOf(4, 5, 7, 8, 10),
		listOf("Red", "Blue", "White"),
		listOf("http://image-ref-uri/-shoe-102-01.jpg", "http://image-ref-uri/-shoe-102-02.jpg"),
		3.5
	)

	private lateinit var productsLocalDataSource: FakeProductsDataSource
	private lateinit var productsRemoteDataSource: FakeProductsDataSource

	// class under test
	private lateinit var productsRepository: ProductsRepository

	@get:Rule
	val instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun createRepository() {
		productsLocalDataSource = FakeProductsDataSource(mutableListOf())
		productsRemoteDataSource = FakeProductsDataSource(mutableListOf(pro1, pro3))

		productsRepository = ProductsRepository(productsRemoteDataSource, productsLocalDataSource)
	}

	@Test
	fun getProductsById_invalidId_returnsError() = runBlockingTest {
		val resultRes = productsRepository.getProductById("invalidId", false)
		if (resultRes is Result.Success)
			assert(false)
		else if (resultRes is Result.Error) {
			assertEquals(resultRes.exception.message, "Product Not Found")
		}
	}

	@Test
	fun getProductsById_validId_returnsProduct() = runBlockingTest {
		productsRepository.insertProduct(pro1)
		val resultRes = productsRepository.getProductById(pro1.productId, false)
		if (resultRes is Result.Success) {
			assertThat(resultRes.data, `is`(pro1))
		} else if (resultRes is Result.Error) {
			assert(false)
		}
	}

	@Test
	fun insertProduct_returnsSuccess() = runBlockingTest {
		val insertRes = productsRepository.insertProduct(pro1)
		if (insertRes is Result.Success) {
			assertThat(insertRes.data, `is`(true))
		} else {
			assert(false)
		}
	}

	@Test
	fun insertImages_returnsSuccess() = runBlockingTest {
		val result = productsRepository.insertImages(pro1.images.map { it.toUri() })
		assertThat(result.size, `is`(pro1.images.size))
	}

	@Test
	fun insertImages_invalidImages_returnsError() = runBlockingTest {
		val result =
			productsRepository.insertImages(listOf("http://image-ref-uri/dwoeiovnwi-invalidinvalidinvalid/weoifhowf".toUri()))
		assertThat(result[0], `is`(ERR_UPLOAD))
	}

	@Test
	fun updateProduct_returnsSuccess() = runBlockingTest {
		productsRepository.insertProduct(pro2)
		val updatedPro = pro2
		updatedPro.availableSizes = listOf(5, 6, 10, 12)
		val insertRes = productsRepository.updateProduct(updatedPro)
		if (insertRes is Result.Success) {
			assertThat(insertRes.data, `is`(true))
		} else {
			assert(false)
		}
	}

	@Test
	fun updateImages_returnsList() = runBlockingTest {
		val oldList = productsRepository.insertImages(pro1.images.map { it.toUri() })
		val result = productsRepository.updateImages(pro3.images.map { it.toUri() }, oldList)
		assertThat(result.size, `is`(pro3.images.size))
	}

	@Test
	fun updateImages_invalidImage_returnsError() = runBlockingTest {
		val oldList = productsRepository.insertImages(pro1.images.map { it.toUri() })
		val newList = oldList.toMutableList()
		newList[0] = "http://csifduoskjgn/invalidinvalidinvalid/wehoiw"
		val result = productsRepository.updateImages(newList.map { it.toUri() }, oldList)
		assertThat(result[0], `is`(ERR_UPLOAD))
	}

	@Test
	fun deleteProductById_returnsSuccess() = runBlockingTest {
		productsRepository.insertProduct(pro1)
		productsRepository.insertProduct(pro2)
		val result = productsRepository.deleteProductById(pro1.productId)
		assert(result is Result.Success)
	}

	@Test
	fun deleteProductById_invalidId_returnsError() = runBlockingTest {
		productsRepository.insertProduct(pro1)
		productsRepository.insertProduct(pro2)
		val result = productsRepository.deleteProductById(pro3.productId)
		assert(result is Result.Error)
	}

	@Test
	fun refreshProducts_returnsSuccess() = runBlockingTest {
		val result = productsRepository.refreshProducts()
		assertThat(result, `is`(StoreDataStatus.DONE))
	}

	@Test
	fun observeProducts_noData_returnsNoData() = runBlockingTest {
		productsLocalDataSource.deleteAllProducts()
		val result = productsRepository.observeProducts().getOrAwaitValue()
		if (result is Result.Success) {
			assertThat(result.data.size, `is`(0))
		} else {
			assert(false)
		}
	}

	@Test
	fun observeProducts_hasData_returnsSuccessWithData() = runBlockingTest {
		val initialValue = productsRepository.observeProducts().getOrAwaitValue()

		val insertRes = async { productsRepository.insertProduct(pro3) }
		insertRes.await()
		val refreshRes = async { productsRepository.refreshProducts() }
		assertThat(refreshRes.await(), `is`(StoreDataStatus.DONE))

		val newValue = productsRepository.observeProducts().getOrAwaitValue()

		assertNotEquals(initialValue.toString(), newValue.toString())
		if (initialValue is Result.Success) {
			assertThat(initialValue.data.size, `is`(0))
		} else {
			assert(false)
		}
		if (newValue is Result.Success) {
			assertThat(newValue.data.size, `is`(greaterThan(0)))
		} else {
			assert(false)
		}
	}

	@Test
	fun observeProductsByOwner_noData_returnsNoData() = runBlockingTest {
		productsLocalDataSource.deleteAllProducts()
		val result = productsRepository.observeProductsByOwner(pro1.owner).getOrAwaitValue()
		if (result is Result.Success) {
			assertThat(result.data.size, `is`(0))
		} else {
			assert(false)
		}
	}

	@Test
	fun observeProductsByOwner_hasData_returnsSuccessWithData() = runBlockingTest {
		val initialValue = productsRepository.observeProductsByOwner(pro3.owner).getOrAwaitValue()

		val insertRes = async { productsRepository.insertProduct(pro3) }
		insertRes.await()
		val refreshRes = async { productsRepository.refreshProducts() }
		assertThat(refreshRes.await(), `is`(StoreDataStatus.DONE))

		val newValue = productsRepository.observeProductsByOwner(pro3.owner).getOrAwaitValue()

		assertNotEquals(initialValue.toString(), newValue.toString())
		if (initialValue is Result.Success) {
			assertThat(initialValue.data.size, `is`(0))
		} else {
			assert(false)
		}
		if (newValue is Result.Success) {
			assertThat(newValue.data.size, `is`(greaterThan(0)))
		} else {
			assert(false)
		}
	}

	@Test
	fun getAllProductsByOwner_noData_returnsNoData() = runBlockingTest {
		productsLocalDataSource.deleteAllProducts()
		val result = productsRepository.getAllProductsByOwner(pro1.owner)
		if (result is Result.Success) {
			assertThat(result.data.size, `is`(0))
		} else {
			assert(false)
		}
	}

	@Test
	fun getAllProductsByOwner_hasData_returnsData() = runBlockingTest {
		productsRepository.refreshProducts()
		val result = productsRepository.getAllProductsByOwner(pro1.owner)
		if (result is Result.Success) {
			assertThat(result.data.size, `is`(greaterThan(0)))
		} else {
			assert(false)
		}
	}

}