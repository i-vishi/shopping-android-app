package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vishalgaur.shoppingapp.R

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "onCreate starts")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}
}