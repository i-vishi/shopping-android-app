package com.vishalgaur.shoppingapp.data.source

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success

class FakeProductsDataSource(private var products: MutableList<Product>? = mutableListOf()) :
	ProductDataSource {
	private val imagesStorage = mutableListOf<String>()
	override fun observeProducts(): LiveData<Result<List<Product>>?> {
		products?.let { pros ->
			val res = MutableLiveData(pros)
			return Transformations.map(res) {
				Success(it.toList())
			}
		}
		return MutableLiveData(Error(Exception()))
	}

	override fun observeProductsByOwner(ownerId: String): LiveData<Result<List<Product>>?> {
		products?.let { allPros ->
			val pros = allPros.filter { pr -> pr.owner == ownerId }
			val res = MutableLiveData(pros)
			return Transformations.map(res) {
				Success(it.toList())
			}
		}
		return MutableLiveData(Error(Exception()))
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
			return if (res.isNotEmpty()) {
				Success(res[0])
			} else {
				Error(Exception("Product Not Found"))
			}
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
		products?.let {
			val pos = it.indexOfFirst { product -> productId == product.productId }
			if (pos >= 0)
				it.removeAt(pos)
			else throw Exception("Product Not Found")
		}
	}

	override suspend fun getAllProductsByOwner(ownerId: String): Result<List<Product>> {
		val res = products?.filter { product ->
			product.owner == ownerId
		}
		return if (res != null) {
			Success(res)
		} else {
			Success(emptyList())
		}
	}

	override suspend fun deleteAllProducts() {
		products = mutableListOf()
	}

	override suspend fun insertMultipleProducts(data: List<Product>) {
		products?.addAll(data)
	}

	override suspend fun uploadImage(uri: Uri, fileName: String): Uri {
		val res = uri.toString() + fileName
		if (res.contains("invalidinvalidinvalid")) {
			throw Exception("Error uploading Images")
		}
		imagesStorage.add(res)
		return res.toUri()
	}

	override fun revertUpload(fileName: String) {
		val pos = imagesStorage.indexOfFirst { imageRef ->
			imageRef.contains(fileName)
		}
		if (pos >= 0) {
			imagesStorage.removeAt(pos)
		}
	}

	override fun deleteImage(imgUrl: String) {
		imagesStorage.remove(imgUrl)
	}
}