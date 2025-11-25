package com.moshitech.workmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
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
    object DeviceInfo : Screen("device_info")
    object Tests : Screen("tests")
    object BacklightTest : Screen("test_backlight")
    object DisplayTest : Screen("test_display")
    object MultitouchTest : Screen("test_multitouch")
    object ButtonTest : Screen("test_button")
    object SpeakerTest : Screen("test_speaker")
    object MicrophoneTest : Screen("test_microphone")
    object ProximityTest : Screen("test_proximity")
    object LightSensorTest : Screen("test_light_sensor")
    object AccelerometerTest : Screen("test_accelerometer")
    object FlashlightTest : Screen("test_flashlight")
    object VibrationTest : Screen("test_vibration")
    object FingerprintTest : Screen("test_fingerprint")
    object ChargingTest : Screen("test_charging")
    object HeadsetTest : Screen("test_headset")
    object EarpieceTest : Screen("test_earpiece")
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
            com.moshitech.workmate.feature.compass.CompassScreen(navController)
        }
        composable(Screen.AppLock.route) {
             com.moshitech.workmate.feature.applock.AppLockScreen(navController = navController)
        }
        composable(Screen.RamBooster.route) {
             RamBoosterScreen(navController = navController)
        }
        composable(Screen.DeviceInfo.route) {
            com.moshitech.workmate.feature.deviceinfo.DeviceInfoScreen(navController = navController)
        }
        composable(Screen.Tests.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.TestsScreen(
                navController = navController,
                isDark = isSystemInDarkTheme()
            )
        }
        composable(Screen.BacklightTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.BacklightTestScreen(navController) { /* result */ }
        }
        composable(Screen.DisplayTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.DisplayTestScreen(navController) { /* result */ }
        }
        composable(Screen.MultitouchTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.MultitouchTestScreen(navController) { /* result */ }
        }
        composable(Screen.ButtonTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ButtonTestScreen(navController) { /* result */ }
        }
        composable(Screen.SpeakerTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.SpeakerTestScreen(navController) { /* result */ }
        }
        composable(Screen.MicrophoneTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.MicrophoneTestScreen(navController) { /* result */ }
        }
        composable(Screen.ProximityTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ProximityTestScreen(navController) { /* result */ }
        }
        composable(Screen.LightSensorTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.LightSensorTestScreen(navController) { /* result */ }
        }
        composable(Screen.AccelerometerTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.AccelerometerTestScreen(navController) { /* result */ }
        }
        composable(Screen.FlashlightTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.FlashlightTestScreen(navController) { /* result */ }
        }
        composable(Screen.VibrationTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.VibrationTestScreen(navController) { /* result */ }
        }
        composable(Screen.FingerprintTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.FingerprintTestScreen(navController) { /* result */ }
        }
        composable(Screen.ChargingTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ChargingTestScreen(navController) { /* result */ }
        }
        composable(Screen.HeadsetTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.HeadsetTestScreen(navController) { /* result */ }
        }
        composable(Screen.EarpieceTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.EarpieceTestScreen(navController) { /* result */ }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
