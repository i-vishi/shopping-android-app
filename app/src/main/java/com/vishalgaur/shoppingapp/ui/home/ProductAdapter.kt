package com.vishalgaur.shoppingapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.databinding.ProductsListItemBinding

class ProductAdapter(private val data: List<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
	class ViewHolder(binding: ProductsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
		val proName = binding.productNameTv
		val proPrice = binding.productPriceTv
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(ProductsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val proData = data[position]
		holder.apply {
			proName.text = proData.name
			proPrice.text = "Price: $${proData.price}"
		}
	}

	override fun getItemCount(): Int = data.size

	private var onItemClickListener: ((Product) -> Unit)? = null

	fun setOnItemClickListener(listener: ((Product) -> Unit)) {
		onItemClickListener = listener
	}
}