package com.vishalgaur.shoppingapp.ui.loginSignup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.ui.home.MainActivity
import com.vishalgaur.shoppingapp.viewModels.AuthViewModel
import com.vishalgaur.shoppingapp.viewModels.AuthViewModelFactory

class LoginSignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = AuthViewModelFactory(application)
        val viewModel =
            ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        viewModel.authRepository.isLoggedIn.observe(this) {
            if (it == true) {
                launchHome()
            }else {
				setContentView(R.layout.activity_signup)
			}
        }
    }


    private fun launchHome() {
        val homeIntent = Intent(this, MainActivity::class.java)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(homeIntent)
        finish()
    }
}