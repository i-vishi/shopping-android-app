package com.vishalgaur.shoppingapp.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.source.ProductDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.Exception

class ProductsLocalDataSource internal constructor(
    private val productsDao: ProductsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductDataSource {
    override fun observeProducts(): LiveData<Result<List<Product>>?> {
        return Transformations.map(productsDao.observeProducts()) {
            Success(it)
        }
    }

    override suspend fun getAllProducts(): Result<List<Product>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(productsDao.getAllProducts())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun refreshProducts() {
        // refresh products
    }

    suspend fun getAllProductsByOwner(ownerId: String): LiveData<Result<List<Product>>> {
        return Transformations.map(productsDao.getProductsByOwnerId(ownerId)) {
            Success(it)
        }
    }

    override suspend fun getProductById(productId: String): Result<Product> = withContext(ioDispatcher) {
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

    suspend fun insertMultipleProducts(proList: List<Product>) =
        withContext(ioDispatcher) {
            productsDao.insertListOfProducts(proList)
        }

    suspend fun deleteProduct(productId: String): Unit = withContext(ioDispatcher) {
        productsDao.deleteProductById(productId)
    }

    suspend fun deleteAllProducts() = withContext(ioDispatcher) {
        productsDao.deleteAllProducts()
    }
}