package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vishalgaur.shoppingapp.database.SessionManager
import com.vishalgaur.shoppingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val sessionManager = SessionManager(this)

        val uData: HashMap<String, String?>?
        if (sessionManager.isLoggedIn()) {
            uData = sessionManager.getUserDataFromSession()
            val s = "UserName: " + uData["userName"]
            binding.textView.text = s
        }
        setContentView(binding.root)
    }
}