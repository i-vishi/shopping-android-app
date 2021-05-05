package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.Error
import com.vishalgaur.shoppingapp.data.Result.Success
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepository
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {

	private val productsRepository = ProductsRepository.getRepository(application)

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)

	private var _products = MutableLiveData<List<Product>>()
	val products: LiveData<List<Product>> get() = _products

	private var _allProducts = MutableLiveData<List<Product>>()
	val allProducts: LiveData<List<Product>> get() = _allProducts

	private val currentUser = sessionManager.getUserIdFromSession()
	val isUserASeller = sessionManager.isUserSeller()

	private var _userProducts = MutableLiveData<List<Product>>()
	val userProducts: LiveData<List<Product>> get() = _userProducts

	private var filterCategory = MutableLiveData("All")

	private val _storeDataStatus = MutableLiveData<StoreDataStatus>()
	val storeDataStatus: LiveData<StoreDataStatus> get() = _storeDataStatus

	init {
		getProductsByOwner()
	}

	private fun getProducts() {
		viewModelScope.launch {
			_storeDataStatus.value = StoreDataStatus.LOADING
			launch {
				productsRepository.refreshProducts()
			}
			_allProducts = Transformations.switchMap(productsRepository.observeProducts()) {
				getProductsLiveData(it)
			} as MutableLiveData<List<Product>>
		}
	}

	private fun getProductsLiveData(result: Result<List<Product>>?): LiveData<List<Product>> {
		val res = MutableLiveData<List<Product>>()
		if (result is Success) {
			Log.d(TAG, "result is success")
			_storeDataStatus.value = StoreDataStatus.DONE
			res.value = result.data!!
		} else if (result is Error) {
			Log.d(TAG, "result is not success")
			res.value = emptyList()
			_storeDataStatus.value = StoreDataStatus.ERROR
		}
		return res
	}

	private fun getProductsByOwner() {
		viewModelScope.launch {
			_storeDataStatus.value = StoreDataStatus.LOADING
			launch {
				productsRepository.refreshProducts()
			}
			_allProducts =
				Transformations.switchMap(productsRepository.observeProductsByOwner(currentUser!!)) {
					getProductsLiveData(it)
				} as MutableLiveData<List<Product>>
		}
	}

	fun refreshProducts() {
		getProducts()
	}

	fun filterBySearch(queryText: String) {
		filterProducts(filterCategory.value!!)
		_products.value = _products.value?.filter { product ->
			product.name.contains(queryText, true)
		}
	}

	fun filterProducts(filterType: String) {
		Log.d(TAG, "filterType is $filterType")
		filterCategory.value = filterType
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
			productsRepository.deleteProductById(productId)
		}
	}
}