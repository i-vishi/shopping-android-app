package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vishalgaur.shoppingapp.database.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager :ShoppingAppSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        sessionManager = ShoppingAppSessionManager(this)

        val uData: HashMap<String, String?>?
        if (sessionManager.isLoggedIn()) {
            uData = sessionManager.getUserDataFromSession()
            val s = "UserName: " + uData["userName"]
            binding.textView.text = s
        }
        setContentView(binding.root)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if(!sessionManager.isRememberMeOn()){
//            sessionManager.logoutFromSession()
//        }
//        Log.d("TAGTAGTAG", "MainActivity destroyed")
//    }
}