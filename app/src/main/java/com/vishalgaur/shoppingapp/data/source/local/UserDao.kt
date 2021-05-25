package com.vishalgaur.shoppingapp.data.source.local

import androidx.room.*
import com.vishalgaur.shoppingapp.data.UserData

@Dao
interface UserDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(uData: UserData)

	@Query("SELECT * FROM users WHERE userId = :userId")
	suspend fun getById(userId: String): UserData?

	@Query("SELECT * FROM users WHERE mobile = :mobile")
	suspend fun getByMobile(mobile: String): UserData?

	@Update(entity = UserData::class)
	suspend fun updateUser(obj: UserData)

	@Query("DELETE FROM users")
	suspend fun clear()
}