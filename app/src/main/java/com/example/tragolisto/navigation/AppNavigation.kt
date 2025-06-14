package com.example.tragolisto.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tragolisto.chat.ChatScreen
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
fun AppNavigation(requiresOnboarding: Boolean) { // <-- ¡Añadimos este parámetro!
    val navController = rememberNavController()

    // Decidir la ruta de inicio basada en 'requiresOnboarding'
    val startRoute = if (requiresOnboarding) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startRoute // <-- La ruta inicial ahora es dinámica
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    // Después de que el onboarding en la UI se "complete",
                    // navegamos a Home. Asumimos que si hay datos adicionales
                    // como nombre o fecha de nacimiento, OnboardingScreen los
                    // habrá enviado al backend.
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true } // Elimina Onboarding de la pila
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                // Aquí, 'usuarioglobal?.nombre' debería estar ya establecido desde MainActivity
                // después de la autenticación de Firebase y la llamada inicial al backend.
                userName = (usuarioglobal?.nombre ?: "Usuario"), // Usar "Usuario" si el nombre es nulo
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