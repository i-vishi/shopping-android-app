package com.vishalgaur.shoppingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.database.products.ProductsDao
import com.vishalgaur.shoppingapp.database.user.UserDao
import com.vishalgaur.shoppingapp.database.user.UserData


@Database(entities = [UserData::class, Product::class], version = 1)
@TypeConverters(ListTypeConverter::class)
abstract class ShoppingAppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productsDao(): ProductsDao

    companion object {
        @Volatile
        private var INSTANCE: ShoppingAppDb? = null

        fun getInstance(context: Context): ShoppingAppDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ShoppingAppDb::class.java, "ShoppingAppDb"
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}