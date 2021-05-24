package com.vishalgaur.shoppingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.data.utils.AddObjectStatus
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.data.utils.getISOCountriesMap
import com.vishalgaur.shoppingapp.databinding.FragmentAddEditAddressBinding
import com.vishalgaur.shoppingapp.ui.AddAddressViewErrors
import com.vishalgaur.shoppingapp.ui.MyOnFocusChangeListener
import com.vishalgaur.shoppingapp.viewModels.AddEditAddressViewModel
import java.util.*
import kotlin.properties.Delegates

private const val TAG = "AddAddressFragment"

class AddEditAddressFragment : Fragment() {

	private lateinit var binding: FragmentAddEditAddressBinding
	private val focusChangeListener = MyOnFocusChangeListener()
	private val viewModel by viewModels<AddEditAddressViewModel>()

	private var isEdit by Delegates.notNull<Boolean>()
	private lateinit var addressId: String

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentAddEditAddressBinding.inflate(layoutInflater)

		isEdit = arguments?.getBoolean("isEdit") == true
		addressId = arguments?.getString("addressId").toString()

		initViewModel()

		setViews()

		setObservers()
		return binding.root
	}

	private fun initViewModel() {
		viewModel.setIsEdit(isEdit)
		if (isEdit) {
			viewModel.setAddressData(addressId)
		}
	}

	private fun setViews() {
		if (!isEdit) {
			binding.addAddressTopAppBar.topAppBar.title = "Add Address"
		} else {
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

		binding.addAddressSaveBtn.setOnClickListener {
			onAddAddress()
			if (viewModel.errorStatus.value?.isEmpty() == true) {
				viewModel.addAddressStatus.observe(viewLifecycleOwner) { status ->
					if (status == AddObjectStatus.DONE) {
						findNavController().navigateUp()
					}
				}
			}
		}
	}

	private fun setObservers() {
		viewModel.errorStatus.observe(viewLifecycleOwner) { errList ->
			if (errList.isEmpty()) {
				binding.addAddressErrorTextView.visibility = View.GONE
			} else {
				modifyErrors(errList)
			}
		}

		viewModel.dataStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				StoreDataStatus.LOADING -> setLoaderState(View.VISIBLE)
				StoreDataStatus.ERROR -> {
					setLoaderState()
					makeToast("Error getting Data, Try Again!")
				}
				StoreDataStatus.DONE -> {
					fillDataInViews()
					setLoaderState()
				}
				else -> {
					setLoaderState()
				}
			}
		}

		viewModel.addAddressStatus.observe(viewLifecycleOwner) { status ->
			when (status) {
				AddObjectStatus.DONE -> setLoaderState()
				AddObjectStatus.ERR_ADD -> {
					setLoaderState()
					binding.addAddressErrorTextView.visibility = View.VISIBLE
					binding.addAddressErrorTextView.text =
						getString(R.string.save_address_error_text)
					makeToast(getString(R.string.save_address_error_text))
				}
				AddObjectStatus.ADDING -> {
					binding.loaderLayout.circularLoader.bringToFront()
					setLoaderState(View.VISIBLE)
				}
				else -> setLoaderState()
			}
		}
	}

	private fun fillDataInViews() {
		viewModel.addressData.value?.let { address ->
			binding.addAddressTopAppBar.topAppBar.title = "Edit Address"
			val countryName = getISOCountriesMap()[address.countryISOCode]
			binding.addressCountryEditText.setText(countryName, false)
			binding.addressFirstNameEditText.setText(address.fName)
			binding.addressLastNameEditText.setText(address.lName)
			binding.addressStreetAddEditText.setText(address.streetAddress)
			binding.addressStreetAdd2EditText.setText(address.streetAddress2)
			binding.addressCityEditText.setText(address.city)
			binding.addressStateEditText.setText(address.state)
			binding.addressZipcodeEditText.setText(address.zipCode)
			binding.addressPhoneEditText.setText(address.phoneNumber.substringAfter("+91"))
			binding.addAddressSaveBtn.setText(R.string.save_address_btn_text)
		}
	}

	private fun makeToast(errText: String) {
		Toast.makeText(context, errText, Toast.LENGTH_LONG).show()
	}

	private fun setLoaderState(isVisible: Int = View.GONE) {
		binding.loaderLayout.circularLoader.visibility = isVisible
		if (isVisible == View.GONE) {
			binding.loaderLayout.circularLoader.hideAnimationBehavior
		} else {
			binding.loaderLayout.circularLoader.showAnimationBehavior
		}
	}

	private fun onAddAddress() {
		val countryName = binding.addressCountryEditText.text.toString()
		val firstName = binding.addressFirstNameEditText.text.toString()
		val lastName = binding.addressLastNameEditText.text.toString()
		val streetAdd = binding.addressStreetAddEditText.text.toString()
		val streetAdd2 = binding.addressStreetAdd2EditText.text.toString()
		val city = binding.addressCityEditText.text.toString()
		val state = binding.addressStateEditText.text.toString()
		val zipCode = binding.addressZipcodeEditText.text.toString()
		val phoneNumber = binding.addressPhoneEditText.text.toString()

		val countryCode =
			getISOCountriesMap().keys.find { Locale("", it).displayCountry == countryName }

		Log.d(TAG, "onAddAddress: Add/Edit Address Initiated")
		viewModel.submitAddress(
			countryCode!!,
			firstName,
			lastName,
			streetAdd,
			streetAdd2,
			city,
			state,
			zipCode,
			phoneNumber
		)
	}

	private fun setCountrySelectTextField() {
		val isoCountriesMap = getISOCountriesMap()
		val countries = isoCountriesMap.values.toSortedSet().toList()
		val defaultCountry = Locale.getDefault().displayCountry
		val countryAdapter = ArrayAdapter(requireContext(), R.layout.country_list_item, countries)
		(binding.addressCountryEditText as? AutoCompleteTextView)?.let {
			it.setText(defaultCountry, false)
			it.setAdapter(countryAdapter)
		}
	}

	private fun modifyErrors(errList: List<AddAddressViewErrors>) {
		binding.fNameOutlinedTextField.error = null
		binding.lNameOutlinedTextField.error = null
		binding.streetAddOutlinedTextField.error = null
		binding.cityOutlinedTextField.error = null
		binding.stateOutlinedTextField.error = null
		binding.zipCodeOutlinedTextField.error = null
		binding.phoneOutlinedTextField.error = null
		errList.forEach { err ->
			when (err) {
				AddAddressViewErrors.EMPTY -> setEditTextsError(true)
				AddAddressViewErrors.ERR_FNAME_EMPTY ->
					setEditTextsError(true, binding.fNameOutlinedTextField)
				AddAddressViewErrors.ERR_LNAME_EMPTY ->
					setEditTextsError(true, binding.lNameOutlinedTextField)
				AddAddressViewErrors.ERR_STR1_EMPTY ->
					setEditTextsError(true, binding.streetAddOutlinedTextField)
				AddAddressViewErrors.ERR_CITY_EMPTY ->
					setEditTextsError(true, binding.cityOutlinedTextField)
				AddAddressViewErrors.ERR_STATE_EMPTY ->
					setEditTextsError(true, binding.stateOutlinedTextField)
				AddAddressViewErrors.ERR_ZIP_EMPTY ->
					setEditTextsError(true, binding.zipCodeOutlinedTextField)
				AddAddressViewErrors.ERR_ZIP_INVALID ->
					setEditTextsError(false, binding.zipCodeOutlinedTextField)
				AddAddressViewErrors.ERR_PHONE_INVALID ->
					setEditTextsError(false, binding.phoneOutlinedTextField)
				AddAddressViewErrors.ERR_PHONE_EMPTY ->
					setEditTextsError(true, binding.phoneOutlinedTextField)
			}
		}
	}

	private fun setEditTextsError(isEmpty: Boolean, editText: TextInputLayout? = null) {
		if (isEmpty) {
			binding.addAddressErrorTextView.visibility = View.VISIBLE
			if (editText != null) {
				editText.error = "Please Fill the Form"
				editText.errorIconDrawable = null
			}
		} else {
			binding.addAddressErrorTextView.visibility = View.GONE
			editText!!.error = "Invalid!"
			editText.errorIconDrawable = null
		}
	}
}