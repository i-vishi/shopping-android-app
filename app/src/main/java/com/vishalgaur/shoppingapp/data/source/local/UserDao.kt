package com.vishalgaur.shoppingapp.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vishalgaur.shoppingapp.data.UserData

@Dao
interface UserDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(uData: UserData)

	@Query("SELECT * FROM users WHERE userId = :userId")
	suspend fun getById(userId: String): UserData?

	@Query("DELETE FROM users")
	suspend fun clear()
}