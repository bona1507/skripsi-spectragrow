package com.pkmkcub.spectragrow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestLoginFlow {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCompleteLoginFlow() {
        // Step 1: Verify the onboarding screen is displayed
        rule.onNodeWithTag("OnboardingPager").assertIsDisplayed()

        // Step 2: Swipe twice to the left to reach the auth card
        rule.onNodeWithTag("OnboardingPager").performTouchInput {
            swipeLeft()
        }
        rule.onNodeWithTag("OnboardingPager").performTouchInput {
            swipeLeft()
        }

        // Step 3: Click the login button to navigate to the login screen
        rule.onNodeWithTag("LoginButton").assertIsDisplayed()
        rule.onNodeWithTag("LoginButton").performClick()

        // Step 4: Perform login action
        rule.onNodeWithText("Surel").assertIsDisplayed()
        rule.onNodeWithText("Surel").performTextInput("adminpunya@gmail.com")
        rule.onNodeWithText("Kata sandi").assertIsDisplayed()
        rule.onNodeWithText("Kata sandi").performTextInput("punyaadmin")
        rule.onNodeWithTag("TermsCheckbox").assertIsDisplayed()
        rule.onNodeWithTag("TermsCheckbox").performClick()
        rule.onNodeWithTag("LoginSubmitButton").assertIsDisplayed()
        rule.onNodeWithTag("LoginSubmitButton").performClick()
    }
}
