package com.rm.loginappcompose.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rm.loginappcompose.data.AppConstants
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    scope.launch {
                        viewModel.getUserInfo()
                    }
                }
            ) {
                Text(text = "Get UserInfo")
            }

            Button(
                onClick = {
                    scope.launch {
                        App.create(AppConstants.MONGO_APP_ID).currentUser?.logOut()
                    }
                }
            ) {
                Text(text = "Logout")
            }
        }
    }
}

/*@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}*/



