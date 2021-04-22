package com.vishalgaur.shoppingapp.database.user

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(uData: UserData)

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getById(userId: String): UserData?

    @Query("DELETE FROM users")
    fun clear()
}

//@Database(entities = [UserData::class], version = 1)
//abstract class UserDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: UserDatabase? = null
//
//        fun getInstance(context: Context): UserDatabase =
//            INSTANCE ?: synchronized(this) {
//                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
//            }
//
//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(
//                context.applicationContext,
//                UserDatabase::class.java, "ShoppingAppDb"
//            )
//                .fallbackToDestructiveMigration()
//                .allowMainThreadQueries()
//                .build()
//    }
//}