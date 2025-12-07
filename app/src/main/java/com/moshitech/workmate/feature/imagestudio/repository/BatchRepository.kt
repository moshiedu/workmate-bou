package com.moshitech.workmate.feature.imagestudio.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import com.moshitech.workmate.feature.imagestudio.data.ConversionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

class BatchRepository(private val context: Context) {

    suspend fun convertAndSaveImage(uri: Uri, settings: ConversionSettings): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(Exception("Cannot open stream"))
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) return@withContext Result.failure(Exception("Failed to decode bitmap"))

            // Resize Logic
            var finalBitmap = originalBitmap
            if (settings.width != null && settings.height != null) {
                if (settings.maintainAspectRatio) {
                    // Fit within bounds logic
                    val originalRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                    val targetRatio = settings.width.toFloat() / settings.height.toFloat()
                    
                    val (newWidth, newHeight) = if (originalRatio > targetRatio) {
                        // Fit to width
                        settings.width to (settings.width / originalRatio).toInt()
                    } else {
                        // Fit to height
                        (settings.height * originalRatio).toInt() to settings.height
                    }
                    finalBitmap = originalBitmap.scale(newWidth, newHeight)
                } else {
                    // Stretch logic
                    finalBitmap = originalBitmap.scale(settings.width, settings.height)
                }
            } else if (settings.width != null) {
                 val ratio = originalBitmap.height.toFloat() / originalBitmap.width.toFloat()
                 val height = (settings.width * ratio).toInt()
                 finalBitmap = originalBitmap.scale(settings.width, height)
            } else if (settings.height != null) {
                 val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                 val width = (settings.height * ratio).toInt()
                 finalBitmap = originalBitmap.scale(width, settings.height)
            }

            // Save Logic
            val ext = when (settings.format) {
                CompressFormat.JPEG -> "jpg"
                CompressFormat.PNG -> "png"
                CompressFormat.WEBP -> "webp"
                CompressFormat.BMP -> "bmp"
            }
            val compressFmt = when (settings.format) {
                CompressFormat.JPEG -> Bitmap.CompressFormat.JPEG
                CompressFormat.PNG -> Bitmap.CompressFormat.PNG
                CompressFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                CompressFormat.BMP -> Bitmap.CompressFormat.PNG // BMP uses PNG compression internally
            }

            val filename = "BATCH_${System.currentTimeMillis()}_${(0..1000).random()}.$ext"
            val file = File(context.cacheDir, filename)
            var out = FileOutputStream(file)
            
            // Logic for Target Size
            if (settings.targetSizeKB != null && settings.targetSizeKB > 0) {
                var currentQuality = 100
                var streamLength: Long
                val targetBytes = settings.targetSizeKB * 1024L
                
                // First attempt at 100% quality to check if target is achievable
                finalBitmap.compress(compressFmt, 100, out)
                out.flush()
                out.close()
                streamLength = file.length()
                
                // If image at 100% quality is already smaller than target, we're done
                if (streamLength <= targetBytes) {
                    // Target size is larger than what we can achieve - keep 100% quality
                    // No need to compress further
                } else {
                    // Iterative compression to reach target size
                    do {
                        file.delete()
                        out = FileOutputStream(file)
                        finalBitmap.compress(compressFmt, currentQuality, out)
                        out.flush()
                        out.close()
                        
                        streamLength = file.length()
                        if (streamLength > targetBytes) {
                            currentQuality -= 10 // Reduce quality by 10% step
                            if (currentQuality < 10) currentQuality = 5 // Minimum quality floor
                        }
                    } while (streamLength > targetBytes && currentQuality > 5)
                }
                
            } else {
                finalBitmap.compress(compressFmt, settings.quality, out)
                out.flush()
                out.close()
            }

            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
