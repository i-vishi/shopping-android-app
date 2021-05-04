package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.databinding.ProductsListItemBinding

private const val TAG = "ProductAdapter"

class ProductAdapter(private val data: List<Product>, private val context: Context) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    lateinit var onClickListener: OnClickListener

    inner class ViewHolder(private var binding: ProductsListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val proName = binding.productNameTv
        private val proPrice = binding.productPriceTv
        private val productCard = binding.productCard
        private val productImage = binding.productImageView
        private val proDeleteButton = binding.productDeleteButton
        private val proEditBtn = binding.productEditButton
        private val proMrp = binding.productActualPriceTv
        private val proOffer = binding.productOfferValueTv
        private val proRatingBar = binding.productRatingBar

        fun bind(productData: Product) {
            productCard.setOnClickListener {
                onClickListener.onClick(productData)
            }
            proName.text = productData.name
            proPrice.text =
                context.getString(R.string.pro_details_price_value, productData.price.toString())
            proRatingBar.rating = productData.rating.toFloat()

            val imgUrl = productData.images[0].toUri().buildUpon().scheme("https").build()
            Glide.with(context)
                .asBitmap()
                .load(imgUrl)
                .into(productImage)

            productImage.clipToOutline = true

            proEditBtn.setOnClickListener {
                onEditProduct(productData.productId)
            }

            proDeleteButton.setOnClickListener {
                onDeleteProduct(productData.productId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
			ProductsListItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proData = data[position]
        holder.bind(proData)
    }

    override fun getItemCount(): Int = data.size

    interface OnClickListener {
        fun onClick(productData: Product)
    }

    fun onDeleteProduct(productId: String) {
        Log.d(TAG, "onDeleteProduct: deletion initiated for $productId")
    }

    fun onEditProduct(productId: String) {
        Log.d(TAG, "onEditProduct: Edit Initiated for $productId")
    }
}