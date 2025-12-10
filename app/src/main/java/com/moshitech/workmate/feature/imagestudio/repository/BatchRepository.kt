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
import androidx.heifwriter.HeifWriter

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

            // Determine Target Format
            var targetFormat = settings.format
            
            if (targetFormat == CompressFormat.ORIGINAL) {
                 val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                 context.contentResolver.openInputStream(uri)?.use { 
                     BitmapFactory.decodeStream(it, null, options) 
                 }
                 val mime = options.outMimeType ?: "image/jpeg"
                 
                 targetFormat = when {
                     mime.contains("png") -> CompressFormat.PNG
                     mime.contains("webp") -> CompressFormat.WEBP
                     mime.contains("bmp") -> CompressFormat.BMP
                     mime.contains("heic") || mime.contains("heif") -> CompressFormat.HEIF
                     else -> CompressFormat.JPEG // Default fallback
                 }
            }

            // Save Logic
            val ext = when (targetFormat) {
                CompressFormat.JPEG, CompressFormat.ORIGINAL -> "jpg" // Original should have been resolved above
                CompressFormat.PNG -> "png"
                CompressFormat.WEBP -> "webp"
                CompressFormat.BMP -> "bmp"
                CompressFormat.HEIF -> "heic"
            }
            val compressFmt = when (targetFormat) {
                CompressFormat.JPEG, CompressFormat.ORIGINAL -> Bitmap.CompressFormat.JPEG
                CompressFormat.PNG -> Bitmap.CompressFormat.PNG
                CompressFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                CompressFormat.BMP -> Bitmap.CompressFormat.PNG // BMP uses PNG compression internally
                CompressFormat.HEIF -> null // Special handling
            }

            val filename = "BATCH_${System.currentTimeMillis()}_${(0..1000).random()}.$ext"
            val file = File(context.cacheDir, filename)
            var out = FileOutputStream(file)
            
            // Logic for Target Size
            // Logic for Target Size or Format
            if (targetFormat == CompressFormat.HEIF && android.os.Build.VERSION.SDK_INT >= 28) {
                // HEIF Saving using HeifWriter
                // Note: HeifWriter doesn't support target size estimation easily, checking quality loops is expensive. 
                // We will respect Quality setting.
                
                try {
                    val writer = HeifWriter.Builder(
                        file.absolutePath, 
                        finalBitmap.width, 
                        finalBitmap.height, 
                        HeifWriter.INPUT_MODE_BITMAP
                    )
                    .setQuality(settings.quality)
                    .build()
                    
                    writer.start()
                    writer.addBitmap(finalBitmap)
                    writer.stop(0)
                    writer.close()
                } catch (e: Exception) {
                    // Fallback to JPEG if HEIF fails (e.g. device has no encoder)
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, settings.quality, out)
                    out.flush()
                    out.close()
                }
            } else if (settings.targetSizeKB != null && settings.targetSizeKB > 0 && compressFmt != null) {
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
                // Iterative compression to reach target size
                var resizeAttempts = 0
                var workBitmap = finalBitmap // Work on a copy/reference if we need to resize
                
                do {
                    file.delete()
                    out = FileOutputStream(file)
                    workBitmap.compress(compressFmt, currentQuality, out)
                    out.flush()
                    out.close()
                    
                    streamLength = file.length()
                    
                    
                    // Check if format ignores quality (PNG is lossless)
                    val isLossless = compressFmt == Bitmap.CompressFormat.PNG || 
                                    (android.os.Build.VERSION.SDK_INT >= 30 && compressFmt == Bitmap.CompressFormat.WEBP_LOSSLESS)

                    if (streamLength > targetBytes) {
                        // Reduce Quality first (ONLY if not lossless)
                        if (!isLossless && currentQuality > 10) {
                            currentQuality -= 10
                        } else {
                            // Resize Phase
                            // Adaptive scaling: If way off (>2x), scale aggressively (0.7), else gentle (0.9)
                            val scaleFactor = if (streamLength > targetBytes * 2) 0.7 else 0.9
                            
                            val newWidth = (workBitmap.width * scaleFactor).toInt()
                            val newHeight = (workBitmap.height * scaleFactor).toInt()
                            
                            // Safety Check: Don't go too small
                            if (newWidth > 50 && newHeight > 50 && resizeAttempts < 20) {
                                workBitmap = workBitmap.scale(newWidth, newHeight)
                                resizeAttempts++
                            } else {
                                // Can't reduce further safely - break loop
                                break
                            }
                        }
                    }
                } while (streamLength > targetBytes)
                }
                
            } else if (compressFmt != null) {
                finalBitmap.compress(compressFmt, settings.quality, out)
                out.flush()
                out.close()
            } else {
                // Fallback for unexpected null (should not happen for non-HEIF)
                 finalBitmap.compress(Bitmap.CompressFormat.JPEG, settings.quality, out)
                 out.flush()
                 out.close()
            }

            Result.success(Uri.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
