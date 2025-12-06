package com.moshitech.workmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moshitech.workmate.feature.home.HomeScreen
import com.moshitech.workmate.feature.photoconversion.ui.PhotoConversionScreen
import com.moshitech.workmate.feature.rambooster.RamBoosterScreen
import com.moshitech.workmate.feature.unitconverter.UnitConverterScreen
import com.moshitech.workmate.feature.settings.SettingsScreen
import com.moshitech.workmate.feature.splash.SplashScreen
import com.moshitech.workmate.MainViewModel
import com.moshitech.workmate.feature.speedtest.SpeedTestViewModel

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
    object GyroscopeTest : Screen("test_gyroscope")
    object MagnetometerTest : Screen("test_magnetometer")
    object OTGTest : Screen("test_otg")
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
    object BluetoothTest : Screen("test_bluetooth")
    object WifiTest : Screen("test_wifi")
    object GpsTest : Screen("test_gps")
    object NfcTest : Screen("test_nfc")
    object UsbTest : Screen("test_usb")
    object Benchmarks : Screen("benchmarks")
    object BenchmarkResults : Screen("benchmark_results")
    object BenchmarkTrends : Screen("benchmark_trends")
    object Widgets : Screen("widgets")
    object PermissionsExplorer : Screen("permissions_explorer")
    object IntegrityCheck : Screen("integrity_check")
    object SpeedTest : Screen("speed_test")
    object SpeedTestHistory : Screen("speed_test_history")
    object DocumentScanner : Screen("scanner_graph")
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
             PhotoConversionScreen(navController = navController)
        }
        composable(Screen.UnitConversion.route) {
             UnitConverterScreen(navController = navController)
        }
        composable(
            route = "unit_conversion_details/{categoryName}",
            arguments = listOf(androidx.navigation.navArgument("categoryName") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "LENGTH"
            val viewModel: com.moshitech.workmate.feature.unitconverter.UnitConverterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            
            if (categoryName == "TIME" || categoryName.startsWith("TIME_")) {
                com.moshitech.workmate.feature.unitconverter.TimeToolsScreen(navController, categoryName)
            } else if (categoryName == "BMI") {
                LaunchedEffect(Unit) {
                    viewModel.selectCategory(com.moshitech.workmate.feature.unitconverter.UnitCategory.BMI)
                }
                com.moshitech.workmate.feature.unitconverter.BMICalculatorScreen(navController, viewModel)
            } else if (categoryName == "SCREEN_PPI") {
                LaunchedEffect(Unit) {
                    viewModel.selectCategory(com.moshitech.workmate.feature.unitconverter.UnitCategory.SCREEN_PPI)
                }
                com.moshitech.workmate.feature.unitconverter.ScreenPPICalculatorScreen(navController, viewModel)
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
        composable(
            route = "sensor_detail/{sensorType}",
            arguments = listOf(androidx.navigation.navArgument("sensorType") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val sensorType = backStackEntry.arguments?.getInt("sensorType") ?: 0
            com.moshitech.workmate.feature.deviceinfo.screens.SensorDetailScreen(navController, sensorType)
        }
        composable(Screen.Benchmarks.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.BenchmarksScreen(navController)
        }
        composable(Screen.BenchmarkResults.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.BenchmarkResultsScreen(navController)
        }
        composable(Screen.BenchmarkTrends.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.BenchmarkTrendsScreen(navController)
        }
        composable(Screen.Tests.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.TestsScreen(navController)
        }
        composable(Screen.BacklightTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.BacklightTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("backlight", passed))
            }
        }
        composable(Screen.DisplayTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.DisplayTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("display", passed))
            }
        }
        composable(Screen.MultitouchTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.MultitouchTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("multitouch", passed))
            }
        }
        composable(Screen.ButtonTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ButtonTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("buttons", passed))
            }
        }
        composable(Screen.SpeakerTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.SpeakerTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("speakers", passed))
            }
        }
        composable(Screen.MicrophoneTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.MicrophoneTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("microphone", passed))
            }
        }
        composable(Screen.ProximityTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ProximityTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("proximity", passed))
            }
        }
        composable(Screen.LightSensorTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.LightSensorTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("light_sensor", passed))
            }
        }
        composable(Screen.AccelerometerTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.AccelerometerTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("accelerometer", passed))
            }
        }
        composable(Screen.GyroscopeTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.GyroscopeTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("gyroscope", passed))
            }
        }
        composable(Screen.MagnetometerTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.MagnetometerTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("magnetometer", passed))
            }
        }
        composable(Screen.OTGTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.OTGTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("usb", passed))
            }
        }
        composable(Screen.FlashlightTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.FlashlightTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("flashlight", passed))
            }
        }
        composable(Screen.VibrationTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.VibrationTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("vibration", passed))
            }
        }
        composable(Screen.FingerprintTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.FingerprintTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("fingerprint", passed))
            }
        }
        composable(Screen.ChargingTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.ChargingTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("charging", passed))
            }
        }
        composable(Screen.HeadsetTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.HeadsetTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("headset", passed))
            }
        }
        composable(Screen.EarpieceTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.EarpieceTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("earpiece", passed))
            }
        }
        composable(Screen.BluetoothTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.BluetoothTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("bluetooth", passed))
            }
        }
        composable(Screen.WifiTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.WifiTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("wifi", passed))
            }
        }
        composable(Screen.GpsTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.GpsTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("gps", passed))
            }
        }
        composable(Screen.NfcTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.NfcTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("nfc", passed))
            }
        }
        composable(Screen.UsbTest.route) {
            com.moshitech.workmate.feature.deviceinfo.testing.screens.UsbTestScreen(navController) { passed ->
                navController.previousBackStackEntry?.savedStateHandle?.set("test_result", Pair("usb", passed))
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Widgets.route) {
            com.moshitech.workmate.feature.widgets.WidgetsScreen(navController)
        }
        composable(Screen.PermissionsExplorer.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.PermissionsExplorerScreen(navController)
        }
        composable(Screen.IntegrityCheck.route) {
            com.moshitech.workmate.feature.deviceinfo.screens.IntegrityCheckScreen(navController)
        }
//        composable(Screen.SpeedTest.route) {
//            com.moshitech.workmate.feature.speedtest.SpeedTestScreen(navController, mainViewModel = mainViewModel)
//        }
        composable(Screen.SpeedTest.route) {
            val speedTestViewModel: SpeedTestViewModel = viewModel()
            com.moshitech.workmate.feature.speedtest.SpeedTestScreen(
                navController = navController,
                viewModel = speedTestViewModel,
                mainViewModel = mainViewModel
            )
        }
        composable(Screen.SpeedTestHistory.route) {
            val speedTestViewModel: SpeedTestViewModel = viewModel()
            com.moshitech.workmate.feature.speedtest.SpeedTestHistoryScreen(
                navController = navController,
                viewModel = speedTestViewModel
            )
        }
        composable(
            route = "speed_test_detail/{testId}",
            arguments = listOf(
                androidx.navigation.navArgument("testId") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            val speedTestViewModel: SpeedTestViewModel = viewModel()
            com.moshitech.workmate.feature.speedtest.SpeedTestDetailScreen(
                navController = navController,
                testId = testId,
                viewModel = speedTestViewModel
            )
        }
    }
}
