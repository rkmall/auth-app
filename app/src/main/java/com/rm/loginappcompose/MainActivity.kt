package com.rm.loginappcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.rm.loginappcompose.data.AppConstants
import com.rm.loginappcompose.googlesignin.SignInWithGoogle
import com.rm.loginappcompose.googlesignin.rememberGoogleSignInState
import com.rm.loginappcompose.ui.navigation.AppNavGraph
import com.rm.loginappcompose.ui.navigation.RouteName
import com.rm.loginappcompose.ui.theme.LoginAppComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginAppComposeTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    startDestination = RouteName.AUTHENTICATION,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SignInWithGoogleTest() {
    val signInState = rememberGoogleSignInState()
    SignInWithGoogle(
        state = signInState  ,
        clientId = AppConstants.WEB_CLIENT_ID ,
        onTokenIdReceived = {
            Log.d("Token", "Token: $it")
        },
        onDialogDismissed = {}
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {signInState.open()}) {
            Text(text = "Sign In With Google")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoogleSignInTest() {
    LoginAppComposeTheme {
        SignInWithGoogleTest()
    }
}