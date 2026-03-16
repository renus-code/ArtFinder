package com.humber.artfinder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.humber.artfinder.data.repository.ArtRepository
import com.humber.artfinder.data.repository.AuthRepository
import com.humber.artfinder.data.repository.UserRepository
import com.humber.artfinder.ui.screens.*
import com.humber.artfinder.ui.viewmodel.*

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    // Repositories
    val authRepo = remember { AuthRepository() }
    val userRepo = remember { UserRepository() }
    val artRepo = remember { ArtRepository() }

    // ViewModels
    val authVM: AuthViewModel = viewModel(factory = AuthVMFactory(authRepo))
    val userVM: UserViewModel = viewModel(factory = UserVMFactory(authRepo, userRepo))
    val artVM: ArtViewModel = viewModel(factory = ArtVMFactory(artRepo))
    
    val startDestination = if (authVM.currentUser != null) "art_list" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authVM,
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { 
                    userVM.fetchProfile()
                    userVM.fetchVisitedArtworks()
                    navController.navigate("art_list") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("signup") {
            SignUpScreen(
                authVM = authVM,
                userVM = userVM,
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate("art_list") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("art_list") {
            ArtListScreen(
                authVM = authVM,
                artVM = artVM,
                userVM = userVM,
                onNavigateToDetails = { id -> navController.navigate("art_details/$id") },
                onNavigateToProfile = { navController.navigate("profile") },
                onSignOut = {
                    authVM.signOut()
                    userVM.clearProfile()
                    navController.navigate("login") {
                        popUpTo("art_list") { inclusive = true }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                userVM = userVM,
                onNavigateToVisitedList = { navController.navigate("visited_list") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("visited_list") {
            VisitedArtListScreen(
                userVM = userVM,
                onNavigateToDetails = { id -> navController.navigate("art_details/$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "art_details/{artworkId}",
            arguments = listOf(navArgument("artworkId") { type = NavType.IntType })
        ) { backStackEntry ->
            val artworkId = backStackEntry.arguments?.getInt("artworkId") ?: 0
            ArtDetailScreen(
                artworkId = artworkId,
                artVM = artVM,
                userVM = userVM,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
