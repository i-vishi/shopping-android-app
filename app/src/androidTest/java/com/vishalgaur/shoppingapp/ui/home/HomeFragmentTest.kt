package com.vishalgaur.shoppingapp.ui.home

import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest{
	private lateinit var homeScenario: FragmentScenario<HomeFragment>
	private lateinit var navController: NavController
	private lateinit var sessionManager: ShoppingAppSessionManager

//	@Before
//	fun setUp() {
//		sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())
//	}
}