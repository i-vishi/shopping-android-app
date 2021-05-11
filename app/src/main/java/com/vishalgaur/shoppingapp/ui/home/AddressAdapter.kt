package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.getISOCountriesMap
import com.vishalgaur.shoppingapp.databinding.LayoutAddressCardBinding

class AddressAdapter(private val context: Context, addresses: List<UserData.Address>) :
	RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

	lateinit var onClickListener: OnClickListener
	private val data: List<UserData.Address> = addresses

	private var lastCheckedAddress: String? = null
	private var lastCheckedCard: MaterialCardView? = null
	var selectedAddressPos = -1

	inner class ViewHolder(private var binding: LayoutAddressCardBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(address: UserData.Address, position: Int) {
			binding.addressCard.isChecked = position == selectedAddressPos
			binding.addressPersonNameTv.text =
				context.getString(R.string.person_name, address.fName, address.lName)
			binding.addressCompleteAddressTv.text = getCompleteAddress(address)
			binding.addressMobileTv.text = address.phoneNumber
			binding.addressCard.setOnClickListener {
				onCardClick(position, address.addressId, it as MaterialCardView)
			}
			binding.addressEditBtn.setOnClickListener {
				onClickListener.onEditClick(address.addressId)
			}
			binding.addressDeleteBtn.setOnClickListener {
				onClickListener.onDeleteClick(address.addressId)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
			LayoutAddressCardBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(data[position], position)
	}

	override fun getItemCount(): Int = data.size

	interface OnClickListener {
		fun onEditClick(addressTd: String)
		fun onDeleteClick(addressId: String)
	}

	@SuppressLint("ResourceAsColor")
	private fun onCardClick(position: Int, addressTd: String, card: MaterialCardView) {
		if (addressTd != lastCheckedAddress) {
			lastCheckedCard?.apply {
				isChecked = false
				strokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					1F,
					resources.displayMetrics
				).toInt()
				strokeColor = R.color.light_gray
			}
			card.apply {
				strokeColor = R.color.blue_accent_300
				isChecked = true
				strokeWidth = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					2F,
					resources.displayMetrics
				).toInt()
			}
			lastCheckedAddress = addressTd
			lastCheckedCard = card
			selectedAddressPos = position
		}
	}

	private fun getCompleteAddress(address: UserData.Address): String {
		return if (address.streetAddress2.isBlank()) {
			"${address.streetAddress}, ${address.city}, ${address.state} - ${address.zipCode}, ${getISOCountriesMap()[address.countryISOCode]}"
		} else {
			"${address.streetAddress}, ${address.streetAddress2}, ${address.city}, ${address.state} - ${address.zipCode}, ${getISOCountriesMap()[address.countryISOCode]}"
		}
	}
}