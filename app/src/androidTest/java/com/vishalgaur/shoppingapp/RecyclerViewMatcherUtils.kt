package com.vishalgaur.shoppingapp

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`


fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?> {
	return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
		override fun describeTo(description: Description) {
			description.appendText("has item at position $position: ")
			itemMatcher.describeTo(description)
		}

		override fun matchesSafely(view: RecyclerView): Boolean {
			val viewHolder = view.findViewHolderForAdapterPosition(position)
				?: // has no item on such position
				return false
			return itemMatcher.matches(viewHolder.itemView)
		}
	}
}

abstract class RecyclerViewItemAction: ViewAction {
	override fun getConstraints(): Matcher<View>? {
		return null
	}

	override fun getDescription(): String {
		return "Action on a specific Button"
	}
}

class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
	override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
		if (noViewFoundException != null) {
			throw noViewFoundException
		}
		val recyclerView = view as RecyclerView
		val adapter = recyclerView.adapter
		assertThat(adapter!!.itemCount, `is`(expectedCount))
	}
}