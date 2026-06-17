package com.task.ibiproductsapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.task.ibiproductsapp.common.Routes
import com.task.ibiproductsapp.presentation.login.LoginScreen
import com.task.ibiproductsapp.presentation.login.addeditproduct.AddEditProductScreen
import com.task.ibiproductsapp.presentation.login.favorite.FavoritesScreen
import com.task.ibiproductsapp.presentation.product.ProductListScreen
import com.task.ibiproductsapp.presentation.productdetail.ProductDetailScreen
import com.task.ibiproductsapp.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Products,
        BottomNavItem.Favorites,
        BottomNavItem.Settings
    )

    val startDestination = if (isLoggedIn) Routes.PRODUCTS else Routes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.PRODUCTS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.PRODUCTS) {
                ProductListScreen(
                    onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                    onAddProductClick = { navController.navigate(Routes.ADD_PRODUCT) }
                )
            }

            composable(
                route = Routes.PRODUCT_DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) {
                ProductDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate(Routes.editProduct(id)) }
                )
            }

            composable(Routes.ADD_PRODUCT) {
                AddEditProductScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.EDIT_PRODUCT,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) {
                AddEditProductScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onProductClick = { id -> navController.navigate(Routes.productDetail(id)) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Products : BottomNavItem(Routes.PRODUCTS, "Products", Icons.Default.Home)
    object Favorites : BottomNavItem(Routes.FAVORITES, "Favorites", Icons.Default.Favorite)
    object Settings : BottomNavItem(Routes.SETTINGS, "Settings", Icons.Default.Settings)
}