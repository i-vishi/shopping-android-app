package com.vishalgaur.shoppingapp

import android.app.Application
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepoInterface

class ShoppingApplication: Application() {
	val authRepository: AuthRepoInterface
	get() = ServiceLocator.provideAuthRepository(this)

	override fun onCreate() {
		super.onCreate()
	}
}