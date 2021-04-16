package com.vishalgaur.shoppingapp.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseUtils {

    private lateinit var firebaseAuth: FirebaseAuth

    fun initializeAuth() {
        firebaseAuth = Firebase.auth
    }

    fun signUp(mobile:String, password: String) {
    }
}