package com.vishalgaur.shoppingapp.data.source

import androidx.lifecycle.LiveData
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result

interface ProductDataSource {

	fun observeProducts(): LiveData<Result<List<Product>>?>

	suspend fun getAllProducts(): Result<List<Product>>

	suspend fun refreshProducts()

	suspend fun getProductById(productId: String): Result<Product?>

	suspend fun insertProduct(newProduct: Product)

	suspend fun updateProduct(proData: Product)
}