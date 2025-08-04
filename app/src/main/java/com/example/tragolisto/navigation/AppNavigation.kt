package com.example.tragolisto.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tragolisto.R
import com.example.tragolisto.chat.ChatScreen
import com.example.tragolisto.chat.ChatSelectorScreen
import com.example.tragolisto.creations.CreationsScreen
import com.example.tragolisto.data.global.usuarioglobal
import com.example.tragolisto.favorites.FavoritesScreen
import com.example.tragolisto.home.HomeScreen
import com.example.tragolisto.onboarding.OnboardingScreen // Make sure this is a Composable!
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
fun AppNavigation(requiresOnboarding: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val defaultUserName = stringResource(id = R.string.default_user_name)

    val startRoute = if (requiresOnboarding) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                esModoInvitado = usuarioglobal?.idToken == "offline",
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userName = usuarioglobal?.nombre ?: defaultUserName,
                esModoOffline = usuarioglobal?.idToken == "offline",
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onPartyClick = { navController.navigate(Screen.Party.route) },
                onRecipesClick = { navController.navigate(Screen.Recipes.route) },
                onCreationsClick = { navController.navigate(Screen.Creations.route) }
            )
        }

        composable(Screen.Chat.route) {
            ChatSelectorScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Party.route) {
            PartyScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Recipes.route) {
            RecipesScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.Creations.route) {
            CreationsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
