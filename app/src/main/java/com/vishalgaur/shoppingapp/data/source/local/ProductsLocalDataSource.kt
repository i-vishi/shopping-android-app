package com.vishalgaur.shoppingapp.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.source.ProductDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductsLocalDataSource internal constructor(
	private val productsDao: ProductsDao,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductDataSource {
	override fun observeProducts(): LiveData<Result<List<Product>>?> {
		return try {
			Transformations.map(productsDao.observeProducts()) {
				Success(it)
			}
		} catch (e: Exception) {
			Transformations.map(MutableLiveData(e)) {
				Error(e)
			}
		}
	}

	override fun observeProductsByOwner(ownerId: String): LiveData<Result<List<Product>>?> {
		return try {
			Transformations.map(productsDao.observeProductsByOwner(ownerId)) {
				Success(it)
			}
		} catch (e: Exception) {
			Transformations.map(MutableLiveData(e)) {
				Error(e)
			}
		}
	}

	override suspend fun getAllProducts(): Result<List<Product>> = withContext(ioDispatcher) {
		return@withContext try {
			Success(productsDao.getAllProducts())
		} catch (e: Exception) {
			Error(e)
		}
	}

	override suspend fun getAllProductsByOwner(ownerId: String): Result<List<Product>> =
		withContext(ioDispatcher) {
			return@withContext try {
				Success(productsDao.getProductsByOwnerId(ownerId))
			} catch (e: Exception) {
				Error(e)
			}
		}

	override suspend fun getProductById(productId: String): Result<Product> =
		withContext(ioDispatcher) {
			try {
				val product = productsDao.getProductById(productId)
				if (product != null) {
					return@withContext Success(product)
				} else {
					return@withContext Error(Exception("Product Not Found!"))
				}
			} catch (e: Exception) {
				return@withContext Error(e)
			}
		}

	override suspend fun insertProduct(newProduct: Product) = withContext(ioDispatcher) {
		productsDao.insert(newProduct)
	}

	override suspend fun updateProduct(proData: Product) = withContext(ioDispatcher) {
		productsDao.insert(proData)
	}

	override suspend fun insertMultipleProducts(data: List<Product>) = withContext(ioDispatcher) {
		productsDao.insertListOfProducts(data)
	}

	override suspend fun deleteProduct(productId: String): Unit = withContext(ioDispatcher) {
		productsDao.deleteProductById(productId)
	}

	override suspend fun deleteAllProducts() = withContext(ioDispatcher) {
		productsDao.deleteAllProducts()
	}
}