package com.vishalgaur.shoppingapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.local.ProductsDao
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserDao
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
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

	private lateinit var userDao: UserDao
	private lateinit var productsDao: ProductsDao
	private lateinit var appDb: ShoppingAppDatabase

	@get:Rule
	var instantTaskExecutorRule = InstantTaskExecutorRule()

	@Before
	fun createDb() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext

		appDb =
			Room.inMemoryDatabaseBuilder(context, ShoppingAppDatabase::class.java)
				.allowMainThreadQueries()
				.build()

		userDao = appDb.userDao()
		productsDao = appDb.productsDao()
	}

	@After
	fun closeDb() {
		appDb.clearAllTables()
		appDb.close()
	}

	@Test
	fun insertAndGetUser() {
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
		runBlocking {
			userDao.insert(user)
			val result = userDao.getById("sdjm43892yfh948ehod")
			assertThat(result?.userId, `is`(user.userId))
		}

	}

	@Test
	fun noData_returnsNull() {
		runBlocking {
			val result = userDao.getById("1232")
			assertThat(result, `is`(nullValue()))
		}
	}

	@Test
	fun insertClearUser_returnsNull() {
		val user = UserData(
			"sdjm43892yfh948ehod",
			"Vishal",
			"+919999988888",
			"vishal@somemail.com",
			"dh94328hd",
			emptyList(),
			emptyList(),
			emptyList()
		)
		runBlocking {
			userDao.insert(user)
			userDao.clear()
			val result = userDao.getById("sdjm43892yfh948ehod")
			assertThat(result, `is`(nullValue()))
		}
	}

	@Test
	fun insertAndGetProduct() {
		runBlocking {
			productsDao.insert(pro1)
			val result = productsDao.getProductById(pro1.productId)
			assertEquals(pro1, result)
		}
	}

	@Test
	fun insertClearProduct_returnsNull() = runBlocking {
		productsDao.insert(pro1)
		productsDao.deleteAllProducts()
		val result = productsDao.getAllProducts()
		assertEquals(0, result.size)
	}

	@Test
	fun deleteProductById() = runBlocking {
		productsDao.insert(pro2)
		productsDao.deleteProductById(pro2.productId)
		val result = productsDao.getProductById(pro2.productId)
		assertThat(result, `is`(nullValue()))
	}

	@Test
	fun noProducts_returnsEmptyList() = runBlocking {
		val result = productsDao.getAllProducts()
		assertThat(result.size, `is`(0))
	}

	@Test
	fun deleteAllProducts_returnsEmptyList() = runBlocking {
		productsDao.insert(pro2)
		productsDao.deleteAllProducts()
		val result = productsDao.getAllProducts()
		assertThat(result.size, `is`(0))
	}

	@Test
	fun getProductsByOwner_returnsData() = runBlocking {
		productsDao.insert(pro2)
		val result = productsDao.getProductsByOwnerId(pro2.owner)
		assertThat(result.size, `is`(1))
	}

	@Test
	fun insertMultipleProducts() = runBlocking {
		productsDao.insertListOfProducts(listOf(pro1, pro2))
		val result = productsDao.getAllProducts()
		assertThat(result.size, `is`(2))
	}

	@Test
	fun observeProducts_returnsLiveData() = runBlocking {
		val initialRes = productsDao.observeProducts()
		productsDao.insert(pro1)
		val newValue = productsDao.observeProducts().getOrAwaitValue()

		assertThat(initialRes.value, not(newValue))
		assertThat(initialRes.value, `is`(nullValue()))
		assertThat(newValue.size, `is`(1))
	}

	@Test
	fun observeProductsByOwner_returnsLiveData() = runBlocking {
		val initialRes = productsDao.observeProductsByOwner(pro1.owner)
		productsDao.insert(pro1)
		val newValue = productsDao.observeProductsByOwner(pro1.owner).getOrAwaitValue()

		assertThat(initialRes.value, not(newValue))
		assertThat(initialRes.value, `is`(nullValue()))
		assertThat(newValue.size, `is`(1))
	}
}