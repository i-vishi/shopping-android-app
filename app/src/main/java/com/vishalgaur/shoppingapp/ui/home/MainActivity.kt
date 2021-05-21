package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "onCreate starts")
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Bottom Navigation
		setUpNav()
	}

	private fun setUpNav() {
		val navFragment =
			supportFragmentManager.findFragmentById(R.id.home_nav_host_fragment) as NavHostFragment
		NavigationUI.setupWithNavController(binding.homeBottomNavigation, navFragment.navController)

		navFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
			when (destination.id) {
				R.id.homeFragment -> setBottomNavVisibility(View.VISIBLE)
				R.id.cartFragment -> setBottomNavVisibility(View.VISIBLE)
				R.id.accountFragment -> setBottomNavVisibility(View.VISIBLE)
				R.id.orderSuccessFragment -> setBottomNavVisibility(View.VISIBLE)
				else -> setBottomNavVisibility(View.GONE)
			}
		}

		val sessionManager = ShoppingAppSessionManager(this.applicationContext)
		if (sessionManager.isUserSeller()) {
			binding.homeBottomNavigation.menu.removeItem(R.id.cartFragment)
		}else {
			binding.homeBottomNavigation.menu.removeItem(R.id.ordersFragment)
		}
	}

	private fun setBottomNavVisibility(visibility: Int) {
		binding.homeBottomNavigation.visibility = visibility
	}
}