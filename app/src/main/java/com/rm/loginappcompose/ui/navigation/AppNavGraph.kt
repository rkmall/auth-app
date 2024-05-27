package com.rm.loginappcompose.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rm.loginappcompose.data.AppConstants
import com.rm.loginappcompose.googlesignin.rememberGoogleSignInState
import com.rm.loginappcompose.ui.screen.authentication.AuthenticationScreen
import com.rm.loginappcompose.ui.screen.authentication.AuthenticationViewModel
import com.rm.loginappcompose.ui.screen.home.HomeScreen
import com.rm.loginappcompose.ui.screen.home.HomeViewModel
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    startDestination: String,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = startDestination ) {
        authenticationRoute(
            navigateToHome = {
                navController.navigate(Route.Home.route)
            }
        )

        homeRoute(
            navigateToAuth = {
                navController.navigate(Route.Authentication.route)
            }
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit
) {
    composable(route = Route.Authentication.route) {
        val viewModel: AuthenticationViewModel = hiltViewModel()

        AuthenticationScreen(
            state = viewModel.authState.collectAsState().value,
            effect = viewModel.authEffect,
            onEventSent = { event -> viewModel.setAuthEvent(event) },
            onNavigationRequested = { navigationEffect ->
                navigateToHome()
            }
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToAuth: () -> Unit
) {
    composable(route = Route.Home.route) {
        val viewModel: HomeViewModel = hiltViewModel()

        val scope = rememberCoroutineScope()

        HomeScreen(
            userInfo = viewModel.userInfo.collectAsState().value,
            onLogoutButtonClicked = {
                scope.launch {
                    App.create(AppConstants.MONGO_APP_ID).currentUser?.logOut()
                    navigateToAuth()
                }
            }
        )
    }
}