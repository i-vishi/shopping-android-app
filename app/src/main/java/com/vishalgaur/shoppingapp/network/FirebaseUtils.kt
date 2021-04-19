package com.vishalgaur.shoppingapp.network

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


private const val TAG = "FirebaseUtils"

enum class SignUpErrors  {NONE, SERR}

class FirebaseUtils {

    private var firebaseAuth: FirebaseAuth = Firebase.auth

    fun signUp(email: String, password: String) {}
}

data class EmailMobileData(
    val emails: ArrayList<String> = ArrayList(),
    val mobiles: ArrayList<String> = ArrayList()
)