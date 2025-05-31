package com.example.tragolisto.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tragolisto.chat.ChatScreen
import com.example.tragolisto.creations.CreationsScreen
import com.example.tragolisto.favorites.FavoritesScreen
import com.example.tragolisto.home.HomeScreen
import com.example.tragolisto.onboarding.OnboardingScreen
import com.example.tragolisto.party.PartyScreen
import com.example.tragolisto.recipes.RecipesScreen
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Favorites : Screen("favorites")
    object Party : Screen("party")
    object Recipes : Screen("recipes")
    object Creations : Screen("creations")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var userName by remember { mutableStateOf("") }
    var userBirthDate by remember { mutableStateOf<LocalDate?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = { name, birthDate ->
                    userName = name
                    userBirthDate = birthDate
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userName = userName,
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onPartyClick = { navController.navigate(Screen.Party.route) },
                onRecipesClick = { navController.navigate(Screen.Recipes.route) },
                onCreationsClick = { navController.navigate(Screen.Creations.route) }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Party.route) {
            PartyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Recipes.route) {
            RecipesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Creations.route) {
            CreationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 