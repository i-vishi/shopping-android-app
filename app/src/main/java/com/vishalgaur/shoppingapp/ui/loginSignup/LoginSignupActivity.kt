package com.vishalgaur.shoppingapp.ui.loginSignup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.ui.launchHome

class LoginSignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = ShoppingAppSessionManager(this)

        if (sessionManager.isLoggedIn()) {
            launchHome(this)
            finish()
        } else {
            setContentView(R.layout.activity_signup)
        }
    }
}