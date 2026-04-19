package com.humber.artfinder

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.humber.artfinder.ui.navigation.AppNavGraph
import com.humber.artfinder.ui.theme.ArtFinderTheme
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginToSignUpFlow() {
        composeTestRule.setContent {
            ArtFinderTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }

        // Check if we are on Login Screen (assuming it has a "Login" title or button)
        composeTestRule.onNodeWithText("Login").assertExists()

        // Find the sign up navigation text/button and click it
        // Note: Using substring match in case text is "Don't have an account? Sign Up"
        composeTestRule.onNodeWithText("Sign Up", substring = true).performClick()

        // Check if we navigated to Sign Up Screen
        composeTestRule.onNodeWithText("Create Account").assertExists()
    }
}
