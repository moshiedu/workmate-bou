package com.moshitech.workmate.feature.photoconversion.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryDao
import com.moshitech.workmate.feature.photoconversion.data.local.ConversionHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversionSettings(
    val format: CompressFormat,
    val quality: Int, // 0-100
    val width: Int? = null,
    val height: Int? = null,
    val maintainAspectRatio: Boolean = true
)

enum class CompressFormat(val extension: String, val compressFormat: Bitmap.CompressFormat) {
    JPEG("jpg", Bitmap.CompressFormat.JPEG),
    PNG("png", Bitmap.CompressFormat.PNG),
    WEBP("webp", if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP);
}

class ImageRepository(
    private val context: Context,
    private val historyDao: ConversionHistoryDao
) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun convertAndSaveImage(uri: Uri, settings: ConversionSettings): Result<ConversionHistoryEntity> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("Failed to decode image"))
            }

            // Resize logic
            var finalBitmap = originalBitmap
            if (settings.width != null && settings.height != null) {
                finalBitmap = Bitmap.createScaledBitmap(originalBitmap, settings.width, settings.height, true)
            }

            // Save logic
            val timestamp = System.currentTimeMillis()
            val fileName = "converted_${timestamp}.${settings.format.extension}"
            val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Converted")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, fileName)

            val outputStream = FileOutputStream(outputFile)
            finalBitmap.compress(settings.format.compressFormat, settings.quality, outputStream)
            outputStream.flush()
            outputStream.close()

            // Create history item
            val historyItem = ConversionHistoryEntity(
                originalPath = uri.toString(),
                convertedPath = outputFile.absolutePath,
                format = settings.format.name,
                size = formatFileSize(outputFile.length()),
                timestamp = timestamp
            )
            
            // Save to DB
            historyDao.insert(historyItem)

            Result.success(historyItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHistory(): Flow<List<ConversionHistoryEntity>> {
        return historyDao.getAll()
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }
    
    suspend fun deleteHistoryItem(item: ConversionHistoryEntity) {
        historyDao.delete(item)
        // Optionally delete the file too? For now, let's keep the file.
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        if (kb < 1024) {
            return String.format(Locale.US, "%.2f KB", kb)
        }
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.2f MB", mb)
    }
}
