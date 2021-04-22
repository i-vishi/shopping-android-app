package com.vishalgaur.shoppingapp.database.products

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    var productId: String = "",
    var name: String = "",
    var owner: String = "",
    var description: String = "",
    var price: Long = 0L,
    var availableSizes: List<Int>,
    var availableColors: List<String>,
    var rating: Double = 0.0
) : Parcelable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "productId" to productId,
            "name" to name,
            "owner" to owner,
            "price" to price,
            "description" to description,
            "availableSizes" to availableSizes,
            "availableColors" to availableColors,
            "rating" to rating
        )
    }
}
