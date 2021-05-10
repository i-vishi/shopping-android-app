package com.vishalgaur.shoppingapp

import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers


internal fun clickClickableSpan(textToClick: CharSequence): ViewAction {
	return object : ViewAction {

		override fun getConstraints(): Matcher<View> {
			return Matchers.instanceOf(TextView::class.java)
		}

		override fun getDescription(): String {
			return "clicking on a ClickableSpan"
		}

		override fun perform(uiController: UiController, view: View) {
			val textView = view as TextView
			val spannableString = textView.text as SpannableString

			if (spannableString.isEmpty()) {
				// TextView is empty, nothing to do
				throw NoMatchingViewException.Builder()
					.includeViewHierarchy(true)
					.withRootView(textView)
					.build()
			}

			// Get the links inside the TextView and check if we find textToClick
			val spans =
				spannableString.getSpans(0, spannableString.length, ClickableSpan::class.java)
			if (spans.isNotEmpty()) {
				var spanCandidate: ClickableSpan
				for (span: ClickableSpan in spans) {
					spanCandidate = span
					val start = spannableString.getSpanStart(spanCandidate)
					val end = spannableString.getSpanEnd(spanCandidate)
					val sequence = spannableString.subSequence(start, end)
					if (textToClick.toString() == sequence.toString()) {
						span.onClick(textView)
						return
					}
				}
			}

			// textToClick not found in TextView
			throw NoMatchingViewException.Builder()
				.includeViewHierarchy(true)
				.withRootView(textView)
				.build()

		}
	}
}