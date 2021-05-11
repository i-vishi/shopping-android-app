package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import com.vishalgaur.shoppingapp.data.utils.AddAddressStatus
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getAddressId
import com.vishalgaur.shoppingapp.isPhoneValid
import com.vishalgaur.shoppingapp.isZipCodeValid
import com.vishalgaur.shoppingapp.ui.AddAddressViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AddEditAddressViewModel(application: Application) : AndroidViewModel(application) {

	private val authRepository = AuthRepository.getRepository(application)

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)
	private val currentUser = sessionManager.getUserIdFromSession()

	private val _isEdit = MutableLiveData<Boolean>()
	val isEdit: LiveData<Boolean> get() = _isEdit

	private val _addressId = MutableLiveData<String>()
	val addressId: LiveData<String> get() = _addressId

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	private val _errorStatus = MutableLiveData<List<AddAddressViewErrors>>()
	val errorStatus: LiveData<List<AddAddressViewErrors>> get() = _errorStatus

	private val _addAddressStatus = MutableLiveData<AddAddressStatus?>()
	val addAddressStatus: LiveData<AddAddressStatus?> get() = _addAddressStatus

	private val _addressData = MutableLiveData<UserData.Address>()
	val addressData: LiveData<UserData.Address> get() = _addressData

	@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
	val newAddressData = MutableLiveData<UserData.Address>()

	init {
		_errorStatus.value = mutableListOf()
	}

	fun setIsEdit(state: Boolean) {
		_isEdit.value = state
	}

	fun setAddressData(addressId: String) {
		_addressId.value = addressId
	}

	fun submitAddress(
		countryCode: String,
		firstName: String,
		lastName: String,
		streetAdd: String,
		streetAdd2: String,
		city: String,
		state: String,
		zipCode: String,
		phoneNumber: String
	) {
		val errorsList = mutableListOf<AddAddressViewErrors>()
		if (firstName.isBlank() || lastName.isBlank() || streetAdd.isBlank() || city.isBlank() || state.isBlank() || zipCode.isBlank() || phoneNumber.isBlank())
			errorsList.add(AddAddressViewErrors.EMPTY)
		if (firstName.isBlank()) errorsList.add(AddAddressViewErrors.ERR_FNAME_EMPTY)
		if (lastName.isBlank()) errorsList.add(AddAddressViewErrors.ERR_LNAME_EMPTY)
		if (streetAdd.isBlank()) errorsList.add(AddAddressViewErrors.ERR_STR1_EMPTY)
		if (city.isBlank()) errorsList.add(AddAddressViewErrors.ERR_CITY_EMPTY)
		if (state.isBlank()) errorsList.add(AddAddressViewErrors.ERR_STATE_EMPTY)
		if (zipCode.isBlank()) errorsList.add(AddAddressViewErrors.ERR_ZIP_EMPTY)
		else if (!isZipCodeValid(zipCode)) errorsList.add(AddAddressViewErrors.ERR_ZIP_INVALID)
		if (phoneNumber.isBlank()) errorsList.add(AddAddressViewErrors.ERR_PHONE_EMPTY)
		else if (!isPhoneValid(phoneNumber)) errorsList.add(AddAddressViewErrors.ERR_PHONE_INVALID)

		_errorStatus.value = errorsList

		if (errorsList.isEmpty()) {
			val addressId = if (_isEdit.value == true) _addressId.value!! else
				getAddressId(currentUser!!)
			val newAddress = UserData.Address(
				addressId,
				firstName.trim(),
				lastName.trim(),
				countryCode.trim(),
				streetAdd.trim(),
				streetAdd2.trim(),
				city.trim(),
				state.trim(),
				zipCode.trim(),
				"+91" + phoneNumber.trim()
			)
			newAddressData.value = newAddress
			if (_isEdit.value == true) {
				updateAddress()
			} else {
				insertAddress()
			}
		}
	}

	private fun updateAddress() {
	}

	private fun insertAddress() {
		viewModelScope.launch {
			if (newAddressData.value != null) {
				_addAddressStatus.value = AddAddressStatus.ADDING
				val deferredRes = async {
					authRepository.insertAddress(newAddressData.value!!, currentUser!!)
				}
				val res = deferredRes.await()
				if (res is Success) {
					Log.d(TAG, "onInsertAddress: Success")
					_addAddressStatus.value = AddAddressStatus.DONE
				} else {
					_addAddressStatus.value = AddAddressStatus.ERR_ADD
					if (res is Error) {
						Log.d(TAG, "onInsertAddress: Error, ${res.exception.message}")
					}
				}
			} else {
				Log.d(TAG, "Address is Null, Cannot Add!")
			}
		}
	}

	companion object {
		private const val TAG = "AddAddressViewModel"
	}
}