package com.vishalgaur.shoppingapp

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
	@Test
	fun checkEmail_empty_returnsFalse() {
		val email = ""
		val result = isEmailValid(email)

		assertEquals(result, false)
	}

	@Test
	fun checkEmail_invalid_returnsFalse() {
		val email1 = "vishalgaur"
		val email2 = "vishalgaur.com"
		val email3 = "vishalgaur@google"
		val result1 = isEmailValid(email1)
		val result2 = isEmailValid(email2)
		val result3 = isEmailValid(email3)

		assertEquals(result1, false)
		assertEquals(result2, false)
		assertEquals(result3, false)
	}

	@Test
	fun checkEmail_valid_returnsTrue() {
		val email1 = "    vishalgaur@google.com"
		val email2 = "vishal123@gmail.co.in     "
		val email3 = "rr.vishalgaur@gmail.com"
		val result1 = isEmailValid(email1)
		val result2 = isEmailValid(email2)
		val result3 = isEmailValid(email3)

		assertEquals(result1, true)
		assertEquals(result2, true)
		assertEquals(result3, true)
	}

	@Test
	fun checkPhone_empty_returnsFalse() {
		val phone = ""
		val result = isPhoneValid(phone)

		assertEquals(result, false)
	}

	@Test
	fun checkPhone_invalid_returnsFalse() {
		val phone1 = "1968743574694865"
		val phone2 = "  1111  "
		val phone3 = "2454678910"
		val result1 = isPhoneValid(phone1)
		val result2 = isPhoneValid(phone2)
		val result3 = isPhoneValid(phone3)

		assertEquals(result1, false)
		assertEquals(result2, false)
		assertEquals(result3, false)
	}

	@Test
	fun checkPhone_valid_returnsTrue() {
		val phone1 = "9876543210"
		val phone2 = "    6985741526"
		val phone3 = "8989895858   "
		val result1 = isPhoneValid(phone1)
		val result2 = isPhoneValid(phone2)
		val result3 = isPhoneValid(phone3)

		assertEquals(result1, true)
		assertEquals(result2, true)
		assertEquals(result3, true)
	}

	@Test
	fun getRandomString_hasExpectedLength() {
		val result1 = getRandomString(10, "odweuih", 10)
		val result2 = getRandomString(10, "", 5)
		assertThat(result1.length, `is`(27))
		assertThat(result2.length, `is`(15))
	}

	@Test
	fun getProductId_hasExpectedSubStrings() {
		val result = getProductId("soewifnc9we48yf0", "shoes")
		val subs = result.split("-")
		assert(subs.size >= 3)
	}

	@Test
	fun getOfferPercentage_zeroZero_returnsZero() {
		val result = getOfferPercentage(0.0, 0.0)
		assertThat(result, `is`(0))
	}

	@Test
	fun getOfferPercentage_highLow_returnsZero() {
		val result = getOfferPercentage(90.0, 100.0)
		assertThat(result, `is`(0))
	}

	@Test
	fun getOfferPercentage_lowHigh_returnsOfferInt() {
		val result1 = getOfferPercentage(100.0, 90.5)
		val result2 = getOfferPercentage(100.0, 90.75)
		val result3 = getOfferPercentage(100.0, 90.25)
		assertThat(result1, `is`(10))
		assertThat(result2, `is`(9))
		assertThat(result3, `is`(10))
	}

	@Test
	fun getOfferPercentage_equal_returnsZero() {
		val result = getOfferPercentage(90.5, 90.5)
		assertThat(result, `is`(0))
	}
}