package com.vishalgaur.shoppingapp.data.source.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource : UserDataSource {
    private val firebaseDb: FirebaseFirestore = Firebase.firestore

    private fun usersCollectionRef() = firebaseDb.collection(USERS_COLLECTION)
    private fun allEmailsMobilesRef() =
        firebaseDb.collection(USERS_COLLECTION).document(EMAIL_MOBILE_DOC)


    override suspend fun getUserById(userId: String): Result<UserData?> {
        val resRef = usersCollectionRef().whereEqualTo(USERS_ID_FIELD, userId).get()
        return if (resRef.isSuccessful) {
            Success(resRef.result?.documents?.get(0)?.toObject(UserData::class.java))
        } else {
            Error(Exception(resRef.exception))
        }
    }


    override suspend fun addUser(userData: UserData) {
        usersCollectionRef().add(userData.toHashMap())
            .addOnSuccessListener {
                Log.d(TAG, "Doc added")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "firestore error occurred: $e")
            }
    }

     override suspend fun getUserByMobile(pNumber: String) =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, pNumber).get().await()
            .documents[0]
            .toObject(UserData::class.java)

     override suspend fun getUserByMobileAndPassword(mobile: String, pwd: String): MutableList<DocumentSnapshot> =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, mobile)
            .whereEqualTo(USERS_PWD_FIELD, pwd).get().await().documents

     override fun updateEmailsAndMobiles(email: String, mobile: String) {
        allEmailsMobilesRef().update(EMAIL_MOBILE_EMAIL_FIELD, FieldValue.arrayUnion(email))
        allEmailsMobilesRef().update(EMAIL_MOBILE_MOB_FIELD, FieldValue.arrayUnion(mobile))
    }

     override suspend fun getEmailsAndMobiles() = allEmailsMobilesRef().get().await().toObject(
         EmailMobileData::class.java)

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USERS_ID_FIELD = "userId"
        private const val USERS_MOBILE_FIELD = "mobile"
        private const val USERS_PWD_FIELD = "password"
        private const val EMAIL_MOBILE_DOC = "emailAndMobiles"
        private const val EMAIL_MOBILE_EMAIL_FIELD = "emails"
        private const val EMAIL_MOBILE_MOB_FIELD = "mobiles"
        private const val TAG = "AuthRemoteDataSource"
    }
}