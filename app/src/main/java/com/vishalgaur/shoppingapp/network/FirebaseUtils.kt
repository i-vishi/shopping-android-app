package com.vishalgaur.shoppingapp.network

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

enum class SignUpErrors { NONE, SERR }

enum class LogInErrors { NONE, LERR }

data class EmailMobileData(
    val emails: ArrayList<String> = ArrayList(),
    val mobiles: ArrayList<String> = ArrayList()
)

class FirebaseDbUtils {
    private var firebaseDb = Firebase.firestore

    private fun usersCollectionRef() = firebaseDb.collection(USERS_COLLECTION)

    private fun allEmailsMobilesRef() =
        firebaseDb.collection(USERS_COLLECTION).document(EMAIL_MOBILE_DOC)

    fun addUser(data: HashMap<String, String>) = usersCollectionRef().add(data)

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


    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USERS_MOBILE_FIELD = "mobile"
        private const val USERS_PWD_FIELD = "password"
        private const val EMAIL_MOBILE_DOC = "emailAndMobiles"
        private const val EMAIL_MOBILE_EMAIL_FIELD = "emails"
        private const val EMAIL_MOBILE_MOB_FIELD = "mobiles"
    }
}