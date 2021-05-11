package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.databinding.FragmentAddEditAddressBinding
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import java.util.*
import kotlin.properties.Delegates

class AddEditAddressFragment : Fragment(){

	private lateinit var binding: FragmentAddEditAddressBinding
	private val focusChangeListener = MyOnFocusChangeListener()

	private var isEdit by Delegates.notNull<Boolean>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentAddEditAddressBinding.inflate(layoutInflater)

		isEdit = arguments?.getBoolean("isEdit") == true

		setViews()
		return binding.root
	}

	private fun setViews() {
		if(!isEdit) {
			binding.addAddressTopAppBar.topAppBar.title = "Add Address"
		}
		else {
			binding.addAddressTopAppBar.topAppBar.title = "Edit Address"
		}
		binding.addAddressTopAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}
		binding.loaderLayout.circularLoader.visibility = View.GONE
		binding.addressFirstNameEditText.onFocusChangeListener = focusChangeListener
		binding.addressLastNameEditText.onFocusChangeListener = focusChangeListener
		binding.addressStreetAddEditText.onFocusChangeListener = focusChangeListener
		binding.addressStreetAdd2EditText.onFocusChangeListener = focusChangeListener
		binding.addressCityEditText.onFocusChangeListener = focusChangeListener
		binding.addressStateEditText.onFocusChangeListener = focusChangeListener
		binding.addressZipcodeEditText.onFocusChangeListener = focusChangeListener
		binding.addressPhoneEditText.onFocusChangeListener = focusChangeListener
		setCountrySelectTextField()
	}

	private fun setCountrySelectTextField() {
		val isoCountries = Locale.getISOCountries()
		val countriesList = isoCountries.map { isoCountry ->
			Locale("", isoCountry).displayCountry
		}
		val countries = countriesList.toSortedSet().toList()
		val defaultCountry = Locale.getDefault().displayCountry
		val countryAdapter = ArrayAdapter(requireContext(), R.layout.country_list_item, countries)
		(binding.addressCountryEditText as? AutoCompleteTextView)?.let {
			it.setText(defaultCountry, false)
			it.setAdapter(countryAdapter)
		}
	}
}