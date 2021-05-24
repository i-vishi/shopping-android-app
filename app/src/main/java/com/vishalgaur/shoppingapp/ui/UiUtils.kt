package com.vishalgaur.shoppingapp.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.shoppingapp.data.UserData
import com.vishalgaur.shoppingapp.data.utils.getISOCountriesMap
import com.vishalgaur.shoppingapp.ui.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

enum class SignUpViewErrors { NONE, ERR_EMAIL, ERR_MOBILE, ERR_EMAIL_MOBILE, ERR_EMPTY, ERR_NOT_ACC, ERR_PWD12NS }

enum class LoginViewErrors { NONE, ERR_EMPTY, ERR_MOBILE }

enum class OTPStatus { NONE, CORRECT, WRONG }

enum class AddProductViewErrors { NONE, EMPTY, ERR_PRICE_0 }

enum class AddAddressViewErrors { EMPTY, ERR_FNAME_EMPTY, ERR_LNAME_EMPTY, ERR_STR1_EMPTY, ERR_CITY_EMPTY, ERR_STATE_EMPTY, ERR_ZIP_EMPTY, ERR_ZIP_INVALID, ERR_PHONE_INVALID, ERR_PHONE_EMPTY }

enum class AddItemErrors { ERROR_SIZE, ERROR_COLOR }

class MyOnFocusChangeListener : View.OnFocusChangeListener {
	override fun onFocusChange(v: View?, hasFocus: Boolean) {
		if (v != null) {
			val inputManager =
				v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			if (!hasFocus) {

				inputManager.hideSoftInputFromWindow(v.windowToken, 0)
			} else {
				inputManager.toggleSoftInputFromWindow(v.windowToken, 0, 0)

			}
		}
	}
}

fun <T> throttleLatest(
	intervalMs: Long = 300L,
	coroutineScope: CoroutineScope,
	destinationFunction: (T) -> Unit
): (T) -> Unit {
	var throttleJob: Job? = null
	var latestParam: T
	return { param: T ->
		latestParam = param
		if (throttleJob?.isCompleted != false) {
			throttleJob = coroutineScope.launch {
				delay(intervalMs)
				latestParam.let(destinationFunction)
			}
		}
	}
}

fun <T> debounce(
	waitMs: Long = 300L,
	coroutineScope: CoroutineScope,
	destinationFunction: (T) -> Unit
): (T) -> Unit {
	var debounceJob: Job? = null
	return { param: T ->
		debounceJob?.cancel()
		debounceJob = coroutineScope.launch {
			delay(waitMs)
			destinationFunction(param)
		}
	}
}

class DotsIndicatorDecoration(
	private val radius: Float,
	private val indicatorItemPadding: Float,
	private val indicatorHeight: Int,
	@ColorInt private val colorInactive: Int,
	@ColorInt private val colorActive: Int
) : RecyclerView.ItemDecoration() {

	private val inactivePaint = Paint()
	private val activePaint = Paint()

	init {
		val width = Resources.getSystem().displayMetrics.density * 1
		inactivePaint.apply {
			strokeCap = Paint.Cap.ROUND
			strokeWidth = width
			style = Paint.Style.STROKE
			isAntiAlias = true
			color = colorInactive
		}

		activePaint.apply {
			strokeCap = Paint.Cap.ROUND
			strokeWidth = width
			style = Paint.Style.FILL_AND_STROKE
			isAntiAlias = true
			color = colorActive
		}
	}

	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
		super.onDrawOver(c, parent, state)

		val adapter = parent.adapter ?: return

		val itemCount = adapter.itemCount

		val totalLength: Float = (radius * 2 * itemCount)
		val padBWItems = max(0, itemCount - 1) * indicatorItemPadding
		val indicatorTotalWidth = totalLength + padBWItems
		val indicatorStartX = (parent.width - indicatorTotalWidth) / 2F

		val indicatorPosY = parent.height - indicatorHeight / 2F

		drawInactiveDots(c, indicatorStartX, indicatorPosY, itemCount)

		val activePos: Int =
			(parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
		if (activePos == RecyclerView.NO_POSITION) {
			return
		}

		val activeChild =
			(parent.layoutManager as LinearLayoutManager).findViewByPosition(activePos)
				?: return

		drawActiveDot(c, indicatorStartX, indicatorPosY, activePos)


	}

	private fun drawInactiveDots(
		c: Canvas,
		indicatorStartX: Float,
		indicatorPosY: Float,
		itemCount: Int
	) {
		val w = radius * 2 + indicatorItemPadding
		var st = indicatorStartX + radius
		for (i in 1..itemCount) {
			c.drawCircle(st, indicatorPosY, radius, inactivePaint)
			st += w
		}
	}

	private fun drawActiveDot(
		c: Canvas,
		indicatorStartX: Float,
		indicatorPosY: Float,
		highlightPos: Int
	) {
		val w = radius * 2 + indicatorItemPadding
		val highStart = indicatorStartX + radius + w * highlightPos
		c.drawCircle(highStart, indicatorPosY, radius, activePaint)
	}

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State
	) {
		super.getItemOffsets(outRect, view, parent, state)
		outRect.bottom = indicatorHeight
	}

}

internal fun launchHome(context: Context) {
	val homeIntent = Intent(context, MainActivity::class.java)
	homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	context.startActivity(homeIntent)
}

internal fun getCompleteAddress(address: UserData.Address): String {
	return if (address.streetAddress2.isBlank()) {
		"${address.streetAddress}, ${address.city}, ${address.state} - ${address.zipCode}, ${getISOCountriesMap()[address.countryISOCode]}"
	} else {
		"${address.streetAddress}, ${address.streetAddress2}, ${address.city}, ${address.state} - ${address.zipCode}, ${getISOCountriesMap()[address.countryISOCode]}"
	}
}