package com.vishalgaur.shoppingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserData(
		@PrimaryKey(autoGenerate = true)
		var userId: String,
		var userName: String,
		var mobile: String,
)
