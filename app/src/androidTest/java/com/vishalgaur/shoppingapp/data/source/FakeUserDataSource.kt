package com.vishalgaur.shoppingapp.data.source

import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.EmailMobileData

class FakeUserDataSource(private var uData: UserData?) : UserDataSource {

	private var emailMobileData = EmailMobileData()

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

	override suspend fun getEmailsAndMobiles(): EmailMobileData {
		return emailMobileData
	}

	override suspend fun getUserByMobileAndPassword(
		mobile: String,
		password: String
	): MutableList<UserData> {
		val res = mutableListOf<UserData>()
		uData?.let {
			if(it.mobile == mobile && it.password == password){
				res.add(it)
			}
		}
		return res
	}

	override suspend fun clearUser() {
		uData = null
	}

	override suspend fun getUserByMobile(phoneNumber: String): UserData? {
		return super.getUserByMobile(phoneNumber)
	}

	override fun updateEmailsAndMobiles(email: String, mobile: String) {
		emailMobileData.emails.add(email)
		emailMobileData.mobiles.add(mobile)
	}

}