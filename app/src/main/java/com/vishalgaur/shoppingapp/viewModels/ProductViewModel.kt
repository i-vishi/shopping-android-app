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
import com.vishalgaur.shoppingapp.data.utils.AddObjectStatus
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.ui.AddItemErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "ProductViewModel"

class ProductViewModel(private val productId: String, application: Application) :
	AndroidViewModel(application) {

	private val _productData = MutableLiveData<Product?>()
	val productData: LiveData<Product?> get() = _productData

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	private val _errorStatus = MutableLiveData<List<AddItemErrors>>()
	val errorStatus: LiveData<List<AddItemErrors>> get() = _errorStatus

	private val _addItemStatus = MutableLiveData<AddObjectStatus?>()
	val addItemStatus: LiveData<AddObjectStatus?> get() = _addItemStatus

	private val _isLiked = MutableLiveData<Boolean>()
	val isLiked: LiveData<Boolean> get() = _isLiked

	private val _isItemInCart = MutableLiveData<Boolean>()
	val isItemInCart: LiveData<Boolean> get() = _isItemInCart

	private val productsRepository =(application as ShoppingApplication).productsRepository
	private val authRepository = (application as ShoppingApplication).authRepository
	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)
	private val currentUserId = sessionManager.getUserIdFromSession()

	init {
		_isLiked.value = false
		_errorStatus.value = emptyList()
		viewModelScope.launch {
			authRepository.hardRefreshUserData()
			Log.d(TAG, "init: productId: $productId")
			getProductDetails()
			checkIfInCart()
			setLike()
		}

	}

	private fun getProductDetails() {
		viewModelScope.launch {
			_dataStatus.value = StoreDataStatus.LOADING
			try {
				Log.d(TAG, "getting product Data")
				val res = productsRepository.getProductById(productId)
				if (res is Success) {
					_productData.value = res.data
					_dataStatus.value = StoreDataStatus.DONE
				} else if (res is Error) {
					throw Exception("Error getting product")
				}
			} catch (e: Exception) {
				_productData.value = Product()
				_dataStatus.value = StoreDataStatus.ERROR
			}
		}
	}

	fun setLike() {
		viewModelScope.launch {
			val res = authRepository.getLikesByUserId(currentUserId!!)
			if (res is Success) {
				val userLikes = res.data ?: emptyList()
				_isLiked.value = userLikes.contains(productId)
				Log.d(TAG, "Setting Like: Success, value = ${_isLiked.value}, ${res.data?.size}")
			} else {
				if (res is Error)
					Log.d(TAG, "Getting Likes: Error Occurred, ${res.exception.message}")
			}
		}
	}

	fun toggleLikeProduct() {
		Log.d(TAG, "toggling Like")
		viewModelScope.launch {
			val deferredRes = async {
				if (_isLiked.value == true) {
					authRepository.removeProductFromLikes(productId, currentUserId!!)
				} else {
					authRepository.insertProductToLikes(productId, currentUserId!!)
				}
			}
			val res = deferredRes.await()
			if (res is Success) {
				_isLiked.value = !_isLiked.value!!
			} else{
				if(res is Error)
					Log.d(TAG, "Error toggling like, ${res.exception}")
			}
		}
	}

	fun isSeller() = sessionManager.isUserSeller()

	fun checkIfInCart() {
		viewModelScope.launch {
			val deferredRes = async { authRepository.getUserData(currentUserId!!) }
			val userRes = deferredRes.await()
			if (userRes is Success) {
				val uData = userRes.data
				if (uData != null) {
					val cartList = uData.cart
					val idx = cartList.indexOfFirst { it.productId == productId }
					_isItemInCart.value = idx >= 0
					Log.d(TAG, "Checking in Cart: Success, value = ${_isItemInCart.value}, ${cartList.size}")
				} else {
					_isItemInCart.value = false
				}
			} else {
				_isItemInCart.value = false
			}
		}
	}

	fun addToCart(size: Int?, color: String?) {
		val errList = mutableListOf<AddItemErrors>()
		if (size == null) errList.add(AddItemErrors.ERROR_SIZE)
		if (color.isNullOrBlank()) errList.add(AddItemErrors.ERROR_COLOR)

		if (errList.isEmpty()) {
			val itemId = UUID.randomUUID().toString()
			val newItem = UserData.CartItem(
				itemId, productId, productData.value!!.owner, 1, color, size
			)
			insertCartItem(newItem)
		}
	}

	private fun insertCartItem(item: UserData.CartItem) {
		viewModelScope.launch {
			_addItemStatus.value = AddObjectStatus.ADDING
			val deferredRes = async {
				authRepository.insertCartItemByUserId(item, currentUserId!!)
			}
			val res = deferredRes.await()
			if (res is Success) {
				authRepository.hardRefreshUserData()
				Log.d(TAG, "onAddItem: Success")
				_addItemStatus.value = AddObjectStatus.DONE
			} else {
				_addItemStatus.value = AddObjectStatus.ERR_ADD
				if (res is Error) {
					Log.d(TAG, "onAddItem: Error, ${res.exception.message}")
				}
			}
		}
	}
}