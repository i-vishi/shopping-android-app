package com.vishalgaur.shoppingapp.ui.loginSignup

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.vishalgaur.shoppingapp.EMAIL_ERROR_TEXT
import com.vishalgaur.shoppingapp.MOB_ERROR_TEXT
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.clickClickableSpan
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupFragmentTest {
	private lateinit var signUpScenario: FragmentScenario<SignupFragment>
	private lateinit var navController: NavController
	private lateinit var sessionManager: ShoppingAppSessionManager

	@Before
	fun setUp() {
		sessionManager = ShoppingAppSessionManager(ApplicationProvider.getApplicationContext())
		sessionManager.logoutFromSession()
		signUpScenario = launchFragmentInContainer(themeResId = R.style.Theme_ShoppingApp)
		navController = TestNavHostController(ApplicationProvider.getApplicationContext())

		runOnUiThread {
			navController.setGraph(R.navigation.signup_nav_graph)
			signUpScenario.onFragment {
				Navigation.setViewNavController(it.requireView(), navController)
			}
		}
	}

	@Test
	fun useAppContext() {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		assertEquals("com.vishalgaur.shoppingapp", context.packageName)
	}

	@Test
	fun userCanEnterName() {
		insertInNameEditText("Vishal Gaur  ")
	}

	@Test
	fun userCanEnterMobile() {
		insertInMobileEditText("8976527465")
	}

	@Test
	fun userCanEnterEmail() {
		insertInEmailEditText("  weuiyjkh@ujhyew.dciwe")
	}

	@Test
	fun userCanEnterPassword() {
		insertInPwdEditText("dh239048fy")
	}

	@Test
	fun userCanEnterInCnfPassword() {
		insertInCnfPwdEditText("con34uyf98")
	}

	@Test
	fun userCanClickTermsSwitch() {
		clickTermsSwitch()
	}

	@Test
	fun userCanClickSellerSwitch() {
		clickSellerSwitch()
	}

	@Test
	fun userCanClickSignUp() {
		clickSignUpButton()
	}

	@Test
	fun userCanClickLogInText() {
		clickLoginText()
	}

	@Test
	fun onLoginClick_navigateToLoginFragment() {
		clickLoginText()
		assertEquals(navController.currentDestination?.id, R.id.LoginFragment)
	}

	@Test
	fun onSignUp_emptyForm_showsError() {
		clickSignUpButton()

		onView(withId(R.id.signup_error_text_view)).check(matches(isDisplayed()))
	}

	@Test
	fun onSignUp_invalidEmail_showsEmailError() {
		insertInNameEditText("Vishal Gaur ")
		insertInMobileEditText("8976527465  ")
		insertInEmailEditText("  weuiyjyew.dciwe")
		insertInPwdEditText("dh239048fy")
		insertInCnfPwdEditText("dh239048fy")
		clickTermsSwitch()
		clickSignUpButton()

		onView(withId(R.id.signup_email_edit_text)).check(matches(hasErrorText(`is`(EMAIL_ERROR_TEXT))))
	}

	@Test
	fun onSignUp_invalidMobile_showsMobileError() {
		insertInNameEditText("Vishal Gaur ")
		insertInMobileEditText("86527465  ")
		insertInEmailEditText("  weuiyj@yew.dciwe")
		insertInPwdEditText("dh239048fy")
		insertInCnfPwdEditText("dh239048fy")
		clickTermsSwitch()
		clickSignUpButton()

		onView(withId(R.id.signup_mobile_edit_text)).check(matches(hasErrorText(`is`(MOB_ERROR_TEXT))))
	}

	@Test
	fun onSignUp_notAcceptedTerms_showsError() {
		insertInNameEditText("Vishal Gaur ")
		insertInMobileEditText("8652744565  ")
		insertInEmailEditText("  weuiyj@yew.dciwe")
		insertInPwdEditText("dh239048fy")
		insertInCnfPwdEditText("dh239048fy")
		clickSignUpButton()

		onView(withId(R.id.signup_error_text_view)).check(matches(withText("Accept the Terms.")))
	}

	@Test
	fun onSignUp_notSamePasswords_showsError() {
		insertInNameEditText("Vishal Gaur ")
		insertInMobileEditText("8652744565  ")
		insertInEmailEditText("  weuiyj@yew.dciwe")
		insertInPwdEditText("dh2398fy")
		insertInCnfPwdEditText("dh239048fy")
		clickTermsSwitch()
		clickSignUpButton()

		onView(withId(R.id.signup_error_text_view)).check(matches(withText("Both passwords are not same!")))
	}

	@Test
	fun onSignUp_validForm_showsNoError() {
		Intents.init()
		insertInNameEditText("Vishal Gaur ")
		insertInMobileEditText("8652744565  ")
		insertInEmailEditText("  weuiyj@yew.dciwe")
		insertInPwdEditText("dh2398fy")
		insertInCnfPwdEditText("dh2398fy")
		clickTermsSwitch()
		clickSignUpButton()

		intended(hasComponent(OtpActivity::class.java.name))
	}


	private fun insertInNameEditText(name: String) =
		onView(withId(R.id.signup_name_edit_text)).perform(scrollTo(), clearText(), typeText(name))

	private fun insertInMobileEditText(phone: String) =
		onView(withId(R.id.signup_mobile_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(phone)
		)

	private fun insertInEmailEditText(email: String) =
		onView(withId(R.id.signup_email_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(email)
		)

	private fun insertInPwdEditText(pwd: String) =
		onView(withId(R.id.signup_password_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(pwd)
		)

	private fun insertInCnfPwdEditText(pwd2: String) =
		onView(withId(R.id.signup_cnf_password_edit_text)).perform(
			scrollTo(),
			clearText(),
			typeText(pwd2)
		)

	private fun clickTermsSwitch() =
		onView(withId(R.id.signup_policy_switch)).perform(scrollTo(), click())

	private fun clickSellerSwitch() =
		onView(withId(R.id.signup_seller_switch)).perform(scrollTo(), click())

	private fun clickSignUpButton() =
		onView(withId(R.id.signup_signup_btn)).perform(scrollTo(), click())

	private fun clickLoginText() =
		onView(withId(R.id.signup_login_text_view)).perform(
			scrollTo(),
			clickClickableSpan("Log In")
		)

}