package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.repository.AuthRepository
import com.vishalgaur.shoppingapp.repository.ProductsRepository
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {
	val authRepository = AuthRepository(application)

	private val productsRepository = ProductsRepository(application)

	lateinit var products: LiveData<List<Product>>

	private val _selectedCategory = MutableLiveData<String>()
	val selectedCategory: LiveData<String> get() = _selectedCategory

	private val _productData = MutableLiveData<Product>()
	private val productData: LiveData<Product> get() = _productData

	init {
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