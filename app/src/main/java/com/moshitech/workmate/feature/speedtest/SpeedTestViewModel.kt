package com.moshitech.workmate.feature.speedtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpeedTestState(
    val isTesting: Boolean = false,
    val currentStage: TestStage = TestStage.IDLE,
    val ping: Long = 0,
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val downloadProgress: Float = 0f, // 0 to 1
    val uploadProgress: Float = 0f, // 0 to 1
    val currentSpeedDisplay: String = "0.00 Mbps",
    val error: String? = null,
    val networkInfo: NetworkInfo = NetworkInfo(),
    val history: List<SpeedTestResult> = emptyList()
)

enum class TestStage {
    IDLE, PING, DOWNLOAD, UPLOAD, FINISHED
}

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {

    private val speedTestManager = SpeedTestManager(application)
    private val dao = AppDatabase.getDatabase(application).speedTestDao()

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state.asStateFlow()

    init {
        loadHistory()
        fetchNetworkInfo()
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

    fun startTest() {
        if (_state.value.isTesting) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isTesting = true,
                currentStage = TestStage.PING,
                currentSpeedDisplay = "Testing Ping...",
                error = null
            )

            // Refresh network info before test
            val netInfo = speedTestManager.fetchNetworkInfo()
            _state.value = _state.value.copy(networkInfo = netInfo)

            // 1. Measure Ping
            val ping = speedTestManager.measurePing()
            _state.value = _state.value.copy(
                ping = ping,
                currentStage = TestStage.DOWNLOAD,
                currentSpeedDisplay = "Preparing Download..."
            )

            // 2. Measure Download
            val downloadSpeed = speedTestManager.measureDownloadSpeed { speed, display ->
                _state.value = _state.value.copy(
                    downloadSpeed = speed,
                    currentSpeedDisplay = display,
                    downloadProgress = 0.5f
                )
            }
            _state.value = _state.value.copy(
                downloadSpeed = downloadSpeed,
                currentStage = TestStage.UPLOAD,
                currentSpeedDisplay = "Preparing Upload..."
            )

            // 3. Measure Upload
            val uploadSpeed = speedTestManager.measureUploadSpeed(
                onProgress = { speed, display ->
                    _state.value = _state.value.copy(
                        uploadSpeed = speed,
                        currentSpeedDisplay = display,
                        uploadProgress = 0.5f
                    )
                },
                onError = { errorMsg ->
                    _state.value = _state.value.copy(error = errorMsg)
                }
            )

            // 4. Finish & Save
            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = uploadSpeed,
                ping = ping,
                networkType = netInfo.type,
                ipAddress = netInfo.ip,
                isp = netInfo.isp
            )
            dao.insert(result)

            _state.value = _state.value.copy(
                isTesting = false,
                currentStage = TestStage.FINISHED,
                uploadSpeed = uploadSpeed,
                currentSpeedDisplay = "Test Complete"
            )
        }
    }
}
