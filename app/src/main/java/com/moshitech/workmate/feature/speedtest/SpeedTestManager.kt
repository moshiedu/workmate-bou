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
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.math.min

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

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val PING_URL = "https://www.google.com"
    }

    private val gson = Gson()

    suspend fun fetchNetworkInfo(): NetworkInfo = withContext(Dispatchers.IO) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val type = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "No Connection"
        }

        // Fetch IP and ISP info
        var ip = "Unknown"
        var isp = "Unknown"
        try {
            val request = Request.Builder()
                .url("http://ip-api.com/json")
                .build()
            
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

    data class PingResult(
        val ping: Long,
        val jitter: Long,
        val packetLoss: Float // Percentage 0-100
    )

    suspend fun measureAdvancedPing(): PingResult = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Long>()
        val totalAttempts = 10
        var failedAttempts = 0

        for (i in 0 until totalAttempts) {
            val start = System.currentTimeMillis()
            try {
                val request = Request.Builder()
                    .url(PING_URL)
                    .head()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        failedAttempts++
                    } else {
                        pings.add(System.currentTimeMillis() - start)
                    }
                }
            } catch (e: Exception) {
                failedAttempts++
                e.printStackTrace()
            }
            // Small delay between pings
            kotlinx.coroutines.delay(100)
        }

        val packetLoss = (failedAttempts.toFloat() / totalAttempts.toFloat()) * 100f
        
        if (pings.isEmpty()) {
            return@withContext PingResult(0, 0, 100f)
        }

        val avgPing = pings.average()
        val jitter = pings.map { kotlin.math.abs(it - avgPing) }.average().toLong()
        val finalPing = pings.minOrNull() ?: 0L // Use min ping as "best" ping

        return@withContext PingResult(finalPing, jitter, packetLoss)
    }

    suspend fun measureDownloadSpeed(downloadUrl: String, onProgress: (Float, String) -> Unit): Float = withContext(Dispatchers.IO) {
        var totalBytesRead = 0L
        val startTime = System.currentTimeMillis()
        var currentSpeed = 0f

        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val inputStream = response.body?.byteStream() ?: return@withContext 0f
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var lastUpdate = startTime

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    val currentTime = System.currentTimeMillis()
                    val duration = (currentTime - startTime) / 1000.0
                    
                    if (duration > 15) break // Stop after 15s

                    if (currentTime - lastUpdate > 100) {
                        if (duration > 0) {
                            currentSpeed = (totalBytesRead * 8 / duration / 1_000_000).toFloat() // Mbps
                            onProgress(currentSpeed, "%.2f Mbps".format(currentSpeed))
                        }
                        lastUpdate = currentTime
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext currentSpeed
    }

    suspend fun measureUploadSpeed(
        uploadUrl: String,
        onProgress: (Float, String) -> Unit,
        onError: (String) -> Unit
    ): Float = withContext(Dispatchers.IO) {
        var totalBytesSent = 0L
        val startTime = System.currentTimeMillis()
        var currentSpeed = 0f
        
        try {
            // Create a dummy request body (10MB total, sent in chunks)
            val dummyData = ByteArray(1024 * 1024) // 1MB chunk
            val requestBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = 10L * 1024 * 1024 // 10MB total
                override fun writeTo(sink: BufferedSink) {
                    var remaining = contentLength()
                    while (remaining > 0) {
                        val toWrite = minOf(dummyData.size.toLong(), remaining)
                        sink.write(dummyData, 0, toWrite.toInt())
                        remaining -= toWrite
                        
                        totalBytesSent += toWrite
                        val duration = (System.currentTimeMillis() - startTime) / 1000.0
                        
                        if (duration > 10) break // Stop after 10s

                        if (duration > 0) {
                            currentSpeed = (totalBytesSent * 8 / duration / 1_000_000).toFloat()
                            onProgress(currentSpeed, "%.2f Mbps".format(currentSpeed))
                        }
                    }
                }
            }

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { 
                // We don't care about response, just the upload process
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Upload failed")
        }

        return@withContext currentSpeed
    }
}
