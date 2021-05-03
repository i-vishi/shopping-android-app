package com.vishalgaur.shoppingapp.data.source

import androidx.lifecycle.LiveData
import com.vishalgaur.shoppingapp.data.Product

interface ProductDataSource {

    fun observeProducts() : LiveData<List<Product>>

    suspend fun getAllProducts(): List<Product>

    suspend fun getAllProductsByOwner(ownerId: String): List<Product>

    suspend fun refreshProducts()

    fun observeProductById(productId: String): LiveData<Product?>

    suspend fun getProductById(productId: String): Product?

    suspend fun insertProduct(newProduct: Product)

    suspend fun insertMultipleProducts(proList: List<Product>)

    suspend fun deleteProduct(productId: String)
}