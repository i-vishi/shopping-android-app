package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.viewModels.HomeViewModel
import com.vishalgaur.shoppingapp.viewModels.HomeViewModelFactory

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

	private lateinit var viewModel: HomeViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "onCreate starts")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}


//	override fun onDestroy() {
//		Log.d(TAG, "MainActivity destroyed")
//		if (!viewModel.authRepository.isRememberMeOn()) {
//			viewModel.authRepository.signOut()
//		}
//		super.onDestroy()
//	}

}