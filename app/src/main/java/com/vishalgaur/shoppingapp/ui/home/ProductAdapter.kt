package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.databinding.ProductsListItemBinding
import com.vishalgaur.shoppingapp.getOfferPercentage

class ProductAdapter(private val data: List<Product>, private val context: Context) :
	RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

	lateinit var onClickListener: OnClickListener
	lateinit var bindImageButtons: BindImageButtons
	private val sessionManager = ShoppingAppSessionManager(context)

	inner class ViewHolder(binding: ProductsListItemBinding) :
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
		private val proLikeButton = binding.productLikeCheckbox
		private val proCartButton = binding.productAddToCartButton

		fun bind(productData: Product) {
			productCard.setOnClickListener {
				onClickListener.onClick(productData)
			}
			proName.text = productData.name
			proPrice.text =
				context.getString(R.string.pro_details_price_value, productData.price.toString())
			proRatingBar.rating = productData.rating.toFloat()
			proMrp.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
			proMrp.text =
				context.getString(
					R.string.pro_details_actual_strike_value,
					productData.mrp.toString()
				)
			proOffer.text = context.getString(
				R.string.pro_offer_precent_text,
				getOfferPercentage(productData.mrp, productData.price).toString()
			)

			val imgUrl = productData.images[0].toUri().buildUpon().scheme("https").build()
			Glide.with(context)
				.asBitmap()
				.load(imgUrl)
				.into(productImage)

			productImage.clipToOutline = true

			if (sessionManager.isUserSeller()) {
				proLikeButton.visibility = View.GONE
				proCartButton.visibility = View.GONE
				proEditBtn.setOnClickListener {
					onClickListener.onEditClick(productData.productId)
				}

				proDeleteButton.setOnClickListener {
					onClickListener.onDeleteClick(productData)
				}
			} else {
				proEditBtn.visibility = View.GONE
				proDeleteButton.visibility = View.GONE
				bindImageButtons.setLikeButton(productData.productId, proLikeButton)
				bindImageButtons.setCartButton(productData.productId, proCartButton)
				proLikeButton
				proLikeButton.setOnCheckedChangeListener { _, _ ->
					onClickListener.onLikeClick(productData.productId)

				}
				proCartButton.setOnClickListener {
					onClickListener.onAddToCartClick(productData)
				}
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

	interface BindImageButtons {
		fun setLikeButton(productId: String, button: CheckBox)
		fun setCartButton(productId: String, imgView: ImageView)
	}

	interface OnClickListener {
		fun onClick(productData: Product)
		fun onDeleteClick(productData: Product)
		fun onEditClick(productId: String)
		fun onLikeClick(productId: String)
		fun onAddToCartClick(productData: Product)
	}
}