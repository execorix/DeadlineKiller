package com.bignerdranch.android.deadlinetimer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bignerdranch.android.deadlinetimer.ui.auth.AuthScreen
import com.bignerdranch.android.deadlinetimer.ui.theme.DeadlineTimerTheme

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_elementsAreDisplayed() {
        composeTestRule.setContent {
            DeadlineTimerTheme {
                AuthScreen(onAuthSuccess = {} as (String, String, String) -> Unit)
            }
        }
        composeTestRule.onNodeWithTag("email_input").assertIsDisplayed()
        composeTestRule.onNodeWithText("Войти").assertIsDisplayed()
    }
}