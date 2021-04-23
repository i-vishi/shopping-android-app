package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.database.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.getProductId
import com.vishalgaur.shoppingapp.repository.AuthRepository
import com.vishalgaur.shoppingapp.repository.ProductsRepository
import com.vishalgaur.shoppingapp.ui.AddProductErrors
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {
	private val authRepository = AuthRepository(application)

	private val productsRepository = ProductsRepository(application)

	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)

	lateinit var products: LiveData<List<Product>>

	private val currentUser = sessionManager.getUserIdFromSession()

	lateinit var userProducts: LiveData<List<Product>>

	private val _selectedCategory = MutableLiveData<String>()
	val selectedCategory: LiveData<String> get() = _selectedCategory

	private val _errorStatus = MutableLiveData<AddProductErrors>()
	val errorStatus: LiveData<AddProductErrors> get() = _errorStatus

	private val _productData = MutableLiveData<Product>()
	private val productData: LiveData<Product> get() = _productData

	init {
		_errorStatus.value = AddProductErrors.NONE
		viewModelScope.launch {
			refreshData()
		}
	}

	fun setCategory(catName: String) {
		_selectedCategory.value = catName
	}

	private suspend fun refreshData() {
		authRepository.refreshData()
		getAllProducts()
	}

	private fun getAllProducts() {
		products = productsRepository.getAllProducts()
	}

	private fun getProductsByOwner() {
		userProducts = productsRepository.getAllProductsByOwner(currentUser!!)
	}

	fun submitProduct(name: String, price: Double?, desc: String, sizes: List<Int>) {
		if (name.isBlank() || price != null || desc.isBlank() || sizes.isNullOrEmpty()) {
			_errorStatus.value = AddProductErrors.EMPTY
		} else {
			if (price == 0.0) {
				_errorStatus.value = AddProductErrors.ERR_PRICE_0
			} else {
				_errorStatus.value = AddProductErrors.NONE
				val proNum = userProducts.value?.size?.plus(1) ?: 1
				val proId = getProductId(currentUser!!, selectedCategory.value!!, name, proNum.toLong())
			}
		}
	}

	private fun insertProduct() {
		viewModelScope.launch {
			if (productData.value != null) {
				productsRepository.insertProduct(productData.value!!)
			} else {
				Log.d(TAG, "Product is Null, Cannot Add Product")
			}
		}
	}
}