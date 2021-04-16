package com.vishalgaur.shoppingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserData(
		@PrimaryKey
		var userId: String,
		var name: String,
		var mobile: String,
		var email: String,
		var password: String
)
