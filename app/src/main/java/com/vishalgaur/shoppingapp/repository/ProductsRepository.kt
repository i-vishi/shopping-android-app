package com.vishalgaur.shoppingapp.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import com.vishalgaur.shoppingapp.database.ShoppingAppDb
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.network.AddProductErrors
import com.vishalgaur.shoppingapp.network.FirebaseDbUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "ProductsRepository"

class ProductsRepository(application: Application) {
    private var appDb: ShoppingAppDb = ShoppingAppDb.getInstance(application)

    private var firebaseDb = FirebaseDbUtils()

    suspend fun refreshData() {
        Log.d(TAG, "refreshing products")
        insertAllProductsToRoom()
    }

    suspend fun insertAllProductsToRoom() {
        withContext(Dispatchers.IO) {
            val res = firebaseDb.getAllProducts().await().toObjects(Product::class.java)
            Log.d(TAG, "Adding all products to Room")
            appDb.productsDao().insertListOfProducts(res)
        }
    }

    suspend fun insertProduct(newProduct: Product): AddProductErrors {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "adding product with id: ${newProduct.productId} in Room")
            appDb.productsDao().insert(newProduct)
        }
        Log.d(TAG, "adding product with id: ${newProduct.productId} on firebase")
        val res = firebaseDb.addProduct(newProduct.toHashMap()).await()

        return if (res != null) {
            Log.d(TAG, "Product Added to firestore")
            AddProductErrors.NONE
        } else {
            Log.d(TAG, "error adding product to firebase ")
            AddProductErrors.ERR_ADD

        }
    }

    suspend fun insertImages(imgList: List<Uri>) : List<String> {
        val urlList = mutableListOf<String>()
        imgList.forEach { uri ->
            val fileName = uri.lastPathSegment?.split("/")?.last()
            if (fileName != null) {
                val downloadUrl = firebaseDb.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            }
        }
        return urlList
    }

    fun getAllProducts() = appDb.productsDao().getAllProducts()

    fun getProductById(productId: String) = appDb.productsDao().getProductById(productId)

    fun getAllProductsByOwner(owner: String) = appDb.productsDao().getProductsByOwnerId(owner)
}