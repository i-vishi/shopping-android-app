package com.vishalgaur.shoppingapp.data.source

import com.google.firebase.firestore.DocumentSnapshot
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData

interface UserDataSource {
    suspend fun addUser(userData: UserData)

    suspend fun getUserById(userId: String): Result<UserData?>

    fun updateEmailsAndMobiles(email: String, mobile: String) {}

    suspend fun getEmailsAndMobiles(): EmailMobileData? {
        return null
    }

    suspend fun getUserByMobileAndPassword(
        mobile: String,
        password: String
    ): MutableList<DocumentSnapshot> {
        return mutableListOf()
    }

    suspend fun clearUser() {}

    suspend fun getUserByMobile(phoneNumber: String): UserData? {
        return null
    }
}