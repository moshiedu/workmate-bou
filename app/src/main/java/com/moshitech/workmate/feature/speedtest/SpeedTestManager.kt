package com.moshitech.workmate.feature.speedtest

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class NetworkInfo(
    val ip: String = "",
    val isp: String = "",
    val type: String = ""
)

data class IpApiResponse(
    val query: String, // IP
    val isp: String
)

class SpeedTestManager(private val context: Context) {

    // Using Cloudflare speed test files for reliability
    private val DOWNLOAD_URL = "https://speed.cloudflare.com/__down?bytes=25000000" // 25MB
    private val UPLOAD_URL = "https://speed.cloudflare.com/__up"
    private val PING_URL = "https://www.google.com"
    private val IP_API_URL = "http://ip-api.com/json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun fetchNetworkInfo(): NetworkInfo = withContext(Dispatchers.IO) {
        var ip = "Unknown"
        var isp = "Unknown"
        var type = getNetworkType()

        try {
            val request = Request.Builder().url(IP_API_URL).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val apiResponse = gson.fromJson(body, IpApiResponse::class.java)
                        ip = apiResponse.query
                        isp = apiResponse.isp
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext NetworkInfo(ip, isp, type)
    }

    private fun getNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return "No Internet"
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return "Unknown"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }

    suspend fun measurePing(): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(PING_URL)
                .head()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext -1L
            }
            return@withContext System.currentTimeMillis() - start
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1L
        }
    }

    suspend fun measureDownloadSpeed(onProgress: (Float, String) -> Unit): Float = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(DOWNLOAD_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val inputStream = connection.inputStream
            val buffer = ByteArray(8192)
            var bytesRead = 0
            var totalBytesRead = 0L
            val startTime = System.currentTimeMillis()
            var lastUpdate = startTime

            // Run for max 15 seconds or until finished
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                val currentTime = System.currentTimeMillis()
                val duration = currentTime - startTime
                
                if (duration > 15000) break // Stop after 15s

                if (currentTime - lastUpdate > 100) { // Update every 100ms
                    val speedMbps = (totalBytesRead * 8.0f) / (duration / 1000.0f) / 1_000_000.0f
                    onProgress(speedMbps, formatSpeed(speedMbps))
                    lastUpdate = currentTime
                }
            }

            val totalDuration = System.currentTimeMillis() - startTime
            val finalSpeedMbps = (totalBytesRead * 8.0f) / (totalDuration / 1000.0f) / 1_000_000.0f
            return@withContext finalSpeedMbps

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0f
        } finally {
            inputStream?.close()
            connection?.disconnect()
        }
    }

    suspend fun measureLiteDownloadSpeed(): Float = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            // 5MB for lite test
            val url = URL("https://speed.cloudflare.com/__down?bytes=5000000")
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val inputStream = connection.inputStream
            val buffer = ByteArray(8192)
            var bytesRead = 0
            var totalBytesRead = 0L
            val startTime = System.currentTimeMillis()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
            }

            val totalDuration = System.currentTimeMillis() - startTime
            if (totalDuration == 0L) return@withContext 0f
            
            val finalSpeedMbps = (totalBytesRead * 8.0f) / (totalDuration / 1000.0f) / 1_000_000.0f
            return@withContext finalSpeedMbps

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0f
        } finally {
            inputStream?.close()
            connection?.disconnect()
        }
    }

    suspend fun measureUploadSpeed(
        onProgress: (Float, String) -> Unit,
        onError: (String) -> Unit
    ): Float = withContext(Dispatchers.IO) {
        try {
            val totalSize = 20 * 1024 * 1024L // 20MB
            val startTime = System.currentTimeMillis()
            var lastUpdate = startTime

            val requestBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()

                override fun contentLength() = totalSize

                override fun writeTo(sink: BufferedSink) {
                    val buffer = ByteArray(8192) { 1 }
                    var bytesWritten = 0L
                    
                    while (bytesWritten < totalSize) {
                        sink.write(buffer)
                        bytesWritten += buffer.size
                        
                        val currentTime = System.currentTimeMillis()
                        val duration = currentTime - startTime
                        
                        // Update progress
                        if (currentTime - lastUpdate > 100) {
                            val speedMbps = (bytesWritten * 8.0f) / (duration / 1000.0f) / 1_000_000.0f
                            onProgress(speedMbps, formatSpeed(speedMbps))
                            lastUpdate = currentTime
                        }
                    }
                }
            }

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onError("Upload failed: ${response.code}")
                    return@withContext 0f
                }
            }

            val totalDuration = System.currentTimeMillis() - startTime
            val finalSpeedMbps = (totalSize * 8.0f) / (totalDuration / 1000.0f) / 1_000_000.0f
            return@withContext finalSpeedMbps

        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Upload error")
            return@withContext 0f
        }
    }

    private fun formatSpeed(mbps: Float): String {
        return "%.2f Mbps".format(mbps)
    }
}
