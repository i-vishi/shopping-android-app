package com.vishalgaur.shoppingapp

import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

const val MOB_ERROR_TEXT = "Enter valid mobile number!"
const val EMAIL_ERROR_TEXT = "Enter valid email address!"
const val ERR_INIT = "ERROR"
const val ERR_EMAIL = "_EMAIL"
const val ERR_MOBILE = "_MOBILE"
const val ERR_UPLOAD = "UploadErrorException"

internal fun isEmailValid(email: String): Boolean {
    val EMAIL_PATTERN = Pattern.compile(
		"\\s*[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
				"\\@" +
				"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
				"(" +
				"\\." +
				"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
				")+\\s*"
	)
    return if (email.isEmpty()) {
        false
    } else {
        EMAIL_PATTERN.matcher(email).matches()
    }
}

internal fun isPhoneValid(phone: String): Boolean {
    val PHONE_PATTERN = Pattern.compile("^\\s*[6-9]\\d{9}\\s*\$")
    return if (phone.isEmpty()) {
        false
    } else {
        PHONE_PATTERN.matcher(phone).matches()
    }
}

internal fun getRandomString(length: Int, uNum: String, endLength: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    fun getStr(l: Int): String = (1..l).map { allowedChars.random() }.joinToString("")
    return getStr(length) + uNum + getStr(endLength)
}

internal fun getProductId(ownerId: String, proCategory: String): String {
    val uniqueId = UUID.randomUUID().toString()
    return "pro-$proCategory-$ownerId-$uniqueId"
}

internal fun getOfferPercentage(costPrice: Double, sellingPrice: Double): Int {
    val off = ((costPrice - sellingPrice) * 100) / costPrice
    return off.roundToInt()
}


