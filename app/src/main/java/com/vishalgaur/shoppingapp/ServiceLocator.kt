package com.vishalgaur.shoppingapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserLocalDataSource
import com.vishalgaur.shoppingapp.data.source.remote.AuthRemoteDataSource
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository

object ServiceLocator {
	private var database: ShoppingAppDatabase? = null
	private val lock = Any()

	@Volatile
	var authRepository: AuthRepository? = null
		@VisibleForTesting set

	fun provideAuthRepository(context: Context): AuthRepository {
		synchronized(this) {
			return authRepository ?: createAuthRepository(context)
		}
	}

	private fun createAuthRepository(context: Context): AuthRepository {
		val appSession = ShoppingAppSessionManager(context.applicationContext)
		val newRepo = AuthRepository(createUserLocalDataSource(context), AuthRemoteDataSource(), appSession)
		authRepository = newRepo
		return newRepo
	}

	private fun createUserLocalDataSource(context: Context): UserLocalDataSource {
		val database = database ?: ShoppingAppDatabase.getInstance(context.applicationContext)
		return UserLocalDataSource(database.userDao())
	}

//	private fun createDatabase(context: Context): ShoppingAppDatabase {
//		val db = Room.databaseBuilder(
//			context.applicationContext,
//			ShoppingAppDatabase::class.java, "ShoppingAppDb"
//		)
//			.fallbackToDestructiveMigration()
//			.allowMainThreadQueries()
//			.build()
//		database = db
//		return db
//	}
}