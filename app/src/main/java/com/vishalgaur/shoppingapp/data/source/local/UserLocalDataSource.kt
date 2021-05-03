package com.vishalgaur.shoppingapp.data.source.local

import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class UserLocalDataSource internal constructor(
    private val userDao: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserDataSource {

    override suspend fun addUser(userData: UserData) {
        withContext(ioDispatcher) {
            userDao.clear()
            userDao.insert(userData)
        }
    }

    override suspend fun getUserById(userId: String): Result<UserData?> = withContext(ioDispatcher) {
        try {
            val uData = userDao.getById(userId)
            if (uData != null) {
                return@withContext Success(uData)
            } else {
                return@withContext Error(Exception("User Not Found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }


    suspend fun clearUser() {
        withContext(ioDispatcher) {
            userDao.clear()
        }
    }
}