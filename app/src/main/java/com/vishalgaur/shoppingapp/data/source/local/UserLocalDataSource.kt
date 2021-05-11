package com.vishalgaur.shoppingapp.data.source.local

import android.util.Log
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.UserDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

	override suspend fun getUserById(userId: String): Result<UserData?> =
		withContext(ioDispatcher) {
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

	override suspend fun getUserByMobile(phoneNumber: String): UserData? =
		withContext(ioDispatcher) {
			try {
				val uData = userDao.getByMobile(phoneNumber)
				if (uData != null) {
					return@withContext uData
				} else {
					return@withContext null
				}
			} catch (e: Exception) {
				Log.d("UserLocalSource", "onGetUser: Error Occurred, $e")
				return@withContext null
			}
		}

	override suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?> =
		withContext(ioDispatcher) {
			try {
				val user = userDao.getById(userId)
				val addressList = user?.addresses
				return@withContext Success(addressList)
			} catch (e: Exception) {
				Log.d("UserLocalSource", "onGetUser: Error Occurred, $e")
				return@withContext Error(e)
			}
		}

	override suspend fun clearUser() {
		withContext(ioDispatcher) {
			userDao.clear()
		}
	}

}