package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.LayoutListItemBinding

private const val TAG = "PayByAdapter"

class PayByAdapter(private val data: List<String>) :
	RecyclerView.Adapter<PayByAdapter.ViewHolder>() {

	var lastCheckedMethod: String? = null
	private var lastCheckedCard: MaterialCardView? = null
	private var selectedMethodPos = -1

	inner class ViewHolder(binding: LayoutListItemBinding) :
		RecyclerView.ViewHolder(binding.root) {
		val textView = binding.itemTitleTextView
		val cardView = binding.itemCard
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			LayoutListItemBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	@SuppressLint("ResourceAsColor")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val title = data[position]
		holder.apply {
			textView.text = title
			cardView.setOnClickListener {
				onCardClick(position, data[position], it as MaterialCardView)
			}
		}
	}

	override fun getItemCount() = data.size

	private fun onCardClick(position: Int, method: String, cardView: MaterialCardView) {
		if (method != lastCheckedMethod) {
			cardView.apply {
				strokeColor = context.getColor(R.color.blue_accent_300)
				isChecked = true
				strokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					2F,
					resources.displayMetrics
				).toInt()
			}

			lastCheckedCard?.apply {
				strokeColor = context.getColor(R.color.light_gray)
				isChecked = false
				strokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					1F,
					resources.displayMetrics
				).toInt()
			}

			lastCheckedCard = cardView
			lastCheckedMethod = method
			selectedMethodPos = position
			Log.d(TAG, "onSelectMethod: Selected Method = $lastCheckedMethod")
		}
	}
}