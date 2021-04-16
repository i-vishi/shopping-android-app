package com.vishalgaur.shoppingapp

import java.util.regex.Pattern

enum class ViewErrors { NONE, ERR_EMAIL, ERR_MOBILE, ERR_EMAIL_MOBILE, ERR_EMPTY, ERR_NOT_ACC, ERR_PWD12NS }

enum class OTPStatus {NONE, CORRECT, WRONG}

const val MOB_ERROR = "Enter valid mobile number!"
const val EMAIL_ERROR = "Enter valid email address!"
const val ERR_INIT = "ERROR"
const val ERR_EMAIL = "_EMAIL"
const val ERR_MOBILE = "_MOBILE"

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

internal fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}