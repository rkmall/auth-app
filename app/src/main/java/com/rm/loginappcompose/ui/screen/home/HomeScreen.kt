package com.rm.loginappcompose.ui.screen.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rm.loginappcompose.data.model.UserInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userInfo: UserInfo,
    onLogoutButtonClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Home")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Button(
                        onClick = {
                            onLogoutButtonClicked()
                        }
                    ) {
                        Text(text = "Logout")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
                    RowItem(
                        item = "User Info",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    RowItem(
                        item = "Name: ${userInfo.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal)
                }

                item {
                    RowItem(
                        item = "Email: ${userInfo.email}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal)
                }

                item {
                    RowItem(
                        item = "Picture: ${userInfo.picture}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal)
                }

                item {
                    RowItem(
                        item = "Access token: ${userInfo.mongoAccessId}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun RowItem(
    item: String,
    fontSize: TextUnit,
    fontWeight: FontWeight
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        text = item,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = TextAlign.Start,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
    Divider(thickness = 1.dp)
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        userInfo = UserInfo(),
        onLogoutButtonClicked = {}
    )
}



