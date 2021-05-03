package com.vishalgaur.shoppingapp.data.source

import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.UserData

interface UserDataSource {
    suspend fun addUser(userData: UserData)

    suspend fun getUserById(userId: String) : Result<UserData?>
}