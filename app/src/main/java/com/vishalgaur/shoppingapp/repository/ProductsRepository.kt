package com.vishalgaur.shoppingapp.repository

import android.app.Application
import android.util.Log
import com.vishalgaur.shoppingapp.database.ShoppingAppDb
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.network.FirebaseDbUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ProductsRepository"

class ProductsRepository(private val application: Application) {
	private var appDb: ShoppingAppDb = ShoppingAppDb.getInstance(application)

	private var firebaseDb = FirebaseDbUtils()

	suspend fun refreshData() {

	}

	suspend fun insertProduct(newProduct: Product) {
		withContext(Dispatchers.IO) {
			Log.d(TAG, "adding product with id: ${newProduct.productId} in Room")
			appDb.productsDao().insert(newProduct)
			Log.d(TAG, "adding product with id: ${newProduct.productId} on firebase")
			firebaseDb.addProduct(newProduct.toHashMap())
					.addOnSuccessListener {
						Log.d(TAG, "Product Added")
					}.addOnFailureListener { e ->
						Log.d(TAG, "error adding product: $e")
					}
		}
	}

	fun getAllProducts() = appDb.productsDao().getAllProducts()

	fun getAllProductsByOwner(owner: String) = appDb.productsDao().getProductsByOwnerId(owner)
}