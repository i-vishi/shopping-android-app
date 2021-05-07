package com.vishalgaur.shoppingapp.data.source

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result

interface ProductDataSource {

	fun observeProducts(): LiveData<Result<List<Product>>?>

	suspend fun getAllProducts(): Result<List<Product>>

	suspend fun refreshProducts() {}

	suspend fun getProductById(productId: String): Result<Product>

	suspend fun insertProduct(newProduct: Product)

	suspend fun updateProduct(proData: Product)

	fun observeProductsByOwner(ownerId: String): LiveData<Result<List<Product>>?> {
		return MutableLiveData()
	}

	suspend fun getAllProductsByOwner(ownerId: String): Result<List<Product>> {
		return Result.Success(emptyList())
	}

	suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
		return null
	}

	fun revertUpload(fileName: String) {}
	fun deleteImage(imgUrl: String) {}
	suspend fun deleteProduct(productId: String)
	suspend fun deleteAllProducts() {}
	suspend fun insertMultipleProducts(data: List<Product>) {}
}