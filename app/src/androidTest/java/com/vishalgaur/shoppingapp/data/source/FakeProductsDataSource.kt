package com.vishalgaur.shoppingapp.data.source

import androidx.lifecycle.LiveData
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*

class FakeProductsDataSource(private var products: MutableList<Product>? = mutableListOf()): ProductDataSource {
	override fun observeProducts(): LiveData<Result<List<Product>>?> {
		TODO("Not yet implemented")
	}

	override suspend fun getAllProducts(): Result<List<Product>> {
		products?.let {
			return Success(it)
		}
		return Error(Exception("Products Not Found"))
	}

	override suspend fun refreshProducts() {
		// No implementation
	}

	override suspend fun getProductById(productId: String): Result<Product> {
		products?.let {
			val res = it.filter { product -> product.productId == productId }
			return Success(res[0])
		}
		return Error(Exception("Product Not Found"))
	}

	override suspend fun insertProduct(newProduct: Product) {
		products?.add(newProduct)
	}

	override suspend fun updateProduct(proData: Product) {
		products?.let {
			val pos = it.indexOfFirst { product -> proData.productId == product.productId }
			it[pos] = proData
		}
	}

	override suspend fun deleteProduct(productId: String) {
		TODO("Not yet implemented")
	}
}