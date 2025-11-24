package com.moshitech.workmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.moshitech.workmate.feature.home.HomeScreen
import com.moshitech.workmate.feature.photoconversion.ui.PhotoConversionScreen
import com.moshitech.workmate.feature.rambooster.RamBoosterScreen
import com.moshitech.workmate.feature.unitconverter.UnitConverterScreen
import com.moshitech.workmate.feature.settings.SettingsScreen
import com.moshitech.workmate.feature.splash.SplashScreen
import com.moshitech.workmate.MainViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object PhotoConversion : Screen("photo_conversion")
    object UnitConversion : Screen("unit_conversion")
    object Compass : Screen("compass")
    object AppLock : Screen("app_lock")
    object RamBooster : Screen("ram_booster")
    object Settings : Screen("settings")
    object About : Screen("about")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
}

@Composable
fun WorkmateNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.PhotoConversion.route) {
             PhotoConversionScreen()
        }
        composable(Screen.UnitConversion.route) {
             UnitConverterScreen(navController = navController)
        }
        composable(
            route = "unit_conversion_details/{categoryName}",
            arguments = listOf(androidx.navigation.navArgument("categoryName") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "LENGTH"
            if (categoryName == "TIME") {
                com.moshitech.workmate.feature.unitconverter.TimeToolsScreen(navController)
            } else {
                com.moshitech.workmate.feature.unitconverter.ConversionDetailsScreen(navController, categoryName)
            }
        }
        composable("manage_favorites") {
            com.moshitech.workmate.feature.unitconverter.ManageFavoritesScreen(navController = navController)
        }
        composable(Screen.Compass.route) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Compass Screen")
            }
        }
        composable(Screen.AppLock.route) {
             com.moshitech.workmate.feature.applock.AppLockScreen(navController = navController)
        }
        composable(Screen.RamBooster.route) {
             RamBoosterScreen(navController = navController)
        }
         composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
