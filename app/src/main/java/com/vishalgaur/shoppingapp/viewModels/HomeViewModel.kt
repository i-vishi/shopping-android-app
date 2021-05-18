package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.vishalgaur.shoppingapp.ShoppingApplication
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {

	private val productsRepository =
		(application.applicationContext as ShoppingApplication).productsRepository
	private val authRepository =
		(application.applicationContext as ShoppingApplication).authRepository

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)
	private val currentUser = sessionManager.getUserIdFromSession()
	val isUserASeller = sessionManager.isUserSeller()

	private var _products = MutableLiveData<List<Product>>()
	val products: LiveData<List<Product>> get() = _products

	private lateinit var _allProducts: MutableLiveData<List<Product>>
	val allProducts: LiveData<List<Product>> get() = _allProducts

	private var _userProducts = MutableLiveData<List<Product>>()
	val userProducts: LiveData<List<Product>> get() = _userProducts

	private var _userLikes = MutableLiveData<List<String>>()
	val userLikes: LiveData<List<String>> get() = _userLikes

	private var _filterCategory = MutableLiveData("All")
	val filterCategory: LiveData<String> get() = _filterCategory

	private val _storeDataStatus = MutableLiveData<StoreDataStatus>()
	val storeDataStatus: LiveData<StoreDataStatus> get() = _storeDataStatus

	init {
		viewModelScope.launch {
			authRepository.hardRefreshUserData()
		}
		if (isUserASeller)
			getProductsByOwner()
		else
			getProducts()
		getUserLikes()
	}

	fun setDataLoaded() {
		_storeDataStatus.value = StoreDataStatus.DONE
	}

	fun isProductLiked(productId: String): Boolean {
		return _userLikes.value?.contains(productId) == true
	}

	fun toggleLikeByProductId(productId: String) {

	}

	fun isProductInCart(productId: String): Boolean {
		return false
	}

	fun toggleProductInCart(product: Product) {

	}

	private fun getProducts() {
		_allProducts = Transformations.switchMap(productsRepository.observeProducts()) {
			Log.d(TAG, it.toString())
			getProductsLiveData(it)
		} as MutableLiveData<List<Product>>
		viewModelScope.launch {
			_storeDataStatus.value = StoreDataStatus.LOADING
			val res = async { productsRepository.refreshProducts() }
			res.await()
			Log.d(TAG, "getProductsByOwner: status = ${_storeDataStatus.value}")
		}
	}

	private fun getUserLikes() {
		viewModelScope.launch {
			val res = authRepository.getLikesByUserId(currentUser!!)
			if (res is Success) {
				_userLikes.value = res.data ?: emptyList()
				Log.d(TAG, "Getting Likes: Success")
			} else {
				_userLikes.value = emptyList()
				if (res is Error)
					Log.d(TAG, "Getting Likes: Error, ${res.exception}")
			}
		}
	}

	private fun getProductsLiveData(result: Result<List<Product>?>?): LiveData<List<Product>> {
		val res = MutableLiveData<List<Product>>()
		if (result is Success) {
			Log.d(TAG, "result is success")
			_storeDataStatus.value = StoreDataStatus.DONE
			res.value = result.data ?: emptyList()
		} else {
			Log.d(TAG, "result is not success")
			res.value = emptyList()
			_storeDataStatus.value = StoreDataStatus.ERROR
			if (result is Error)
				Log.d(TAG, "getProductsLiveData: Error Occurred: ${result.exception}")
		}
		return res
	}

	private fun getProductsByOwner() {
		_allProducts =
			Transformations.switchMap(productsRepository.observeProductsByOwner(currentUser!!)) {
				Log.d(TAG, it.toString())
				getProductsLiveData(it)
			} as MutableLiveData<List<Product>>
		viewModelScope.launch {
			_storeDataStatus.value = StoreDataStatus.LOADING
			val res = async { productsRepository.refreshProducts() }
			res.await()
			Log.d(TAG, "getProductsByOwner: status = ${_storeDataStatus.value}")
		}
	}

	fun refreshProducts() {
		getProducts()
	}

	fun filterBySearch(queryText: String) {
		filterProducts(_filterCategory.value!!)
		_products.value = _products.value?.filter { product ->
			product.name.contains(queryText, true) or
					((queryText.toDoubleOrNull() ?: 0.0).compareTo(product.price) == 0)
		}
	}

	fun filterProducts(filterType: String) {
		Log.d(TAG, "filterType is $filterType")
		_filterCategory.value = filterType
		_products.value = when (filterType) {
			"None" -> emptyList()
			"All" -> _allProducts.value
			else -> _allProducts.value?.filter { product ->
				product.category == filterType
			}
		}
	}

	fun deleteProduct(productId: String) {
		viewModelScope.launch {
			val delRes = async { productsRepository.deleteProductById(productId) }
			when (val res = delRes.await()) {
				is Success -> Log.d(TAG, "onDelete: Success")
				is Error -> Log.d(TAG, "onDelete: Error, ${res.exception}")
				else -> Log.d(TAG, "onDelete: Some error occurred!")
			}
		}
	}
}