package com.monito.mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monito.mobile.di.AppContainer
import com.monito.mobile.presentation.day.DayScreen
import com.monito.mobile.presentation.day.DayViewModel
import com.monito.mobile.presentation.dayViewModelFactory
import com.monito.mobile.presentation.hub.HubScreen
import com.monito.mobile.presentation.hub.HubViewModel
import com.monito.mobile.presentation.hubViewModelFactory
import com.monito.mobile.presentation.profile.ProfileScreen
import com.monito.mobile.presentation.profile.ProfileViewModel
import com.monito.mobile.presentation.profileViewModelFactory

@Composable
fun AppNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HUB) {
        composable(Routes.HUB) {
            val viewModel: HubViewModel = viewModel(factory = hubViewModelFactory(container))
            HubScreen(
                viewModel = viewModel,
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenDay = { navController.navigate(Routes.DAY) },
            )
        }
        composable(Routes.DAY) {
            val viewModel: DayViewModel = viewModel(factory = dayViewModelFactory(container))
            DayScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            val viewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory(container))
            ProfileScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
