package com.vishalgaur.shoppingapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.local.UserLocalDataSource
import com.vishalgaur.shoppingapp.data.source.remote.AuthRemoteDataSource
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import kotlinx.coroutines.runBlocking

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
		val newRepo = AuthRepository(createUserLocalDataSource(context), AuthRemoteDataSource(), context)
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