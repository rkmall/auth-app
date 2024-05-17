package com.rm.loginappcompose.ui.navigation

import com.rm.loginappcompose.ui.navigation.RouteName.AUTHENTICATION
import com.rm.loginappcompose.ui.navigation.RouteName.HOME

object RouteName {
    const val AUTHENTICATION = "authentication"
    const val HOME = "home"
}

sealed class Screen(val route: String) {
    data object Authentication : Screen(route = AUTHENTICATION)
    data object Home : Screen(route = HOME)
}