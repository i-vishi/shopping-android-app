package com.vishalgaur.shoppingapp.data.source.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.source.ProductDataSource
import kotlinx.coroutines.tasks.await

class ProductsRemoteDataSource: ProductDataSource {
    private val firebaseDb : FirebaseFirestore = Firebase.firestore
    private val firebaseStorage: FirebaseStorage = Firebase.storage

    private val observableProducts = MutableLiveData<Result<List<Product>>?>()

    private fun storageRef() = firebaseStorage.reference
    private fun productsCollectionRef() = firebaseDb.collection(PRODUCT_COLLECTION)
    private fun shoesStorageRef() = storageRef().child(SHOES_STORAGE_PATH)

    override suspend fun refreshProducts() {
        observableProducts.value = getAllProducts()
    }

    override fun observeProducts(): LiveData<Result<List<Product>>?> {
        return observableProducts
    }

    override suspend fun getAllProducts(): Result<List<Product>> {
        val resRef = productsCollectionRef().get().await()
        return if(!resRef.isEmpty) {
            Success(resRef.toObjects(Product::class.java))
        } else {
            Error(Exception("Error getting Products!"))
        }
    }

    override suspend fun insertProduct(newProduct: Product) {
        productsCollectionRef().add(newProduct.toHashMap()).await()
    }

    override suspend fun getProductById(productId: String): Result<Product?> {
        val resRef = productsCollectionRef().whereEqualTo(PRODUCT_ID_FIELD, productId).get().await()
        return if(!resRef.isEmpty) {
            Success(resRef.documents[0].toObject(Product::class.java))
        } else {
            Error(Exception("Product with id: $productId Not Found!"))
        }
    }

    suspend fun deleteProduct(productId: String) {
        // delete a product
        Log.d(TAG, "onDeleteProduct: delete product with Id: $productId initiated")
    }

    suspend fun uploadImage(uri: Uri, fileName: String): Uri {
        val imgRef = storageRef().child("$SHOES_STORAGE_PATH/$fileName")
        val uploadTask = imgRef.putFile(uri)
        val uriRef = uploadTask.continueWithTask { task->
            if(!task.isSuccessful){
                task.exception?.let { throw it }
            }
            imgRef.downloadUrl
        }
        return uriRef.await()
    }

    fun revertUpload(fileName: String) {
        val imgRef = storageRef().child("${SHOES_STORAGE_PATH}/$fileName")
        imgRef.delete().addOnSuccessListener {
            Log.d(TAG, "onRevert: File with name: $fileName deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onRevert: Error deleting file with name = $fileName, error: $e")
        }
    }

    companion object {
        private const val PRODUCT_COLLECTION = "products"
        private const val PRODUCT_ID_FIELD = "productId"
        private const val SHOES_STORAGE_PATH = "Shoes"
        private const val TAG = "ProductsRemoteSource"
    }
}