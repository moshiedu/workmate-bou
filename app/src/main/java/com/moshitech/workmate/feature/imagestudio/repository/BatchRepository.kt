package com.moshitech.workmate.feature.imagestudio.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.moshitech.workmate.feature.imagestudio.data.CompressFormat
import com.moshitech.workmate.feature.imagestudio.data.ConversionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.heifwriter.HeifWriter
import androidx.exifinterface.media.ExifInterface
import android.graphics.pdf.PdfDocument
import kotlin.coroutines.cancellation.CancellationException

class BatchRepository(private val context: Context) {

    suspend fun convertAndSaveImage(
        uri: Uri, 
        settings: ConversionSettings,
        onProgress: (Float) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f) // Starting
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext Result.failure(Exception("Cannot open stream"))
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) return@withContext Result.failure(Exception("Failed to decode bitmap"))

            onProgress(0.3f) // Decoded

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

            onProgress(0.5f) // Resized (if applicable)

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
                CompressFormat.PDF -> "pdf"
            }
            val compressFmt = when (targetFormat) {
                CompressFormat.JPEG, CompressFormat.ORIGINAL -> Bitmap.CompressFormat.JPEG
                CompressFormat.PNG -> Bitmap.CompressFormat.PNG
                CompressFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                CompressFormat.BMP -> Bitmap.CompressFormat.PNG // Bitmap.compress doesn't support BMP, fallback to PNG
                CompressFormat.HEIF -> Bitmap.CompressFormat.JPEG // Fallback for HEIF writer
                CompressFormat.PDF -> Bitmap.CompressFormat.JPEG // Fallback, handled separately
            }

            val filename = "BATCH_${System.currentTimeMillis()}_${(0..1000).random()}.$ext"
            val file = File(context.cacheDir, filename)
            var out = FileOutputStream(file)
            
            // Simulate Process for better UX during blocking compression
            val progressJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
                var p = 0.7f
                while (isActive && p < 0.95f) {
                    onProgress(p)
                    delay(150)
                    p += 0.01f
                }
            }
            
            // Logic for Target Size
            // Logic for Target Size or Format
            try {
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
            } finally {
                progressJob.cancel()
                onProgress(1.0f)
            }
            
            // Metadata Logic
            if (settings.keepMetadata) {
                try {
                    val inputPfd = context.contentResolver.openFileDescriptor(uri, "r")
                    if (inputPfd != null) {
                        val oldExif = ExifInterface(inputPfd.fileDescriptor)
                        val newExif = ExifInterface(file)
                        
                        val tags = arrayOf(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.TAG_DATETIME,
                            ExifInterface.TAG_DATETIME_ORIGINAL,
                            ExifInterface.TAG_FLASH,
                            ExifInterface.TAG_FOCAL_LENGTH,
                            ExifInterface.TAG_GPS_LATITUDE,
                            ExifInterface.TAG_GPS_LATITUDE_REF,
                            ExifInterface.TAG_GPS_LONGITUDE,
                            ExifInterface.TAG_GPS_LONGITUDE_REF,
                            ExifInterface.TAG_GPS_ALTITUDE,
                            ExifInterface.TAG_GPS_ALTITUDE_REF,
                            ExifInterface.TAG_MAKE,
                            ExifInterface.TAG_MODEL,
                            ExifInterface.TAG_WHITE_BALANCE
                        )
                        
                        tags.forEach { tag ->
                           val value = oldExif.getAttribute(tag)
                           if (value != null) {
                               newExif.setAttribute(tag, value)
                           }
                        }
                        
                        newExif.saveAttributes()
                        inputPfd.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Don't fail the whole export just for metadata
                }
            }

            // Use FileProvider to avoid FileUriExposedException on API 24+
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPdfFromImages(
        uris: List<Uri>,
        settings: ConversionSettings,
        onProgress: (Float) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val uniqueName = "Merged_${System.currentTimeMillis()}.pdf"
        
        // Use cache dir consistent with other conversions
        val pdfFile = File(context.cacheDir, uniqueName)
        
        try {
            uris.forEachIndexed { index, uri ->
                if (!isActive) return@withContext Result.failure(CancellationException("Cancelled"))
                onProgress(index.toFloat() / uris.size)
                
                // Decode and Resize (reuse logic if possible or copy simplified version)
                // limit input size to avoid OOM during PDF drawing
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { 
                    BitmapFactory.decodeStream(it, null, options) 
                }
                
                // Calculate sample size
                var sampleSize = 1
                val targetW = settings.width ?: 1024 // Default reasonable width for PDF if not set
                val targetH = settings.height ?: 1024
                
                if (options.outHeight > targetH || options.outWidth > targetW) {
                     val halfHeight: Int = options.outHeight / 2
                     val halfWidth: Int = options.outWidth / 2
                     while ((halfHeight / sampleSize) >= targetH && (halfWidth / sampleSize) >= targetW) {
                         sampleSize *= 2
                     }
                }
                
                val finalOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, finalOptions)
                }
                
                if (bitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    bitmap.recycle()
                }
            }
            
            onProgress(0.9f) // saving
            
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            
            pdfDocument.close()
            onProgress(1.0f)
            
            // Use FileProvider to avoid FileUriExposedException and allow access
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            Result.success(contentUri)
            
        } catch (e: Exception) {
            pdfDocument.close()
            Result.failure(e)
        }
    }
}
