package com.vishalgaur.shoppingapp.data.source

import com.google.firebase.firestore.DocumentSnapshot
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData

class FakeUserDataSource(var uData: UserData?) : UserDataSource {
    override suspend fun addUser(userData: UserData) {
        uData = userData
    }

    override suspend fun getUserById(userId: String): Result<UserData?> {
        uData?.let {
            if (it.userId == userId) {
                return Success(it)
            }
        }
        return Error(Exception("User Not Found"))
    }

    override suspend fun getEmailsAndMobiles(): EmailMobileData? {
        return super.getEmailsAndMobiles()
    }

    override suspend fun getUserByMobileAndPassword(
        mobile: String,
        password: String
    ): MutableList<DocumentSnapshot> {
        return super.getUserByMobileAndPassword(mobile, password)
    }

    override suspend fun clearUser() {
        super.clearUser()
    }

    override suspend fun getUserByMobile(phoneNumber: String): UserData? {
        return super.getUserByMobile(phoneNumber)
    }
}