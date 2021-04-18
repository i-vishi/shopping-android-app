package com.vishalgaur.shoppingapp.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "users")
data class UserData(
		@PrimaryKey
		var userId: String,
		var name: String,
		var mobile: String,
		var email: String,
		var password: String
): Parcelable
