package com.humber.artfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.humber.artfinder.ui.navigation.AppNavGraph
import com.humber.artfinder.ui.theme.ArtFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtFinderTheme {
                val navController = rememberNavController()
                
                // Navigation Graph
                AppNavGraph(navController = navController)
            }
        }
    }
}
