package com.vishalgaur.shoppingapp.data.source.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.vishalgaur.shoppingapp.ERR_UPLOAD
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.source.ProductDataSource
import com.vishalgaur.shoppingapp.data.source.local.ProductsLocalDataSource
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.remote.ProductsRemoteDataSource
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.util.*

class ProductsRepository(
	private val productsRemoteSource: ProductDataSource,
	private val productsLocalSource: ProductDataSource
) {

	companion object {
		private const val TAG = "ProductsRepository"

		@Volatile
		private var INSTANCE: ProductsRepository? = null

		fun getRepository(app: Application): ProductsRepository {
			return INSTANCE ?: synchronized(this) {
				val database = ShoppingAppDatabase.getInstance(app)
				ProductsRepository(
					ProductsRemoteDataSource(),
					ProductsLocalDataSource(database.productsDao())
				).also {
					INSTANCE = it
				}
			}
		}
	}

	suspend fun refreshProducts(): StoreDataStatus? {
		Log.d(TAG, "Updating Products in Room")
		return updateProductsFromRemoteSource()
	}

	fun observeProducts(): LiveData<Result<List<Product>>?> {
		return productsLocalSource.observeProducts()
	}

	fun observeProductsByOwner(ownerId: String): LiveData<Result<List<Product>>?> {
		return productsLocalSource.observeProductsByOwner(ownerId)
	}

	suspend fun getAllProductsByOwner(ownerId: String): Result<List<Product>> {
		return productsLocalSource.getAllProductsByOwner(ownerId)
	}

	suspend fun getProductById(productId: String, forceUpdate: Boolean = false): Result<Product> {
		if (forceUpdate) {
			updateProductFromRemoteSource(productId)
		}
		return productsLocalSource.getProductById(productId)
	}

	suspend fun insertProduct(newProduct: Product): Result<Boolean> {
		return supervisorScope {
			val localRes = async {
				Log.d(TAG, "onInsertProduct: adding product to local source")
				productsLocalSource.insertProduct(newProduct)
			}
			val remoteRes = async {
				Log.d(TAG, "onInsertProduct: adding product to remote source")
				productsRemoteSource.insertProduct(newProduct)
			}
			try {
				localRes.await()
				remoteRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun insertImages(imgList: List<Uri>): List<String> {
		var urlList = mutableListOf<String>()
		imgList.forEach label@{ uri ->
			val uniId = UUID.randomUUID().toString()
			val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
			try {
				val downloadUrl = productsRemoteSource.uploadImage(uri, fileName)
				urlList.add(downloadUrl.toString())
			} catch (e: Exception) {
				productsRemoteSource.revertUpload(fileName)
				Log.d(TAG, "exception: message = $e")
				urlList = mutableListOf()
				urlList.add(ERR_UPLOAD)
				return@label
			}
		}
		return urlList
	}

	suspend fun updateProduct(product: Product): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onUpdate: updating product in remote source")
				productsRemoteSource.updateProduct(product)
			}
			val localRes = async {
				Log.d(TAG, "onUpdate: updating product in local source")
				productsLocalSource.insertProduct(product)
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String> {
		var urlList = mutableListOf<String>()
		newList.forEach label@{ uri ->
			if (!oldList.contains(uri.toString())) {
				val uniId = UUID.randomUUID().toString()
				val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
				try {
					val downloadUrl = productsRemoteSource.uploadImage(uri, fileName)
					urlList.add(downloadUrl.toString())
				} catch (e: Exception) {
					productsRemoteSource.revertUpload(fileName)
					Log.d(TAG, "exception: message = $e")
					urlList = mutableListOf()
					urlList.add(ERR_UPLOAD)
					return@label
				}
			} else {
				urlList.add(uri.toString())
			}
		}
		oldList.forEach { imgUrl ->
			if (!newList.contains(imgUrl.toUri())) {
				productsRemoteSource.deleteImage(imgUrl)
			}
		}
		return urlList
	}

	suspend fun deleteProductById(productId: String): Result<Boolean> {
		return supervisorScope {
			val remoteRes = async {
				Log.d(TAG, "onDelete: deleting product from remote source")
				productsRemoteSource.deleteProduct(productId)
			}
			val localRes = async {
				Log.d(TAG, "onDelete: deleting product from local source")
				productsLocalSource.deleteProduct(productId)
			}
			try {
				remoteRes.await()
				localRes.await()
				Success(true)
			} catch (e: Exception) {
				Error(e)
			}
		}
	}

	private suspend fun updateProductsFromRemoteSource(): StoreDataStatus? {
		var res: StoreDataStatus? = null
		try {
			val remoteProducts = productsRemoteSource.getAllProducts()
			if (remoteProducts is Success) {
				Log.d(TAG, "pro list = ${remoteProducts.data}")
				productsLocalSource.deleteAllProducts()
				productsLocalSource.insertMultipleProducts(remoteProducts.data)
				res = StoreDataStatus.DONE
			} else {
				res = StoreDataStatus.ERROR
				if (remoteProducts is Error)
					throw remoteProducts.exception
			}
		} catch (e: Exception) {
			Log.d(TAG, "onUpdateProductsFromRemoteSource: Exception occurred, ${e.message}")
		}

		return res
	}

	private suspend fun updateProductFromRemoteSource(productId: String): StoreDataStatus? {
		var res: StoreDataStatus? = null
		try {
			val remoteProduct = productsRemoteSource.getProductById(productId)
			if (remoteProduct is Success && remoteProduct.data != null) {
				productsLocalSource.insertProduct(remoteProduct.data)
				res = StoreDataStatus.DONE
			} else {
				res = StoreDataStatus.ERROR
				if (remoteProduct is Error)
					throw remoteProduct.exception
			}
		} catch (e: Exception) {
			Log.d(TAG, "onUpdateProductFromRemoteSource: Exception occurred, ${e.message}")
		}
		return res
	}
}