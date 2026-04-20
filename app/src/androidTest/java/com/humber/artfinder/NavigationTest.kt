package com.humber.artfinder

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.humber.artfinder.ui.navigation.AppNavGraph
import com.humber.artfinder.ui.theme.ArtFinderTheme
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAuthNavigationFlow() {
        composeTestRule.setContent {
            ArtFinderTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }

        // 1. Verify we start at Login Screen (if not already authenticated)
        // If this fails, clear the app data on your emulator
        composeTestRule.onNodeWithText("Welcome to ArtFinder").assertExists()

        // 2. Navigate to Sign Up
        composeTestRule.onNodeWithText("Sign Up", substring = true).performClick()
        
        // 3. Verify we reached Sign Up Screen
        composeTestRule.onNodeWithText("Create Account").assertExists()

        // 4. Navigate back to Login using the "Already have an account?" link
        composeTestRule.onNodeWithText("Login", substring = true).performClick()
        
        // 5. Verify we are back on Login
        composeTestRule.onNodeWithText("Welcome to ArtFinder").assertExists()
    }

    @Test
    fun testSignUpAccessibilityLabels() {
        composeTestRule.setContent {
            ArtFinderTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }

        // Navigate to Sign Up
        composeTestRule.onNodeWithText("Sign Up", substring = true).performClick()

        // Milestone 4: Verify clear labels on all registration inputs
        composeTestRule.onNodeWithText("Full Name").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Verify the button has a click action
        composeTestRule.onNodeWithTag("").assertDoesNotExist() // Dummy check to ensure sync
        composeTestRule.onNodeWithText("Sign Up").assertHasClickAction()
    }

    @Test
    fun testLoginAccessibilityLabels() {
        composeTestRule.setContent {
            ArtFinderTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }

        // Milestone 4: Verify clear labels and placeholders on Login Screen
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        
        // Check for navigation accessibility
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").assertHasClickAction()
    }
}
