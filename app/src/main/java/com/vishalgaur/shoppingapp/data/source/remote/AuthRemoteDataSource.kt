package com.vishalgaur.shoppingapp.data.source.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource

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

    fun getUserByMobile(pNumber: String) =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, pNumber).get()

    fun getUserByMobileAndPassword(mobile: String, pwd: String) =
        usersCollectionRef().whereEqualTo(USERS_MOBILE_FIELD, mobile)
            .whereEqualTo(USERS_PWD_FIELD, pwd).get()

    fun updateEmailsAndMobiles(email: String, mobile: String) {
        allEmailsMobilesRef().update(EMAIL_MOBILE_EMAIL_FIELD, FieldValue.arrayUnion(email))
        allEmailsMobilesRef().update(EMAIL_MOBILE_MOB_FIELD, FieldValue.arrayUnion(mobile))
    }

    fun getEmailsAndMobiles() = allEmailsMobilesRef().get()

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