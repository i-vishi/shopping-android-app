package com.vishalgaur.shoppingapp

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher


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