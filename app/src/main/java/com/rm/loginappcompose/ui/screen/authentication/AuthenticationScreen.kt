package com.rm.loginappcompose.ui.screen.authentication

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rm.loginappcompose.data.AppConstants
import com.rm.loginappcompose.googlesignin.GoogleSignInState
import com.rm.loginappcompose.googlesignin.SignInWithGoogle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    authenticated: Boolean,
    loadingState: Boolean,
    signInState: GoogleSignInState,
    onButtonClicked: () -> Unit = {},
    onSuccessfulFirebaseSignIn: (String) -> Unit = {},
    onFailedFirebaseSignIn: (Exception) -> Unit = {},
    onDialogDismissed: (String) -> Unit = {},
    navigateToHome: () -> Unit = {}
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
        onTokenIdReceived = {},
        onDialogDismissed = { message -> onDialogDismissed(message) }
    )
}