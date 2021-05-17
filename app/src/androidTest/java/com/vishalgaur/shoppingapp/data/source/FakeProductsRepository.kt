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
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepoInterface
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.LinkedHashMap

class FakeProductsRepository : ProductsRepoInterface {

	var productsServiceData: LinkedHashMap<String, Product> = LinkedHashMap()
	private val imagesStorage = mutableListOf<String>()
	private val observableProducts = MutableLiveData<Result<List<Product>>>()

	override suspend fun refreshProducts(): StoreDataStatus {
		observableProducts.value = Success(productsServiceData.values.toList())
		return StoreDataStatus.DONE
	}

	override fun observeProducts(): LiveData<Result<List<Product>>?> {
		runBlocking { refreshProducts() }
		return observableProducts
	}

	override fun observeProductsByOwner(ownerId: String): LiveData<Result<List<Product>>?> {
		runBlocking { refreshProducts() }
		return Transformations.map(observableProducts) { products ->
			when (products) {
				is Result.Loading -> Result.Loading
				is Error -> Error(products.exception)
				is Success -> {
					val pros = products.data.filter { it.owner == ownerId }
					Success(pros)
				}
			}
		}
	}

	override suspend fun getAllProductsByOwner(ownerId: String): Result<List<Product>> {
		productsServiceData.values.let { pros ->
			val res = pros.filter { it.owner == ownerId }
			return Success(res)
		}
	}

	override suspend fun getProductById(productId: String, forceUpdate: Boolean): Result<Product> {
		productsServiceData[productId]?.let {
			return Success(it)
		}
		return Error(Exception("Product Not Found!"))
	}

	override suspend fun insertProduct(newProduct: Product): Result<Boolean> {
		productsServiceData[newProduct.productId] = newProduct
		return Success(true)
	}

	override suspend fun insertImages(imgList: List<Uri>): List<String> {
		val result = mutableListOf<String>()
		imgList.forEach { uri ->
			val uniId = UUID.randomUUID().toString()
			val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
			val res = uri.toString() + fileName
			imagesStorage.add(res)
			result.add(res)
		}
		return result
	}

	override suspend fun updateProduct(product: Product): Result<Boolean> {
		productsServiceData[product.productId] = product
		return Success(true)
	}

	override suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String> {
		val urlList = mutableListOf<String>()
		newList.forEach { uri ->
			if (!oldList.contains(uri.toString())) {
				val uniId = UUID.randomUUID().toString()
				val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
				val res = uri.toString() + fileName
				imagesStorage.add(res)
				urlList.add(res)
			} else {
				urlList.add(uri.toString())
			}
		}
		oldList.forEach { imgUrl ->
			if (!newList.contains(imgUrl.toUri())) {
				imagesStorage.remove(imgUrl)
			}
		}
		return urlList
	}

	override suspend fun deleteProductById(productId: String): Result<Boolean> {
		productsServiceData.remove(productId)
		refreshProducts()
		return Success(true)
	}
}