package com.rm.loginappcompose.ui.navigation

sealed class Route(val route: String) {
    data object Authentication : Route(route = "authentication")
    data object Home : Route(route = "home")
}