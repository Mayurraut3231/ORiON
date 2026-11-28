package raut.mayur.orion.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import raut.mayur.orion.ui.navigation.destination.home.AppLogDestination
import raut.mayur.orion.ui.navigation.destination.home.CreditDestination
import raut.mayur.orion.ui.navigation.destination.home.ListenTogetherDestination
import raut.mayur.orion.ui.navigation.destination.home.ListenTogetherSettingsDestination
import raut.mayur.orion.ui.navigation.destination.home.MoodDestination
import raut.mayur.orion.ui.navigation.destination.home.NotificationDestination
import raut.mayur.orion.ui.navigation.destination.home.SettingsDestination
import raut.mayur.orion.ui.screen.home.ListenTogetherScreen
import raut.mayur.orion.ui.screen.home.ListenTogetherSettingsScreen
import raut.mayur.orion.ui.screen.home.MoodScreen
import raut.mayur.orion.ui.screen.home.NotificationScreen
import raut.mayur.orion.ui.screen.home.SettingScreen
import raut.mayur.orion.ui.screen.other.AppLogScreen
import raut.mayur.orion.ui.screen.other.CreditScreen

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<CreditDestination> {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<AppLogDestination> {
        AppLogScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<ListenTogetherDestination> {
        ListenTogetherScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<ListenTogetherSettingsDestination> {
        ListenTogetherSettingsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}