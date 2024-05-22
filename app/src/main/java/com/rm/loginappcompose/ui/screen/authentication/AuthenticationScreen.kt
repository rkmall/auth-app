package com.rm.loginappcompose.ui.screen.authentication

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rm.loginappcompose.data.AppConstants
import com.rm.loginappcompose.googlesignin.GoogleSignInState
import com.rm.loginappcompose.googlesignin.SignInWithGoogle
import com.rm.loginappcompose.googlesignin.rememberGoogleSignInState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    loadingState: Boolean,
    signInState: GoogleSignInState,
    onButtonClicked: () -> Unit = {},
    onTokenIdReceived: (String) -> Unit = {},
    onDialogDismissed: (String) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            AuthenticationContent(
                loadingState = loadingState,
                onButtonClicked = onButtonClicked
            )
        }
    )

    SignInWithGoogle(
        state = signInState ,
        clientId = AppConstants.WEB_CLIENT_ID ,
        onTokenIdReceived = { token -> onTokenIdReceived(token) },
        onDialogDismissed = { message -> onDialogDismissed(message) }
    )
}

/**
 * MongoDb will use the token to exchange User information from the Google server.
 * Then, those user information will be stored in the MongoDb.
 */

@Preview
@Composable
fun PreviewAuthenticationScreen() {
    AuthenticationScreen(
        loadingState = false ,
        signInState = rememberGoogleSignInState()
    )
}

