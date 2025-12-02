package com.moshitech.workmate.feature.speedtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import com.moshitech.workmate.feature.speedtest.worker.SpeedTestWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class SpeedTestServer(
    val id: Int,
    val name: String,
    val location: String,
    val host: String,
    val downloadUrl: String,
    val uploadUrl: String
)

data class SpeedTestState(
    val isNetworkAvailable: Boolean = false,
    val isTesting: Boolean = false,
    val currentStage: TestStage = TestStage.IDLE,
    val ping: Long = 0,
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val jitter: Long = 0,
    val packetLoss: Float = 0f,
    val downloadProgress: Float = 0f, // 0 to 1
    val uploadProgress: Float = 0f,   // 0 to 1
    val currentSpeedDisplay: String = "0.00 Mbps",
    val error: String? = null,
    val networkInfo: NetworkInfo = NetworkInfo(),
    val history: List<SpeedTestResult> = emptyList(),
    val servers: List<SpeedTestServer> = emptyList(),
    val selectedServer: SpeedTestServer? = null,
    val isScheduled: Boolean = false,
    val scheduleInterval: Long = 12 // hours
)

enum class TestStage {
    IDLE, PING, DOWNLOAD, UPLOAD, FINISHED
}

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {

    private val speedTestManager = SpeedTestManager(application)
    private val dao = AppDatabase.getDatabase(application).speedTestDao()
    private val connectivityMonitor = NetworkConnectivityMonitor(application)

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state.asStateFlow()

    init {
        initializeServers()
        monitorNetworkConnectivity()
        loadHistory()
        fetchNetworkInfo()
        checkScheduleStatus()
    }

    // -------------------- INITIALIZATION -------------------- //

    private fun initializeServers() {
        val serverList = listOf(
            SpeedTestServer(
                1,
                "Auto (Cloudflare CDN)",
                "Global",
                "speed.cloudflare.com",
                "https://speed.cloudflare.com/__down?bytes=25000000",
                "https://speed.cloudflare.com/__up"
            ),
            SpeedTestServer(
                2,
                "Tele2",
                "Europe",
                "speedtest.tele2.net",
                "http://speedtest.tele2.net/10MB.zip",
                "http://speedtest.tele2.net/upload.php"
            ),
            SpeedTestServer(
                3,
                "Leaseweb",
                "US",
                "mirror.us.leaseweb.net",
                "https://mirror.us.leaseweb.net/speedtest/100mb.bin",
                "https://mirror.us.leaseweb.net/speedtest/upload.php"
            )
        )
        _state.value = _state.value.copy(
            servers = serverList,
            selectedServer = serverList[0]
        )
    }

    fun selectServer(server: SpeedTestServer) {
        _state.value = _state.value.copy(selectedServer = server)
    }

    private fun monitorNetworkConnectivity() {
        viewModelScope.launch {
            connectivityMonitor.isConnected.collect { isConnected ->
                _state.value = _state.value.copy(isNetworkAvailable = isConnected)
                if (isConnected) {
                    fetchNetworkInfo()
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            dao.getAllResults().collect { results ->
                _state.value = _state.value.copy(history = results)
            }
        }
    }

    private fun fetchNetworkInfo() {
        viewModelScope.launch {
            val info = speedTestManager.fetchNetworkInfo()
            _state.value = _state.value.copy(networkInfo = info)
        }
    }

    // -------------------- MAIN SPEED TEST -------------------- //

    fun startTest() {
        if (_state.value.isTesting) return

        viewModelScope.launch {
            // Reset state
            _state.value = _state.value.copy(
                isTesting = true,
                currentStage = TestStage.PING,
                currentSpeedDisplay = "Testing Ping...",
                error = null,
                packetLoss = 0f,
                jitter = 0
            )

            // Refresh network info before test
            val netInfo = speedTestManager.fetchNetworkInfo()
            _state.value = _state.value.copy(networkInfo = netInfo)

            // 1. Ping, Jitter, Packet Loss
            val pingResult = speedTestManager.measureAdvancedPing()
            _state.value = _state.value.copy(
                ping = pingResult.ping,
                jitter = pingResult.jitter,
                packetLoss = pingResult.packetLoss,
                currentStage = TestStage.DOWNLOAD,
                currentSpeedDisplay = "Preparing Download..."
            )

            // 2. Download
            val downloadUrl = _state.value.selectedServer?.downloadUrl
                ?: "https://speed.cloudflare.com/__down?bytes=25000000"

            val downloadSpeed = speedTestManager.measureDownloadSpeed(downloadUrl) { speed, display ->
                _state.value = _state.value.copy(
                    downloadSpeed = speed,
                    currentSpeedDisplay = display,
                    downloadProgress = speed / 100f // Approximation
                )
            }

            _state.value = _state.value.copy(
                downloadSpeed = downloadSpeed,
                currentStage = TestStage.UPLOAD,
                currentSpeedDisplay = "Preparing Upload..."
            )

            // 3. Upload
            val uploadUrl = _state.value.selectedServer?.uploadUrl
                ?: "https://speed.cloudflare.com/__up"

            val uploadSpeed = speedTestManager.measureUploadSpeed(
                uploadUrl = uploadUrl,
                onProgress = { speed, display ->
                    _state.value = _state.value.copy(
                        uploadSpeed = speed,
                        currentSpeedDisplay = display,
                        uploadProgress = speed / 50f // Approximation
                    )
                },
                onError = { errorMsg ->
                    _state.value = _state.value.copy(error = errorMsg)
                }
            )

            val currentState = _state.value

            // 4. Save result
            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = uploadSpeed,
                ping = currentState.ping,
                networkType = netInfo.type,
                ipAddress = netInfo.ip,
                isp = netInfo.isp,
                jitter = currentState.jitter,
                packetLoss = currentState.packetLoss
            )
            dao.insert(result)

            // 5. Final UI state
            _state.value = currentState.copy(
                isTesting = false,
                currentStage = TestStage.FINISHED,
                uploadSpeed = uploadSpeed,
                currentSpeedDisplay = "Test Complete"
            )
        }
    }

    // -------------------- SCHEDULING -------------------- //

    fun toggleSchedule(enable: Boolean, intervalHours: Long = 12) {
        val workManager = WorkManager.getInstance(getApplication())
        val workName = "speed_test_periodic"

        if (enable) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest =
                PeriodicWorkRequestBuilder<SpeedTestWorker>(
                    intervalHours,
                    TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )

            _state.value = _state.value.copy(
                isScheduled = true,
                scheduleInterval = intervalHours
            )
        } else {
            workManager.cancelUniqueWork(workName)
            _state.value = _state.value.copy(isScheduled = false)
        }
    }

    private fun checkScheduleStatus() {
        val workManager = WorkManager.getInstance(getApplication())
        val workName = "speed_test_periodic"

        viewModelScope.launch {
            try {
                val workInfoList = workManager.getWorkInfosForUniqueWork(workName).get()
                val isScheduled = workInfoList.any {
                    it.state == WorkInfo.State.ENQUEUED ||
                            it.state == WorkInfo.State.RUNNING
                }
                _state.value = _state.value.copy(isScheduled = isScheduled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
