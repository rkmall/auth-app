package com.rm.loginappcompose.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rm.loginappcompose.googlesignin.rememberGoogleSignInState
import com.rm.loginappcompose.ui.screen.authentication.AuthenticationScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(
    startDestination: String,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = startDestination ) {
        authenticationRoute {  }

        homeRoute()
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
) {
    composable(
        route = Screen.Authentication.route
    ) {

        var loadingState by remember { mutableStateOf(false) }
        var authenticated by remember { mutableStateOf(false) }
        val signInState = rememberGoogleSignInState()

        AuthenticationScreen(
            authenticated = authenticated ,
            loadingState = loadingState,
            signInState = signInState,
            onButtonClicked = {
                signInState.open()
                loadingState = true
                authenticated = true
            },
            onDialogDismissed = {
                loadingState = false
                authenticated = false
            }
        )
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(
        route = Screen.Home.route
    ) {

    }
}