package com.vishalgaur.shoppingapp

import android.app.Application
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository

class ShoppingApplication: Application() {
	val authRepository: AuthRepository
	get() = ServiceLocator.provideAuthRepository(this)

	override fun onCreate() {
		super.onCreate()
	}
}