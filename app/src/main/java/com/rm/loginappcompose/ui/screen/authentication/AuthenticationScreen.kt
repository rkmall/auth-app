package com.rm.loginappcompose.ui.screen.authentication

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rm.loginappcompose.data.AppConstants
import com.rm.loginappcompose.googlesignin.SignInWithGoogle
import kotlinx.coroutines.flow.Flow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    state: AuthState,
    effect: Flow<AuthEffect>?,
    onEventSent: (AuthEvent) -> Unit,
    onNavigationRequested: (AuthEffect.Navigation.ToHomeScreen) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        effect?.collect { effect ->
            when (effect) {
                is AuthEffect.Navigation.ToHomeScreen -> {
                    onNavigationRequested(effect)
                }

                is AuthEffect.SignInFailure -> {
                    snackBarHostState.showSnackbar(
                        message = "Something went wrong",
                        duration = SnackbarDuration.Short
                    )
                }

                is AuthEffect.GoogleSignInDismissed -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            AuthenticationContent(
                loadingState = state.isLoading,
                onButtonClicked = { onEventSent(AuthEvent.OnSignInButtonClicked) }
            )
        }
    )

    SignInWithGoogle(
        state = state.googleButtonState.signInState,
        clientId = AppConstants.WEB_CLIENT_ID ,
        onTokenIdReceived = { onEventSent(AuthEvent.OnTokenReceived(it)) },
        onDialogDismissed = { onEventSent(AuthEvent.OnGoogleSignInDismissed(it)) },
        onExceptionReceived = { onEventSent(AuthEvent.OnGoogleSignInException) }
    )
}

@Preview
@Composable
fun PreviewAuthenticationScreen() {
    AuthenticationScreen(
        state = AuthState(),
        effect = null,
        onEventSent = {},
        onNavigationRequested = {}
    )
}

