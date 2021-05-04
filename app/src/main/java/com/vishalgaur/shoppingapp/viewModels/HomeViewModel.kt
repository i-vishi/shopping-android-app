package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.vishalgaur.shoppingapp.ERR_UPLOAD
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepository
import com.vishalgaur.shoppingapp.data.utils.AddProductErrors
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import com.vishalgaur.shoppingapp.getProductId
import com.vishalgaur.shoppingapp.ui.AddProductViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val productsRepository = ProductsRepository.getRepository(application)

    private val sessionManager = ShoppingAppSessionManager(application.applicationContext)

    private var _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val currentUser = sessionManager.getUserIdFromSession()
    val isUserASeller = sessionManager.isUserSeller()

    private var _userProducts = MutableLiveData<List<Product>>()
    val userProducts: LiveData<List<Product>> get() = _userProducts

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> get() = _selectedCategory

    private val _storeDataStatus = MutableLiveData<StoreDataStatus>()
    val storeDataStatus: LiveData<StoreDataStatus> get() = _storeDataStatus

    private val _errorStatus = MutableLiveData<AddProductViewErrors>()
    val errorStatus: LiveData<AddProductViewErrors> get() = _errorStatus

    private val _addProductErrors = MutableLiveData<AddProductErrors?>()
    val addProductErrors: LiveData<AddProductErrors?> get() = _addProductErrors

    private val _productData = MutableLiveData<Product>()
    val productData: LiveData<Product> get() = _productData

    init {
        _errorStatus.value = AddProductViewErrors.NONE
        getProducts()
    }

    private fun getProducts() {
        viewModelScope.launch {
            _storeDataStatus.value = StoreDataStatus.LOADING
            launch {
                productsRepository.refreshProducts()
            }
            _products = Transformations.switchMap(productsRepository.observeProducts()) {
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

    fun setCategory(catName: String) {
        _selectedCategory.value = catName
    }

    private fun getProductsByOwner() {
        viewModelScope.launch {
            val res = productsRepository.getAllProductsByOwner(currentUser!!)
            if (res is Success) {
                _userProducts.value = res.data!!
            } else {
                _userProducts.value = emptyList()
            }
        }
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
                val proId =
                    getProductId(currentUser!!, selectedCategory.value!!)
                val newProduct =
                    Product(
                        proId,
                        name.trim(),
                        currentUser,
                        desc.trim(),
                        price,
                        sizes,
                        colors,
                        emptyList(),
                        0.0
                    )
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
                _productData.value?.images = imagesPaths
                if (_productData.value?.images?.isNotEmpty() == true) {
                    if (imagesPaths[0] == ERR_UPLOAD) {
                        Log.d(TAG, "error uploading images")
                        _addProductErrors.value = AddProductErrors.ERR_ADD
                    } else {
                        val res = async { productsRepository.insertProduct(productData.value!!) }
                        res.await()
                        _addProductErrors.value = AddProductErrors.NONE
                    }
                } else {
                    Log.d(TAG, "Product images empty, Cannot Add Product")
                }
            } else {
                Log.d(TAG, "Product is Null, Cannot Add Product")
            }
        }
    }
}