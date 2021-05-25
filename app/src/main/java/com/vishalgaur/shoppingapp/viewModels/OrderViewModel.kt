package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.ShoppingApplication
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getRandomString
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "OrderViewModel"

class OrderViewModel(application: Application) : AndroidViewModel(application) {

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)
	private val currentUser = sessionManager.getUserIdFromSession()

	private val authRepository = (application as ShoppingApplication).authRepository
	private val productsRepository = (application as ShoppingApplication).productsRepository

	private val _userAddresses = MutableLiveData<List<UserData.Address>>()
	val userAddresses: LiveData<List<UserData.Address>> get() = _userAddresses

	private val _userLikes = MutableLiveData<List<String>>()
	val userLikes: LiveData<List<String>> get() = _userLikes

	private val _cartItems = MutableLiveData<List<UserData.CartItem>>()
	val cartItems: LiveData<List<UserData.CartItem>> get() = _cartItems

	private val _priceList = MutableLiveData<Map<String, Double>>()
	val priceList: LiveData<Map<String, Double>> get() = _priceList

	private val _cartProducts = MutableLiveData<List<Product>>()
	val cartProducts: LiveData<List<Product>> get() = _cartProducts

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	private val _orderStatus = MutableLiveData<StoreDataStatus>()
	val orderStatus: LiveData<StoreDataStatus> get() = _orderStatus

	private val _selectedAddress = MutableLiveData<String>()
	private val _selectedPaymentMethod = MutableLiveData<String>()
	private val newOrderData = MutableLiveData<UserData.OrderItem>()

	init {
		viewModelScope.launch {
			authRepository.hardRefreshUserData()
			getUserLikes()
		}
	}

	fun getCartItems() {
		Log.d(TAG, "Getting Cart Items")
		_dataStatus.value = StoreDataStatus.LOADING
		viewModelScope.launch {
			val deferredRes = async {
				authRepository.hardRefreshUserData()
				authRepository.getUserData(currentUser!!)
			}
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

	private fun getUserLikes() {
		Log.d(TAG, "Getting Likes")
		_dataStatus.value = StoreDataStatus.LOADING
		viewModelScope.launch {
			val res = authRepository.getLikesByUserId(currentUser!!)
			if (res is Success) {
				_userLikes.value = res.data ?: emptyList()
				_dataStatus.value = StoreDataStatus.DONE
				Log.d(TAG, "Getting Likes: Success")
			} else {
				_userLikes.value = emptyList()
				_dataStatus.value = StoreDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "Getting Likes: Error Occurred, ${res.exception.message}")
			}
		}
	}

	fun deleteAddress(addressId: String) {
		viewModelScope.launch {
			val delRes = async { authRepository.deleteAddressById(addressId, currentUser!!) }
			when (val res = delRes.await()) {
				is Success -> {
					Log.d(TAG, "onDeleteAddress: Success")
					val addresses = _userAddresses.value?.toMutableList()
					addresses?.let {
						val pos =
							addresses.indexOfFirst { address -> address.addressId == addressId }
						if (pos >= 0)
							it.removeAt(pos)
						_userAddresses.value = it
					}
				}
				is Error -> Log.d(TAG, "onDeleteAddress: Error, ${res.exception}")
				else -> Log.d(TAG, "onDeleteAddress: Some error occurred!")
			}
		}
	}

	fun getItemsPriceTotal(): Double {
		var totalPrice = 0.0
		_priceList.value?.forEach { (itemId, price) ->
			totalPrice += price * (_cartItems.value?.find { it.itemId == itemId }?.quantity ?: 1)
		}
		return totalPrice
	}

	fun toggleLikeProduct(productId: String) {
		Log.d(TAG, "toggling Like")
		viewModelScope.launch {
//			_dataStatus.value = StoreDataStatus.LOADING
			val isLiked = _userLikes.value?.contains(productId) == true
			val allLikes = _userLikes.value?.toMutableList() ?: mutableListOf()
			val deferredRes = async {
				if (isLiked) {
					authRepository.removeProductFromLikes(productId, currentUser!!)
				} else {
					authRepository.insertProductToLikes(productId, currentUser!!)
				}
			}
			val res = deferredRes.await()
			if (res is Success) {
				if (isLiked) {
					allLikes.remove(productId)
				} else {
					allLikes.add(productId)
				}
				_userLikes.value = allLikes
				_dataStatus.value = StoreDataStatus.DONE
			} else {
				_dataStatus.value = StoreDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "onUpdateQuantity: Error Occurred: ${res.exception.message}")
			}
		}
	}

	fun getItemsCount(): Int {
		var totalCount = 0
		_cartItems.value?.forEach {
			totalCount += it.quantity
		}
		return totalCount
	}

	fun setQuantityOfItem(itemId: String, value: Int) {
		viewModelScope.launch {
//			_dataStatus.value = StoreDataStatus.LOADING
			var cartList: MutableList<UserData.CartItem>
			_cartItems.value?.let { items ->
				val item = items.find { it.itemId == itemId }
				val itemPos = items.indexOfFirst { it.itemId == itemId }
				cartList = items.toMutableList()
				if (item != null) {
					item.quantity = item.quantity + value
					val deferredRes = async {
						authRepository.updateCartItemByUserId(item, currentUser!!)
					}
					val res = deferredRes.await()
					if (res is Success) {
						cartList[itemPos] = item
						_cartItems.value = cartList
						_dataStatus.value = StoreDataStatus.DONE
					} else {
						_dataStatus.value = StoreDataStatus.ERROR
						if (res is Error)
							Log.d(TAG, "onUpdateQuantity: Error Occurred: ${res.exception.message}")
					}
				}
			}
		}
	}

	fun deleteItemFromCart(itemId: String) {
		viewModelScope.launch {
//			_dataStatus.value = StoreDataStatus.LOADING
			var cartList: MutableList<UserData.CartItem>
			_cartItems.value?.let { items ->
				val itemPos = items.indexOfFirst { it.itemId == itemId }
				cartList = items.toMutableList()
				val deferredRes = async {
					authRepository.deleteCartItemByUserId(itemId, currentUser!!)
				}
				val res = deferredRes.await()
				if (res is Success) {
					cartList.removeAt(itemPos)
					_cartItems.value = cartList
					val priceRes = async { getAllProductsInCart() }
					priceRes.await()
				} else {
					_dataStatus.value = StoreDataStatus.ERROR
					if (res is Error)
						Log.d(TAG, "onUpdateQuantity: Error Occurred: ${res.exception.message}")
				}
			}
		}
	}

	fun setSelectedAddress(addressId: String) {
		_selectedAddress.value = addressId
	}

	fun setSelectedPaymentMethod(method: String) {
		_selectedPaymentMethod.value = method
	}

	fun finalizeOrder() {
		_orderStatus.value = StoreDataStatus.LOADING
		val deliveryAddress =
			_userAddresses.value?.find { it.addressId == _selectedAddress.value }
		val paymentMethod = _selectedPaymentMethod.value
		val currDate = Date()
		val orderId = getRandomString(6, currDate.time.toString(), 1)
		val items = _cartItems.value
		val itemPrices = _priceList.value
		val shippingCharges = 0.0
		if (deliveryAddress != null && paymentMethod != null && !items.isNullOrEmpty() && !itemPrices.isNullOrEmpty()) {
			val newOrder = UserData.OrderItem(
				orderId,
				items,
				itemPrices,
				deliveryAddress,
				shippingCharges,
				paymentMethod,
				currDate,
			)
			newOrderData.value = newOrder
			insertOrder()
		} else {
			Log.d(TAG, "orFinalizeOrder: Error, data null or empty")
			_orderStatus.value = StoreDataStatus.ERROR
		}
	}

	private fun insertOrder() {
		viewModelScope.launch {
			if (newOrderData.value != null) {
				_orderStatus.value = StoreDataStatus.LOADING
				val deferredRes = async {
					authRepository.placeOrder(newOrderData.value!!, currentUser!!)
				}
				val res = deferredRes.await()
				if (res is Success) {
					Log.d(TAG, "onInsertOrder: Success")
					_cartItems.value = emptyList()
					_cartProducts.value = emptyList()
					_priceList.value = emptyMap()
					_orderStatus.value = StoreDataStatus.DONE
				} else {
					_orderStatus.value = StoreDataStatus.ERROR
					if (res is Error) {
						Log.d(TAG, "onInsertOrder: Error, ${res.exception}")
					}
				}
			} else {
				Log.d(TAG, "orInsertOrder: Error, newProduct Null")
				_orderStatus.value = StoreDataStatus.ERROR
			}
		}
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
			_priceList.value = priceMap
			_cartProducts.value = proList
			if (!res) {
				_dataStatus.value = StoreDataStatus.ERROR
			} else {
				_dataStatus.value = StoreDataStatus.DONE
			}
		}
	}
}