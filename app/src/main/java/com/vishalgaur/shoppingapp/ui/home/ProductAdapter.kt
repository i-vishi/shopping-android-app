package com.vishalgaur.shoppingapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.databinding.ProductsListItemBinding

class ProductAdapter(private val data: List<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

	lateinit var onClickListener: OnClickListener

	class ViewHolder(binding: ProductsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
		val proName = binding.productNameTv
		val proPrice = binding.productPriceTv
		val productCard = binding.productCard
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(ProductsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val proData = data[position]
		holder.apply {
			productCard.setOnClickListener {
				onClickListener.onClick(proData)
			}
			proName.text = proData.name
			proPrice.text = "Price: $${proData.price}"

		}
	}

	override fun getItemCount(): Int = data.size

	interface OnClickListener {
		fun onClick(productData: Product)
	}
}