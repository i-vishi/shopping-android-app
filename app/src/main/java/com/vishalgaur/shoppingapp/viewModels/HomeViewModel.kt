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
import com.vishalgaur.shoppingapp.network.AddProductErrors
import com.vishalgaur.shoppingapp.repository.AuthRepository
import com.vishalgaur.shoppingapp.repository.ProductsRepository
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
import kotlinx.coroutines.async
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

	private val _errorStatus = MutableLiveData<AddProductViewErrors>()
	val errorStatus: LiveData<AddProductViewErrors> get() = _errorStatus

	private val _addProductErrors = MutableLiveData<AddProductErrors?>()
	val addProductErrors :LiveData<AddProductErrors?> get() = _addProductErrors

	private val _productData = MutableLiveData<Product>()
	val productData: LiveData<Product> get() = _productData

	init {
		_errorStatus.value = AddProductViewErrors.NONE
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
		getProductsByOwner()
	}

	private fun getAllProducts() {
		products = productsRepository.getAllProducts()
	}

	private fun getProductsByOwner() {
		userProducts = productsRepository.getAllProductsByOwner(currentUser!!)
	}

	fun submitProduct(name: String, price: Double?, desc: String, sizes: List<Int>, colors: List<String>) {
		if (name.isBlank() || price == null || desc.isBlank() || sizes.isNullOrEmpty() || colors.isNullOrEmpty()) {
			_errorStatus.value = AddProductViewErrors.EMPTY
		} else {
			if (price == 0.0) {
				_errorStatus.value = AddProductViewErrors.ERR_PRICE_0
			} else {
				_errorStatus.value = AddProductViewErrors.NONE
				val proNum = userProducts.value?.size?.plus(1) ?: 1
				val proId = getProductId(currentUser!!, selectedCategory.value!!, name, proNum.toLong())
				val newProduct = Product(proId, name, currentUser, desc, price, sizes, colors, 0.0)
				_productData.value = newProduct
				Log.d(TAG, "pro = $newProduct")
				insertProduct()
			}
		}
	}

	private fun insertProduct() {
		viewModelScope.launch {
			if (productData.value != null) {
				val res = async{productsRepository.insertProduct(productData.value!!)}
				_addProductErrors.value = res.await()
			} else {
				Log.d(TAG, "Product is Null, Cannot Add Product")
			}
		}
	}
}