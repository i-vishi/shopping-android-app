package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.database.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.database.products.Product
import com.vishalgaur.shoppingapp.getProductId
import com.vishalgaur.shoppingapp.network.AddProductErrors
import com.vishalgaur.shoppingapp.network.StoreDataStatus
import com.vishalgaur.shoppingapp.repository.AuthRepository
import com.vishalgaur.shoppingapp.repository.ProductsRepository
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val productsRepository = ProductsRepository(application)

    private val sessionManager = ShoppingAppSessionManager(application.applicationContext)

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val currentUser = sessionManager.getUserIdFromSession()

    lateinit var userProducts: LiveData<List<Product>>

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> get() = _selectedCategory

    private val _storeDataStatus = MutableLiveData<StoreDataStatus>()
    val storeDataStatus: LiveData<StoreDataStatus> get() = _storeDataStatus

    private val _errorStatus = MutableLiveData<AddProductViewErrors>()
    val errorStatus: LiveData<AddProductViewErrors> get() = _errorStatus

    private val _addProductErrors = MutableLiveData<AddProductErrors?>()
    val addProductErrors: LiveData<AddProductErrors?> get() = _addProductErrors

    private val _productData = MutableLiveData<Product>()
    private val productData: LiveData<Product> get() = _productData

    init {
        _errorStatus.value = AddProductViewErrors.NONE
        getProducts()
        getProductsByOwner()
    }

    private fun getProducts() {
        viewModelScope.launch {
            _storeDataStatus.value = StoreDataStatus.LOADING
            try {
                val res = productsRepository.getAllProducts()
                Log.d(TAG, "list = $res")
                _products.value = res
                _storeDataStatus.value = StoreDataStatus.DONE
            } catch (e: Exception) {
                _storeDataStatus.value = StoreDataStatus.ERROR
                _products.value = ArrayList()
            }
        }
    }

    fun setCategory(catName: String) {
        _selectedCategory.value = catName
    }

    private fun getProductsByOwner() {
        userProducts = productsRepository.getAllProductsByOwner(currentUser!!)
    }

    fun submitProduct(
        name: String,
        price: Double?,
        desc: String,
        sizes: List<Int>,
        colors: List<String>,
        imgList: List<Uri>
    ) {
        if (name.isBlank() || price == null || desc.isBlank() || sizes.isNullOrEmpty() || colors.isNullOrEmpty() || imgList.isNullOrEmpty()) {
            _errorStatus.value = AddProductViewErrors.EMPTY
        } else {
            if (price == 0.0) {
                _errorStatus.value = AddProductViewErrors.ERR_PRICE_0
            } else {
                _errorStatus.value = AddProductViewErrors.NONE
                val proNum = userProducts.value?.size?.plus(1) ?: 1
                val proId =
                    getProductId(currentUser!!, selectedCategory.value!!, name, proNum.toLong())
                val newProduct =
                    Product(proId, name, currentUser, desc, price, sizes, colors, emptyList(), 0.0)
                _productData.value = newProduct
                Log.d(TAG, "pro = $newProduct")
                insertProduct(imgList)
            }
        }
    }

    fun refreshProducts() {
        getProducts()
    }

    private fun insertProduct(imgList: List<Uri>) {
        viewModelScope.launch {
            if (_productData.value != null) {
                _addProductErrors.value = AddProductErrors.ADDING
                val resImg = async { productsRepository.insertImages(imgList) }
                val imagesPaths = resImg.await()
                Log.d(TAG, "images urls = $imagesPaths")
                _productData.value?.images = imagesPaths
                if (_productData.value?.images?.isNotEmpty() == true) {
                    val res = async { productsRepository.insertProduct(productData.value!!) }
                    _addProductErrors.value = res.await()
                } else {
                    Log.d(TAG, "Product images empty, Cannot Add Product")
                }
            } else {
                Log.d(TAG, "Product is Null, Cannot Add Product")
            }
        }
    }
}