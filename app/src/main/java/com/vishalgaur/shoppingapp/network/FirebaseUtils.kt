package com.vishalgaur.shoppingapp.network

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

enum class SignUpErrors { NONE, SERR }

enum class LogInErrors { NONE, LERR }

enum class AddProductErrors { NONE, ERR_ADD }

enum class UserType { CUSTOMER, SELLER }

enum class StoreDataStatus { LOADING, ERROR, DONE }

data class EmailMobileData(
    val emails: ArrayList<String> = ArrayList(),
    val mobiles: ArrayList<String> = ArrayList()
)

class FirebaseDbUtils {
    private var firebaseDb = Firebase.firestore
    private var firebaseStorage = Firebase.storage

    private fun usersCollectionRef() = firebaseDb.collection(USERS_COLLECTION)
    private fun productsCollectionRef() = firebaseDb.collection(PRODUCT_COLLECTION)
    private fun allEmailsMobilesRef() =
        firebaseDb.collection(USERS_COLLECTION).document(EMAIL_MOBILE_DOC)

    private fun storageRef() = firebaseStorage.reference
    private fun shoesStorageRef() = storageRef().child(SHOES_STORAGE_PATH)

    fun addUser(data: HashMap<String, String>) = usersCollectionRef().add(data)

    fun addProduct(data: HashMap<String, Any>) = productsCollectionRef().add(data)

    fun getUserByMobileAndPassword(mobile: String, pwd: String) =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, mobile)
            .whereEqualTo(USERS_PWD_FIELD, pwd).get()

    fun getUserByMobile(mobile: String) =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, mobile).get()

    fun updateEmailsAndMobiles(email: String, mobile: String) {
        allEmailsMobilesRef().update(EMAIL_MOBILE_EMAIL_FIELD, FieldValue.arrayUnion(email))
        allEmailsMobilesRef().update(EMAIL_MOBILE_MOB_FIELD, FieldValue.arrayUnion(mobile))
    }

    fun getEmailsAndMobiles() = allEmailsMobilesRef().get()

    fun getProductsByOwner(ownerId: String) =
        productsCollectionRef().whereEqualTo(PRODUCT_OWNER_FIELD, ownerId).get()

    fun getProductById(productId: String) =
        productsCollectionRef().whereEqualTo(PRODUCT_ID_FIELD, productId).get()

    fun getAllProducts() = productsCollectionRef().get()

    suspend fun uploadImage(uri: Uri, fileName: String): Uri {
        val imgRef = storageRef().child("$SHOES_STORAGE_PATH/$fileName")
        val uploadTask = imgRef.putFile(uri)
        val urlRef = uploadTask.continueWithTask { task->
            if(!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imgRef.downloadUrl
        }
        return urlRef.await()
    }


    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USERS_MOBILE_FIELD = "mobile"
        private const val USERS_PWD_FIELD = "password"
        private const val EMAIL_MOBILE_DOC = "emailAndMobiles"
        private const val EMAIL_MOBILE_EMAIL_FIELD = "emails"
        private const val EMAIL_MOBILE_MOB_FIELD = "mobiles"
        private const val PRODUCT_COLLECTION = "products"
        private const val PRODUCT_OWNER_FIELD = "owner"
        private const val PRODUCT_ID_FIELD = "productId"

        private const val SHOES_STORAGE_PATH = "Shoes"
    }
}