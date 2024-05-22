package com.rm.loginappcompose.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rm.loginappcompose.googlesignin.rememberGoogleSignInState
import com.rm.loginappcompose.ui.screen.authentication.AuthenticationScreen
import com.rm.loginappcompose.ui.screen.authentication.AuthenticationViewModel

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
    navigateToHome: () -> Unit
) {
    composable(route = Route.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState.collectAsState()
        val signInState = rememberGoogleSignInState()

        AuthenticationScreen(
            loadingState = loadingState,
            signInState = signInState,
            onButtonClicked = {
                signInState.open()
                viewModel.setLoadingState(true)
            },
            onTokenIdReceived = { token ->
                Log.d("Token", "Token: $token")
                viewModel.signInWithMongoDb(
                    tokenId = token,
                    onSuccess = { result ->
                        Log.d("Token", "Sign-in to MongoDb success: $result")
                        viewModel.setLoadingState(false)

                    },
                    onError = {
                        Log.d("Token", "Sign-in to MongoDb exception: ${it.message}")
                        viewModel.setLoadingState(false)
                    }
                )
            },
            onDialogDismissed = { message ->
                Log.d("Token", "Message: $message")
            }
        )
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(
        route = Route.Home.route
    ) {}
}