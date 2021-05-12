package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.source.repository.AuthRepository
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepository
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "OrderViewModel"

class OrderViewModel(application: Application) : AndroidViewModel(application) {

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)
	private val currentUser = sessionManager.getUserIdFromSession()

	private val authRepository = AuthRepository.getRepository(application)
	private val productsRepository = ProductsRepository.getRepository(application)

	private val _userAddresses = MutableLiveData<List<UserData.Address>>()
	val userAddresses: LiveData<List<UserData.Address>> get() = _userAddresses

	private val _cartItems = MutableLiveData<List<UserData.CartItem>>()
	val cartItems: LiveData<List<UserData.CartItem>> get() = _cartItems

	private val _priceList = MutableLiveData<Map<String, Double>>()
	val priceList: LiveData<Map<String, Double>> get() = _priceList

	private val _cartProducts = MutableLiveData<List<Product>>()
	val cartProducts: LiveData<List<Product>> get() = _cartProducts

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	init {
		getCartItems()
	}

	fun getCartItems() {
		Log.d(TAG, "Getting Cart Items")
		_dataStatus.value = StoreDataStatus.LOADING
		viewModelScope.launch {
			val deferredRes = async { authRepository.getUserData(currentUser!!) }
			val userRes = deferredRes.await()
			if (userRes is Success) {
				val uData = userRes.data
				if (uData != null) {
					_cartItems.value = uData.cart
					val priceRes = async { getAllProductsInCart() }
					priceRes.await()
					Log.d(TAG, "Getting Cart Items: Success ${_priceList.value}")
				} else {
					_cartItems.value = emptyList()
					_dataStatus.value = StoreDataStatus.ERROR
					Log.d(TAG, "Getting Cart Items: User Not Found")
				}
			} else {
				_cartItems.value = emptyList()
				_dataStatus.value = StoreDataStatus.ERROR
				Log.d(TAG, "Getting Cart Items: Error Occurred")
			}
		}
	}

	fun getUserAddresses() {
		Log.d(TAG, "Getting Addresses")
		_dataStatus.value = StoreDataStatus.LOADING
		viewModelScope.launch {
			val res = authRepository.getAddressesByUserId(currentUser!!)
			if (res is Success) {
				_userAddresses.value = res.data ?: emptyList()
				_dataStatus.value = StoreDataStatus.DONE
				Log.d(TAG, "Getting Addresses: Success")
			} else {
				_userAddresses.value = emptyList()
				_dataStatus.value = StoreDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "Getting Addresses: Error Occurred, ${res.exception.message}")
			}
		}
	}

	fun deleteAddress(addressId: String) {
		viewModelScope.launch {
			val delRes = async { authRepository.deleteAddressById(addressId, currentUser!!) }
			when (val res = delRes.await()) {
				is Success -> Log.d(TAG, "onDeleteAddress: Success")
				is Error -> Log.d(TAG, "onDeleteAddress: Error, ${res.exception}")
				else -> Log.d(TAG, "onDeleteAddress: Some error occurred!")
			}
		}
	}

	fun getItemsPriceTotal(): Double {
		return _priceList.value?.values?.sum() ?: 0.0
	}

	fun toggleLikeProduct(productId: String) {
		Log.d(TAG, "toggling Like: $productId")
	}

	private suspend fun getAllProductsInCart() {
		viewModelScope.launch {
			_dataStatus.value = StoreDataStatus.LOADING
			val priceMap = mutableMapOf<String, Double>()
			val proList = mutableListOf<Product>()
			var res = true
			_cartItems.value?.let { itemList ->
				itemList.forEach label@{ item ->
					val productDeferredRes = async {
						productsRepository.getProductById(item.productId, true)
					}
					val proRes = productDeferredRes.await()
					if (proRes is Success) {
						val proData = proRes.data
						proList.add(proData)
						priceMap[item.itemId] = proData.price
					} else {
						res = false
						return@label
					}
				}
			}
			if (!res) {
				_dataStatus.value = StoreDataStatus.ERROR
			} else {
				_dataStatus.value = StoreDataStatus.DONE
			}
			_priceList.value = priceMap
			_cartProducts.value = proList
		}
	}
}