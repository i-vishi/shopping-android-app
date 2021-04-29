package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.network.StoreDataStatus
import com.vishalgaur.shoppingapp.repository.ProductsRepository
import kotlinx.coroutines.launch
import java.lang.Exception

class ProductViewModel(private val productId: String, application: Application) : AndroidViewModel(application) {

	private val _productData = MutableLiveData<Product?>()
	val productData: LiveData<Product?> get() = _productData

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	private val _isLiked = MutableLiveData<Boolean>()
	val isLiked: LiveData<Boolean> get() = _isLiked

	private val productsRepository = ProductsRepository(application)

	init {
		getProductDetails()
		_isLiked.value = false
	}

	private fun getProductDetails() {
		viewModelScope.launch {
			_dataStatus.value = StoreDataStatus.LOADING
			try {
				val res = productsRepository.getProductById(productId)
				_productData.value = res
				_dataStatus.value = StoreDataStatus.DONE
			} catch (e: Exception) {
				_productData.value = Product()
				_dataStatus.value = StoreDataStatus.ERROR
			}
		}
	}

	fun toggleLikeProduct() {
		_isLiked.value = !_isLiked.value!!
	}
}