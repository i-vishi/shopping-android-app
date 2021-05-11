package com.vishalgaur.shoppingapp.data.utils

import java.util.*

enum class SignUpErrors { NONE, SERR }

enum class LogInErrors { NONE, LERR }

enum class AddProductErrors { NONE, ERR_ADD, ERR_ADD_IMG, ADDING }

enum class AddAddressStatus { DONE, ERR_ADD, ADDING }

enum class UserType { CUSTOMER, SELLER }

enum class StoreDataStatus { LOADING, ERROR, DONE }

fun getISOCountriesMap(): Map<String, String> {
	val result = mutableMapOf<String, String>()
	val isoCountries = Locale.getISOCountries()
	val countriesList = isoCountries.map { isoCountry ->
		result[isoCountry] = Locale("", isoCountry).displayCountry
	}
	return result
}