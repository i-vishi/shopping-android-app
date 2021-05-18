package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.databinding.ProductsListItemBinding
import com.vishalgaur.shoppingapp.getOfferPercentage

class LikedProductAdapter(proList: List<Product>, private val context: Context) :
	RecyclerView.Adapter<LikedProductAdapter.ViewHolder>() {

	var data = proList
	lateinit var onClickListener: OnClickListener

	inner class ViewHolder(private val binding: ProductsListItemBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(productData: Product) {
			binding.productCard.setOnClickListener {
				onClickListener.onClick(productData)
			}
			binding.productNameTv.text = productData.name
			binding.productPriceTv.text =
				context.getString(R.string.pro_details_price_value, productData.price.toString())
			binding.productRatingBar.rating = productData.rating.toFloat()
			binding.productActualPriceTv.apply {
				paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
				text = context.getString(
					R.string.pro_details_actual_strike_value,
					productData.mrp.toString()
				)
			}
			binding.productOfferValueTv.text = context.getString(
				R.string.pro_offer_precent_text,
				getOfferPercentage(productData.mrp, productData.price).toString()
			)
			val imgUrl = productData.images[0].toUri().buildUpon().scheme("https").build()
			Glide.with(context)
				.asBitmap()
				.load(imgUrl)
				.into(binding.productImageView)
			binding.productImageView.clipToOutline = true

			//hiding unnecessary button
			binding.productAddToCartButton.visibility = View.GONE
			binding.productDeleteButton.visibility = View.GONE
			binding.productLikeCheckbox.visibility = View.GONE

			// setting edit button as delete button
			binding.productEditButton.setImageResource(R.drawable.ic_delete_24)
			binding.productEditButton.setOnClickListener {
				onClickListener.onDeleteClick(productData.productId)
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
		holder.bind(data[position])
	}

	override fun getItemCount() = data.size

	interface OnClickListener {
		fun onClick(productData: Product)
		fun onDeleteClick(productId: String)
	}
}